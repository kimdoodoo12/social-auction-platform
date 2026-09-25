package com.socialauction.backend.products.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.products.dto.ProductListResponse;
import com.socialauction.backend.products.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // 상품 첫 화면 조회
    @GetMapping("")
    public Page<ProductListResponse> findAll(
            @PageableDefault(size = 8, sort = {"createdAt", "productId"},
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return productService.findAll(pageable);
    }
}
