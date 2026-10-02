package com.socialauction.backend.member.dto;

import java.time.LocalDateTime;

// 회원이 입찰한 상품 pk 조회 
public interface MproductDto {
    Integer getProductId();

    String getProductName();

    LocalDateTime getLastBidTime();
}
