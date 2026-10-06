package com.socialauction.backend.auction.user.dto;

import java.time.LocalDateTime;

public interface AuctionDetailProjection {

    // 상품 정보
    String getProductName();
    String getProductContent();
    Integer getStartPrice();

    // 경매 정보
    String getStatus();
    Integer getTopPrice();
    LocalDateTime getEndTime();

    // 총 입찰 수
    Integer getBidCount();

    // 총 입찰자 수
    Integer getBidMemberCount();
}