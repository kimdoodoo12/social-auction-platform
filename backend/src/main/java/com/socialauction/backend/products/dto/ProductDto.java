package com.socialauction.backend.products.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.socialauction.backend.products.entity.ImageEntity;
import com.socialauction.backend.products.entity.ProductEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @Data @NoArgsConstructor @Builder 
public class ProductDto {
    private Integer productId;      // 상품번호
    private String name;            // 상품이름
    private Integer organizationId; // 기관아이디
    private Integer categoryId;     // 카테고리 아이디
    private Integer startPrice;     // 시작가
    private String description;     // 설명
    private String background;      // 배경
    private LocalDateTime createdAt;// 생성 시간
    private LocalDateTime updatedAt;// 수정 시간

    private List<String> images;    // 상품 이미지경로들

    // 시작가는 제외했음
    public static ProductDto from(ProductEntity productEntity, List<ImageEntity> imageEntities) {
        return ProductDto.builder()
        .name(productEntity.getName() )
        .productId(productEntity.getProductId() )
        .categoryId(productEntity.getCategoryEntity().getCategoryId() )
        .organizationId(productEntity.getOrganizationEntity().getOrganizationId() )
        .createdAt(productEntity.getCreatedAt() )
        // 수정한 인원은 로그인 정보에서 가져오기?
        .updatedAt(productEntity.getUpdatedAt() )
        .description(productEntity.getDescription() )
        .background(productEntity.getBackground() )
        .images(imageEntities.stream().map( entity -> entity.getImage() ).toList())
        .build();
    }
}
