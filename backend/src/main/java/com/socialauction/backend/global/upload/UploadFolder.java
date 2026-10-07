package com.socialauction.backend.global.upload;

import java.nio.file.Path;

// 업로드 파일 폴더 목록. 저장 위치, 조회 URL, 정적 리소스 연결이 모두 여기 하나를 기준으로 한다.
// 새 업로드 종류가 생기면 값만 추가하면 된다.
public enum UploadFolder {
    IMAGES("images"),             // 상품 이미지  → /images/**
    ORGANIZATION("organization"); // 기관 로고·협약서 → /organization/**

    private final String name;

    UploadFolder(String name) {
        this.name = name;
    }

    // 실제 저장 폴더: <프로젝트 루트>/backend/src/main/resources/static/<name>
    public Path dir() {
        return Path.of(System.getProperty("user.dir"), "backend/src/main/resources/static", name)
                .toAbsolutePath().normalize();
    }

    // 조회 URL 접두어: /<name>/
    public String urlPrefix() {
        return "/" + name + "/";
    }

    // 클래스패스 위치 (빌드에 포함된 샘플 파일용): classpath:/static/<name>/
    public String classpathLocation() {
        return "classpath:/static/" + name + "/";
    }
}
