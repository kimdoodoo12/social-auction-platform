package com.socialauction.backend.member.admin.dto;

import java.time.LocalDateTime;

public interface MemberBidInfo {
    Integer getProductId();

    String getProductName();

    Integer getMyBidPrice();

    Integer getFinalPrice();

    String getResult();

    LocalDateTime getBidTime();
}
