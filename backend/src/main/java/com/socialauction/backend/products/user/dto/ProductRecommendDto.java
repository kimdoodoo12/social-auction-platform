package com.socialauction.backend.products.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class ProductRecommendDto {
    private Integer categoryId;
    private Integer count;
}
