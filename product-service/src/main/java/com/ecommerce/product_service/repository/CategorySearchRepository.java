package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.document.CategoryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CategorySearchRepository extends ElasticsearchRepository<CategoryDocument, String> {
}
