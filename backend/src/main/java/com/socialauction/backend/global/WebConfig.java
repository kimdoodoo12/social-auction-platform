// package com.socialauction.backend.global;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// import lombok.RequiredArgsConstructor;


// @Configuration
// @RequiredArgsConstructor
// public class WebConfig implements WebMvcConfigurer{
//     private final AdminInterceptor adminInterceptor;

//     @Override
//     public void addInterceptors(InterceptorRegistry registry){
//         registry.addInterceptor(adminInterceptor)
//                 .addPathPatterns("/admin/**"); // 아직 경로 다 추가 안함. 
//     }
// }
