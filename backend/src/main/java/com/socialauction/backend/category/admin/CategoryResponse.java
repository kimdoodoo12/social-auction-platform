package com.socialauction.backend.category.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor 
public class CategoryResponse {
    private Integer categoryId;
    private String name;
    private Long productCount;
}
