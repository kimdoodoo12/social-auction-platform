package com.socialauction.backend.products.admin.dto;

import com.socialauction.backend.global.upload.UploadFolder;
import com.socialauction.backend.global.upload.UploadUrls;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Builder @Data 
public class ProductManageDto {
    private Integer productId;
    private String image;
    private String productName;
    private String organizationName;
    private Integer startPrice;
    private Integer bidPrice;
    private String auctionStatus;
    private LocalDateTime createdAt;

    public String getImage() {
        return UploadUrls.from(UploadFolder.IMAGES, image);
    }
}
