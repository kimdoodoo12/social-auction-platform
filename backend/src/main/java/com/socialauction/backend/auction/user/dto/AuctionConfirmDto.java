package com.socialauction.backend.auction.user.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.auction.entity.AuctionEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class AuctionConfirmDto {
    
    @NotNull 
    private Integer auctionId;

    @NotNull @Positive 
    private Integer price;

    @NotNull 
    private Long memberId;
}

// public AuctionEntity toEntity(){

//     return AuctionEntity.builder()
//                 .auctionId(this.auctionId)
                
//                 .build()
// }
