package com.socialauction.backend.products.admin.dto;

import com.socialauction.backend.global.UploadFolder;
import com.socialauction.backend.global.UploadUrls;

import org.springframework.web.multipart.MultipartFile;

import com.socialauction.backend.products.entity.ImageEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Builder @Data 
public class ImageDto {
    private Integer imageId;
    private Integer productId;
    private String image;
    private Integer sortOrder; // 이미지 위치: 1~5, 1은 필수 대표 이미지
    private MultipartFile file;     // 업로드/등록용
    
    public static ImageDto from(ImageEntity entity) {
        return ImageDto.builder()
        .imageId(entity.getImageId() )
        .productId(entity.getProductEntity().getProductId() )
        .image(UploadUrls.from(UploadFolder.IMAGES, entity.getImage()))
        .sortOrder(entity.getSortOrder())
        .build();
    }
}
