package com.socialauction.backend.auction.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.products.admin.dto.ImageDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class AuctionDetailDto {
    //전체 데이터 포함
    AuctionFindAllDto auctionFindAllDto;

    //카테고리 정보
    Integer categoryId;
    String categoryName;

    //상품 정보
    @Builder.Default
    List<ImageDto> imageList = new ArrayList<>();

    //기관정보
    String manager;
    LocalDateTime agreementDate;

    //유저정보
    String userName;
    Long countUser;

    public static AuctionDetailDto from(AuctionEntity auctionEntity){
        return AuctionDetailDto.builder()
            .auctionFindAllDto(AuctionFindAllDto.from(auctionEntity))
            .categoryId(auctionEntity.getProductEntity().getCategoryEntity().getCategoryId())
            .categoryName(auctionEntity.getProductEntity().getCategoryEntity().getName())
            .manager(auctionEntity.getProductEntity().getOrganizationEntity().getManager())
            .agreementDate(auctionEntity.getProductEntity().getOrganizationEntity().getAgreementDate())
            //이미지는 서비스에서
            // 최고입찰자 이름 서비스에서
            // 참여입찰자는 서비스에서
            .build();
    }


}
