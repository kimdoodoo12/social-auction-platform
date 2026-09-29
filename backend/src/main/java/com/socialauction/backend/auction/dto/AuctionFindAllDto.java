package com.socialauction.backend.auction.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.auction.entity.AuctionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class AuctionFindAllDto {
    
    private Integer auctionId;

    private String productName;

    private String organizationName;

    private Integer startPrice;

    //현재 최고가
    private Integer topPrice;

    //입찰 수량
    private Integer bidCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public static AuctionFindAllDto from(AuctionEntity auctionEntity){
        return AuctionFindAllDto.builder()
            .auctionId(auctionEntity.getAuctionId())
            .productName(auctionEntity.getProductEntity().getName())
            .organizationName(auctionEntity.getProductEntity().getOrganizationEntity().getName())
            .startPrice(auctionEntity.getProductEntity().getStartPrice())
            //최고가는 서비스에서
            //입찰 수는 서비스에서
            .startTime(auctionEntity.getStartTime())
            .endTime(auctionEntity.getEndTime())
            .build();
    }
}
