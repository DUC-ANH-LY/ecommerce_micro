package com.ecommerce.product_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @Schema(description = "The name of the category", example = "Electronics")
    private String name;
    
    @Schema(description = "A brief description of the category", example = "Electronic devices and accessories")
    private String description;
}
