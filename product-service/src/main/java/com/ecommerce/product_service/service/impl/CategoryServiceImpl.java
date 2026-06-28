package com.ecommerce.product_service.service.impl;

import com.ecommerce.product_service.dto.request.CategoryRequest;
import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.repository.CategoryRepository;
import com.ecommerce.product_service.repository.CategorySearchRepository;
import com.ecommerce.product_service.document.CategoryDocument;
import com.ecommerce.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategorySearchRepository categorySearchRepository;

    @Override
    public Category createCategory(CategoryRequest categoryRequest) {
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .build();
        category = categoryRepository.save(category);
        syncSingleCategoryToElasticsearch(category);
        return category;
    }

    @Override
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(String id, CategoryRequest categoryRequest) {
        Category existingCategory = getCategoryById(id);
        existingCategory.setName(categoryRequest.getName());
        existingCategory.setDescription(categoryRequest.getDescription());
        existingCategory = categoryRepository.save(existingCategory);
        syncSingleCategoryToElasticsearch(existingCategory);
        return existingCategory;
    }

    @Override
    public void deleteCategory(String id) {
        Category existingCategory = getCategoryById(id);
        categoryRepository.delete(existingCategory);
        categorySearchRepository.deleteById(id);
    }

    @Override
    public void syncToElasticsearch() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDocument> docs = categories.stream().map(c -> 
            CategoryDocument.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .build()
        ).toList();
        categorySearchRepository.saveAll(docs);
        System.out.println("Synced " + docs.size() + " categories to Elasticsearch");
    }

    private void syncSingleCategoryToElasticsearch(Category c) {
        CategoryDocument doc = CategoryDocument.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .build();
        categorySearchRepository.save(doc);
    }
}
