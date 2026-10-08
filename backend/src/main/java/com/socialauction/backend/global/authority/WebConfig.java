package com.socialauction.backend.global.authority;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer{
    private final AdminInterceptor adminInterceptor;


    // 관리자인지 사용자인이 확인기능 미구현
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/ieum/admin/**") // 아직 경로 다 추가 안함. 
                .excludePathPatterns("/ieum/admin/member/user/signup",
                                            "/ieum/admin/member/user/signup/findid",
                                            "/ieum/admin/member/user/login",
                                            "/ieum/admin/member/reissue");
    }
}