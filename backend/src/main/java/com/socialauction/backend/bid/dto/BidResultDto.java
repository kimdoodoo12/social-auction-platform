package com.socialauction.backend.bid.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class BidResultDto {

    private Integer auctionId;

    private String name;

    private String phone;

    private Integer bidPrice;

    private LocalDateTime endTime;

    private boolean paymentStatus;
}
