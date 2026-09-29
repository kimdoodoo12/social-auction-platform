package com.socialauction.backend.products.admin.dto;

import java.time.LocalDateTime;

public class RecentBid {
    private Integer bidNumber;
    private Integer totalCount;
    private Integer memberId;
    private String memberName;
    private Integer bidPrice;
    private LocalDateTime bidTime;
}
