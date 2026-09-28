package com.socialauction.backend.bid.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.bid.entity.BidEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class BidDto {
    private Integer bidId;

    private String name;

    private Integer memberId;

    private Integer bidPrice;

    private LocalDateTime bidTime;


    public static BidDto from(BidEntity bidEntity){
        return BidDto.builder()
            .bidId(bidEntity.getBidId())
            .name(bidEntity.getMemberEntity().getName())
            .memberId(bidEntity.getMemberEntity().getMemberId()) 
            .bidPrice(bidEntity.getBidPrice())
            .bidTime(bidEntity.getBidTime())
        .build();
    }
}
