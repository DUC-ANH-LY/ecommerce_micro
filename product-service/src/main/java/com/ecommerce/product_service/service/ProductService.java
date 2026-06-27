package com.ecommerce.product_service.service;

import com.ecommerce.product_service.entity.Product;
import org.springframework.data.domain.Page;

import com.ecommerce.product_service.dto.request.ProductRequest;

public interface ProductService {
    Page<Product> getProducts(int page, int size);
    Page<Product> searchProducts(String keyword, int page, int size);
    void syncToElasticsearch();
    
    Product createProduct(ProductRequest request);
    Product getProductById(String id);
    Product updateProduct(String id, ProductRequest request);
    void deleteProduct(String id);
}
