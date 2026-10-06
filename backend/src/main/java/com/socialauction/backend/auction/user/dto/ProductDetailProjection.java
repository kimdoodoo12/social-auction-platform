package com.socialauction.backend.auction.user.dto;

import java.time.LocalDateTime;

public interface ProductDetailProjection {

    // * 상품 ID: 상품 식별 및 상세 페이지 이동에 사용
    Integer getProductId();

    // * 상품명
    String getProductName();

    // * 상품 상세 설명
    String getDescription();

    // * 상품 제작 배경·사연
    String getBackground();

    // * 제작 기관 ID: 기관 상세 페이지 이동에 사용
    Integer getOrganizationId();

    // * 제작 기관명
    String getOrganizationName();

    // * 제작 기관 소개
    String getOrganizationDescription();

    // * 제작 기관 프로필 이미지 (상품 사진과는 별개)
    String getOrganizationImage();

    // * 기관 협약일
    LocalDateTime getAgreementDate();

    // * 해당 기관이 등록한 전체 상품 수
    Long getRegisteredProductCount();
}
