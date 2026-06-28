package com.ecommerce.product_service.service.impl;

import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.repository.CategoryRepository;
import com.ecommerce.product_service.repository.ProductRepository;
import com.ecommerce.product_service.repository.ProductSearchRepository;
import com.ecommerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.ecommerce.product_service.dto.request.ProductRequest;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Cacheable(value = "products", key = "#page + '-' + #size")
    public Page<Product> getProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    // @Cacheable(value = "productSearch", key = "#keyword + '-' + #page + '-' + #size")
    public Page<Product> searchProducts(String keyword, int page, int size) {
        String queryString = "{\"query_string\": {\"query\": \"*" + keyword + "* OR " + keyword + "~\", \"fields\": [\"name\", \"description\", \"categories\"], \"lenient\": true}}";
        Query query = new StringQuery(queryString);
        query.setPageable(PageRequest.of(page, size));

        SearchHits<Object> searchHits = elasticsearchOperations.search(
                query,
                Object.class,
                IndexCoordinates.of("products", "categories")
        );

        List<String> productIds = new ArrayList<>();
        List<String> categoryIds = new ArrayList<>();
        java.util.Map<String, Float> scoreMap = new java.util.HashMap<>();

        for (SearchHit<Object> hit : searchHits) {
            scoreMap.put(hit.getId(), hit.getScore());
            if ("products".equals(hit.getIndex())) {
                productIds.add(hit.getId());
            } else if ("categories".equals(hit.getIndex())) {
                categoryIds.add(hit.getId());
            }
        }

        if (productIds.isEmpty() && categoryIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        List<Category> matchedCategories = Collections.emptyList();
        if (!categoryIds.isEmpty()) {
            matchedCategories = categoryRepository.findAllById(categoryIds);
        }

        List<Product> products = productRepository.findByIdInOrCategoriesIn(productIds, matchedCategories);

        // Sort products by Elasticsearch relevance score (Descending)
        products.sort((p1, p2) -> {
            float score1 = scoreMap.getOrDefault(p1.getId(), 0f);
            if (p1.getCategories() != null) {
                for (Category c : p1.getCategories()) {
                    score1 = Math.max(score1, scoreMap.getOrDefault(c.getId(), 0f));
                }
            }
            float score2 = scoreMap.getOrDefault(p2.getId(), 0f);
            if (p2.getCategories() != null) {
                for (Category c : p2.getCategories()) {
                    score2 = Math.max(score2, scoreMap.getOrDefault(c.getId(), 0f));
                }
            }
            return Float.compare(score2, score1);
        });

        // Simple manual pagination for the resulting products (since they are fetched combined)
        int start = (int) PageRequest.of(page, size).getOffset();
        int end = Math.min((start + size), products.size());
        List<Product> pagedProducts = products.isEmpty() || start >= products.size() ? 
                Collections.emptyList() : products.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pagedProducts, PageRequest.of(page, size), products.size());
    }

    @Override
    public void syncToElasticsearch() {
        int page = 0;
        int size = 5000; // process 5000 records at a time
        Page<Product> productPage;
        do {
            productPage = productRepository.findAll(PageRequest.of(page, size));
            List<com.ecommerce.product_service.document.ProductDocument> docs = productPage.getContent().stream().map(p -> {
                Set<String> catNames = p.getCategories() != null ? 
                    p.getCategories().stream().map(Category::getName).collect(Collectors.toSet()) : null;
                return com.ecommerce.product_service.document.ProductDocument.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .price(p.getPrice())
                        .stock(p.getStock())
                        .categories(catNames)
                        .build();
            }).toList();
            productSearchRepository.saveAll(docs);
            page++;
            System.out.println("Synced page " + page + " to Elasticsearch");
        } while (productPage.hasNext());
    }

    @Override
    @CacheEvict(value = {"products", "productSearch"}, allEntries = true)
    public Product createProduct(ProductRequest request) {
        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .categories(categories)
                .build();

        product = productRepository.save(product);
        syncSingleProductToElasticsearch(product);
        return product;
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    @CacheEvict(value = {"products", "productSearch"}, allEntries = true)
    public Product updateProduct(String id, ProductRequest request) {
        Product existing = getProductById(id);
        
        Set<Category> categories = existing.getCategories();
        if (request.getCategoryIds() != null) {
            categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());
        existing.setCategories(categories);

        existing = productRepository.save(existing);
        syncSingleProductToElasticsearch(existing);
        return existing;
    }

    @Override
    @CacheEvict(value = {"products", "productSearch"}, allEntries = true)
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
        productSearchRepository.deleteById(id);
    }

    private void syncSingleProductToElasticsearch(Product p) {
        Set<String> catNames = p.getCategories() != null ? 
            p.getCategories().stream().map(Category::getName).collect(Collectors.toSet()) : null;
        com.ecommerce.product_service.document.ProductDocument doc = com.ecommerce.product_service.document.ProductDocument.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .categories(catNames)
                .build();
        productSearchRepository.save(doc);
    }
}
