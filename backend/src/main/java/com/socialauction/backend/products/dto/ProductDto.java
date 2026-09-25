package com.socialauction.backend.products.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.products.entity.ProductEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @Data @NoArgsConstructor @Builder 
public class ProductDto {
    private Integer productId;
    private String name;
    private Integer OrganizationId;
    private Integer categoryId;
    private Integer startPrice;
    private String description;
    private String background;
    private LocalDateTime createdAt;

    public ProductEntity toEntity(){
        return ProductEntity.builder()
        .name(this.name)
        .description(this.description)
        .background(this.background)
        .build();
    }
}
