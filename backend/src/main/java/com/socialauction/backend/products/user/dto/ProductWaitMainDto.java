package com.socialauction.backend.products.user.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
// 상품 상세 - 경매 대기 - 메인화면
public class ProductWaitMainDto {
    private List<String> images = new ArrayList<>();
    private String auctionStatus;
    private String organizationName;
    private String productName;
    private Integer startPrice;
}
