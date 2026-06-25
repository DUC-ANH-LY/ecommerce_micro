package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
