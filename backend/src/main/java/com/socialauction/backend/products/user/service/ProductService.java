package com.socialauction.backend.products.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ProductService {
    private final ProductRepository productRepository;

    // 추천 상품 조회
    public List<ProductDto> findRecommend( Integer memberId ) {
        
    }
}
