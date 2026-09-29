package com.socialauction.backend.products.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class ProductDto {
    private String auctionStatus; // 경매상태
    private String organizationName; // 기관 이름
    private String ProductName; // 상품 이름
    private Integer currentPrice; // 현재
    private Lo
}
