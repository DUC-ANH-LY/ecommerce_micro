package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.request.CategoryRequest;
import com.ecommerce.product_service.entity.Category;
import com.ecommerce.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Endpoints for managing categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Adds a new category to the database")
    public Category createCategory(@RequestBody CategoryRequest categoryRequest) {
        return categoryService.createCategory(categoryRequest);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID", description = "Retrieves a category based on its unique ID")
    public Category getCategoryById(@PathVariable String id) {
        return categoryService.getCategoryById(id);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves a list of all categories in the database")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Updates an existing category's name or description")
    public Category updateCategory(@PathVariable String id, @RequestBody CategoryRequest categoryRequest) {
        return categoryService.updateCategory(id, categoryRequest);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Removes a category from the database by its ID")
    public void deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
    }
}
