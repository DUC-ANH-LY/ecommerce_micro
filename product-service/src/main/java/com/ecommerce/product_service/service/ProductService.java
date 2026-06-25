package com.ecommerce.product_service.service;

import com.ecommerce.product_service.entity.Product;
import org.springframework.data.domain.Slice;

public interface ProductService {
    Slice<Product> getProducts(int page, int size);
    Slice<Product> searchProducts(String keyword, int page, int size);
}
