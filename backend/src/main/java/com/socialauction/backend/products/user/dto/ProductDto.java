package com.socialauction.backend.products.user.dto;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class ProductDto {
    private String auctionStatus; // 경매상태
    private String image;       // 저장된 이미지 파일명
    private String organizationName; // 기관 이름
    private String ProductName; // 상품 이름
    private Long currentPrice; // 현재가
    private LocalDateTime endTime; // 경매 종료 시간
    private Long bidCount; // 입찰 횟수

}
