package com.socialauction.backend.products.dto;

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
    
    public static ImageDto from(ImageEntity entity) {
        return ImageDto.builder()
        .imageId(entity.getImageId() )
        .productId(entity.getProductEntity().getProductId() )
        .image(entity.getImage() )
        .build();
    }
}
