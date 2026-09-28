package com.socialauction.backend.products.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.products.dto.ProductDto;
import com.socialauction.backend.products.dto.ProductListResponse;
import com.socialauction.backend.products.service.ProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequiredArgsConstructor
@RequestMapping ("/product")
public class ProductController {
    private final ProductService productService;

    // 상품 첫 화면(상품관리) 조회
    @GetMapping("aa") // 주소 추후에 설정
    public Page<ProductListResponse> findAll(
            @PageableDefault(size = 8, sort = {"createdAt", "productId"},
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.findAll(pageable);
    }

    // 상품 상세 - 기본정보,상품설명, 상품이미지 조회
    @GetMapping("bb") // 주소 추후에 설정
    public ProductDto findDetail(@RequestParam (name = "productId") Integer productId) {
        return productService.findDetail(productId);
    }
    
    // 상태 이력

    // 판매 중지 
    @PostMapping("tt")
    public boolean stopSelling( @RequestParam (name = "productId" ) Integer productId ) {
        return productService.stopSelling(productId);
    }
    


    // 상품 등록
    @PostMapping("cc")
    public boolean saveProduct(@RequestBody ProductDto productDto) {
        return productService.saveProduct(productDto);
    }
    
    // 상품 수정
    @PutMapping("dd")
    public boolean updateProduct(@RequestBody ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

} // controller end
