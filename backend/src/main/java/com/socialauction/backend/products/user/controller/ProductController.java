package com.socialauction.backend.products.user.controller;

import java.util.List;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController("userProductController") 
@RequiredArgsConstructor 
@RequestMapping ("/user/product")
public class ProductController {
    private final ProductService productService;
    /* ----------- 메인페이지 ------------------ */
    // 추천 상품 조회
    @GetMapping("aa")
    public ProductDto findRecommendProduct(Long memberId ) {
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

<<<<<<< HEAD

    /* ------------- 상품목록 페이지 ------------------- */
    // 상품 목록
    @GetMapping("/list")
    public Page<ProductDto> findList(
        @PageableDefault (size = 9) Pageable pageable,
        @RequestParam (value = "productName", defaultValue = "") String productName,
        @RequestParam (value = "organizationId", required = false) Integer organizationId,
        @RequestParam (value = "auctionStatus", required = false) String auctionStatus,
        @RequestParam (value = "categoryId", required = false) Integer categoryId,
        @RequestParam (value = "lowprice", required = false) Integer lowprice,
        @RequestParam (value = "highprice", required = false) Integer highprice,
        @RequestParam (value = "sortType", defaultValue = "popular") String sort
    ) {
        return productService.findList(
            pageable, productName,organizationId,auctionStatus,
            categoryId,lowprice,highprice,sort
        );
    }
=======
    // // 추천 상품 조회
    // @GetMapping("aa")
    // public List<ProductDto> findRecommendProduct(Long memberId ) {
    //     return productService.findRecommendProduct(memberId);
    // }
    
    
    
>>>>>>> 77c0cf1a7b827598c2d3e44e3ac5fd85a37f416a
}
