package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.request.CategoryRequest;
import com.ecommerce.product_service.entity.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(CategoryRequest categoryRequest);
    Category getCategoryById(String id);
    List<Category> getAllCategories();
    Category updateCategory(String id, CategoryRequest categoryRequest);
    void deleteCategory(String id);
}
