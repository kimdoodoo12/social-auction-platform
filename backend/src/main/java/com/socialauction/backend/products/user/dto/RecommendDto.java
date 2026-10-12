package com.socialauction.backend.products.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecommendDto {

    private Integer productId;          // 상품 ID
    private Integer categoryId;         // 카테고리 ID
    private String auctionStatus;       // 경매 상태
    private String image;               // 이미지 파일명
    private String organizationName;    // 기관 이름
    private String productName;         // 상품 이름
    private Long currentPrice;          // 현재가 (쿼리에서 CAST AS SIGNED로 BIGINT 고정)
    private LocalDateTime endTime;      // 경매 종료 시간
    private Long bidCount;              // 입찰 횟수
    private Double interestScore;       // 저장된 관심 점수, 미조회 상품은 2점
    private LocalDateTime lastViewedAt; // 마지막 조회 시각
}
