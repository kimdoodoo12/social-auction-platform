package com.socialauction.backend.member.admin.dto;

import java.time.LocalDateTime;

public interface MemberBidInfoDto {
    // 입찰번호
    Integer getBidId();

    Integer getProductId();

    String getProductName();

    Integer getMyBidPrice();

    Integer getFinalPrice();

    String getResult();

    LocalDateTime getBidTime();
}
