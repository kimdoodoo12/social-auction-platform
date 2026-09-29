package com.socialauction.backend.products.user.controller;

import org.hibernate.query.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.products.user.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/user/product")
public class ProductController {
    private final ProductService productService;

    // 추천 상품 출력
    
}
