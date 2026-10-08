package com.socialauction.backend.products.user.dto;

import com.socialauction.backend.global.upload.UploadFolder;
import com.socialauction.backend.global.upload.UploadUrls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class ProductDto {
    private String auctionStatus; // 경매상태
    private String image;       // 이미지 경로
    private String organizationName; // 기관 이름
    private String ProductName; // 상품 이름
    private Long currentPrice; // 현재가
    // 남은 시간도 추가해야함 , endTime을 넣어야 함.
    private Long bidCount; // 입찰 횟수

    public String getImage() {
        return UploadUrls.from(UploadFolder.IMAGES, image);
    }
}
