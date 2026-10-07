package com.socialauction.backend.global.upload;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 업로드 폴더(UploadFolder)를 /<폴더>/** 로 직접 제공한다.
// 업로드 직후 파일도 빌드 없이 바로 조회되고, 실행 방법(bin/build 클래스패스)이나
// 스프링 기본 정적 리소스 설정과 무관하게 동작한다.
@Configuration
public class UploadWebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        for (UploadFolder folder : UploadFolder.values()) {
            String location = folder.dir().toUri().toString();
            registry.addResourceHandler(folder.urlPrefix() + "**")
                    .addResourceLocations(location.endsWith("/") ? location : location + "/",
                            folder.classpathLocation())
                    .setCachePeriod(0);
        }
    }
}
