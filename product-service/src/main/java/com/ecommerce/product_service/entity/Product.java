package com.ecommerce.product_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import lombok.*;
import java.math.BigDecimal;
import java.util.Set;

import java.io.Serializable;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product implements Serializable {
    @Id
    private String id;

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    @DocumentReference(lazy = true)
    private Set<Category> categories;
}
