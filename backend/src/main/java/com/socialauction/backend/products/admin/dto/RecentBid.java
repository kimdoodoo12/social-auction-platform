package com.socialauction.backend.products.admin.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class RecentBid {
    private Long bidNumber;
    private Long totalCount;
    private Integer memberId;
    private String memberName;
    private Integer bidPrice;
    private LocalDateTime bidTime;
}
