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

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final ProductSearchRepository productSearchRepository;

    @Cacheable(value = "products", key = "#page + '-' + #size")
    public Page<Product> getProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    // @Cacheable(value = "productSearch", key = "#keyword + '-' + #page + '-' + #size")
    public Page<Product> searchProducts(String keyword, int page, int size) {
        // 1. Search in Elasticsearch
        Page<com.ecommerce.product_service.document.ProductDocument> documentPage = 
            productSearchRepository.searchByKeyword(keyword, PageRequest.of(page, size));
            
        // 2. Extract IDs
        List<String> ids = documentPage.getContent().stream()
                .map(com.ecommerce.product_service.document.ProductDocument::getId)
                .toList();
                
        if (ids.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }
        
        // 3. Fetch from MongoDB to get full Product entities (including lazy Categories)
        List<Product> products = (List<Product>) productRepository.findAllById(ids);
        
        // Return as Page
        return new org.springframework.data.domain.PageImpl<>(products, PageRequest.of(page, size), documentPage.getTotalElements());
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
