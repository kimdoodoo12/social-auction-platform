package com.socialauction.backend.auction.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class AuctionFindDto {
    //상품 정보
    private String productName;
    private String productContent;
    private Integer startPrice;

    //경매 정보
    private String status;
    private Integer topPrice;
    private LocalDateTime endTime;

    //총 입찰 수
    private Integer bidCount;

    //총 입찰자 수
    private Integer bidMemberCount;
    
}
