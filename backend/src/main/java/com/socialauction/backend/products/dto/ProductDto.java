package com.socialauction.backend.products.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.socialauction.backend.products.entity.ProductEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @Data @NoArgsConstructor @Builder 
public class ProductDto {
    private Integer productId;      // 상품번호
    private String name;            // 상품이름
    private Integer OrganizationId; // 기관아이디
    private Integer categoryId;     // 카테고리 아이디
    private Integer startPrice;     // 시작가
    private String description;     // 설명
    private String background;      // 배경
    private LocalDateTime createdAt;// 생성 시간
    private LocalDateTime updatedAt;// 수정 시간

    private List<ProductDto> images;// 상품 이미지들

    public ProductEntity toEntity(){
        return ProductEntity.builder()
        .name(this.name)
        .description(this.description)
        .background(this.background)
        .build();
    }
}
