package com.ecommerce.product_service.service;

import com.ecommerce.product_service.entity.Product;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<Product> getProducts(int page, int size);
    Page<Product> searchProducts(String keyword, int page, int size);
}
