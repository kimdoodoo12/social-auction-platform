package com.socialauction.backend.global;

import java.nio.charset.StandardCharsets;
import org.springframework.web.util.UriUtils;

public final class ProductImageUrls {
    private ProductImageUrls() {}

    public static String from(String image) {
        if (image == null || image.isBlank()) return null;
        // 기존 경로/외부 URL은 유지하고 신규 저장 파일명만 URL로 변환한다.
        if (image.startsWith("/") || image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        return "/images/" + UriUtils.encodePathSegment(image, StandardCharsets.UTF_8);
    }
}
