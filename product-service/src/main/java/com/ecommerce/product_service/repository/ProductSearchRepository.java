package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
    
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"description\", \"categories\"]}}")
    Page<ProductDocument> searchByKeyword(String keyword, Pageable pageable);
}
