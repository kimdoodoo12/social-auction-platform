package com.socialauction.backend.auction.user.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.auction.entity.AuctionEntity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// * 인증된 입찰 정보를 큐·서비스·커밋 후 이벤트에 전달하는 내부 DTO다.
// ! HTTP 요청 바인딩에는 회원 ID가 없는 AuctionBidRequest를 사용한다.
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
