package com.socialauction.backend.products.user.controller;

import java.util.List;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.service.ProductService;

import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.GetMapping;




@RestController("userProductController") 
@RequiredArgsConstructor 
@RequestMapping ("/user/product")
public class ProductController {
    private final ProductService productService;

    // 추천 상품 조회
    @GetMapping("aa")
    public ProductDto findRecommendProduct(Integer memberId ) {
        return productService.findRecommendProduct(memberId);
    }
    

    

    // 인기 상품 조회
    @GetMapping("cc")
    public List<ProductDto> findPopularProduct( ) {
        return productService.findPopularProduct();
    }
    



    
    
    
}
