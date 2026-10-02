package com.socialauction.backend.member.dto;

import java.time.LocalDateTime;

public interface MemberBhistory {
    Integer getProductId();

    String getProductName();

    Integer getMyBidPrice();

    Integer getFinalPrice();

    String getResult();

    LocalDateTime getBidTime();
}
