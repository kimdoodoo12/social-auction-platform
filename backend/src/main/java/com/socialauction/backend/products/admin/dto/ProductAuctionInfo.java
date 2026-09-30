package com.socialauction.backend.products.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class ProductAuctionInfo {
    private Integer auctionId;
    private Integer startPrice;
    private Integer currentPrice;
    private Long bidCount;
    private Long bidderCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
}

