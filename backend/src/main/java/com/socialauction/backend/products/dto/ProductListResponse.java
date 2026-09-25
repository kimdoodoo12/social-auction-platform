package com.socialauction.backend.products.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.products.entity.ProductEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductListResponse {

    private Integer productId;          // 상품번호
    private String imageUrl;            // 대표 이미지 URL
    private String productName;         // 상품명
    private String organizationName;    // 제작기관 이름
    private Integer startPrice;         // 시작가
    private Integer currentPrice;       // 현재가
    private String status;              // 상태
    private LocalDateTime createdAt;    // 등록일

    public static ProductListResponse from(
        ProductEntity entity,
        String imageUrl,
        Integer currentPrice,
        String status
    ){
        return ProductListResponse.builder()
        .productId( entity.getProductId() )
        .imageUrl( imageUrl ) // 이미지 테이블
        .productName( entity.getName())
        .organizationName( entity.getOrganizationEntity().getName() )
        .startPrice( entity.getStartPrice() )
        .currentPrice(currentPrice ) // 입찰기록 테이블
        .status( status ) // 경매 테이블
        .createdAt( entity.getCreatedAt() )
        .build();

    }
}