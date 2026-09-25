package com.socialauction.backend.products.dto;

// 상품 목록에서 사용하는 경매 상태와 최고 입찰가 조회 결과
public interface ProductAuctionSummary {
    Integer getProductId();
    String getStatus();
    Integer getCurrentPrice();
}
