
package com.socialauction.backend.auction.admin.dto;

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
    private Integer topPrice;
    private Integer bidCount;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // [추가] JPQL에서 MAX(b.bidPrice)와 COUNT(b)를 계산해 바로 DTO로 만들 때 사용하는 생성자
    public AuctionFindAllDto(
        Integer auctionId,
        String productName,
        String organizationName,
        Integer startPrice,
        Integer topPrice,
        Long bidCount,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.organizationName = organizationName;
        this.startPrice = startPrice;
        this.topPrice = topPrice != null ? topPrice : 0;
        this.bidCount = bidCount != null ? bidCount.intValue() : 0;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static AuctionFindAllDto from(AuctionEntity auctionEntity){
        return AuctionFindAllDto.builder()
            .auctionId(auctionEntity.getAuctionId())
            .productName(auctionEntity.getProductEntity().getName())
            .organizationName(auctionEntity.getProductEntity().getOrganizationEntity().getName())
            .startPrice(auctionEntity.getProductEntity().getStartPrice())
            .status(auctionEntity.getAuctionStatus())
            .startTime(auctionEntity.getStartTime())
            .endTime(auctionEntity.getEndTime())
            .build();
    }
}