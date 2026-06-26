package com.ecommerce.product_service.service.impl;

import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.repository.CategoryRepository;
import com.ecommerce.product_service.repository.ProductRepository;
import com.ecommerce.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Cacheable(value = "products", key = "#page + '-' + #size")
    public Page<Product> getProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    @Cacheable(value = "productSearch", key = "#keyword + '-' + #page + '-' + #size")
    public Page<Product> searchProducts(String keyword, int page, int size) {
        List<Category> matchingCategories = categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);

        if (matchingCategories.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, PageRequest.of(page, size));
        } else {
            return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoriesIn(keyword, keyword, matchingCategories, PageRequest.of(page, size));
        }
    }
}
