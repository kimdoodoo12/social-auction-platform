package com.socialauction.backend.member.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder
public class MBResultDto {
    // 경매번호
    private Integer auctionId;
    // 상품명
    private String product;
    // 제작기관
    private String organization;
    // 낙찰가
    private Integer paymentPrice;
    // 낙찰일
    private LocalDateTime winningDate;
    // 결제상태
    private String paymentStatus;
}
