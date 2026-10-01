package com.socialauction.backend.products.user.controller;

import java.util.List;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.service.ProductService;

import lombok.RequiredArgsConstructor;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




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
    
    // 마감 임박 상품 조회
    @GetMapping("bb")
    public String findEndProduct(@RequestParam String param) {
        return "bb";
    }
    

    // 인기 상품 조회
    @GetMapping("cc")
    public List<ProductDto> findPopularProduct( ) {
        return productService.findPopularProduct();
    }
    


    // 새로 등록된 상품 조회
    @GetMapping("dd")
    public List<ProductDto> findNewProduct( ) {
        return productService.findNewProduct();
    }
    
    
    
}
