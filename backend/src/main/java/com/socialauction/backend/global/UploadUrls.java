package com.socialauction.backend.global;

import java.nio.charset.StandardCharsets;
import org.springframework.web.util.UriUtils;

// 저장된 파일명을 조회 가능한 URL로 바꾼다. (예: images, "a b.png" → /images/a%20b.png)
public final class UploadUrls {
    private UploadUrls() {}

    public static String from(UploadFolder folder, String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        // 기존 경로/외부 URL은 유지하고 저장 파일명만 URL로 변환한다.
        if (fileName.startsWith("/") || fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return fileName;
        }
        return folder.urlPrefix() + UriUtils.encodePathSegment(fileName, StandardCharsets.UTF_8);
    }
}
