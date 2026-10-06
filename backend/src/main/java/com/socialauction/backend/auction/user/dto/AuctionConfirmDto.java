package com.socialauction.backend.auction.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class AuctionConfirmDto {
    
    private Integer auctionId;

    private Integer price;

    private Integer memberId;
}
