package com.socialauction.backend.products.admin.dto;

import java.time.LocalDateTime;

public class ProductAuctionInfo {
    private Integer startPrice;
    private Integer currentPrice;
    private Integer bidCount;
    private Integer bidderCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer auctionId;
}

