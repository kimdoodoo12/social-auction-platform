package com.socialauction.backend.products.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.dto.ProductRecommendDto;
import com.socialauction.backend.products.user.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service("userProductService") 
@RequiredArgsConstructor 
public class ProductService {
    private final ProductRepository productRepository;
    
    // 추천 상품 조회
    // public List<ProductDto> findRecommendProduct( Long memberId ) {
    //     // 카테고리, 입찰횟수를 담는 dto
    //     // List<ProductRecommendDto> recommendDtos = productRepository.findcount(memberId);
    //     // private int total = 0;
    //     // recommendDtos.forEach( a -> {
    //     //     a.getCategoryId();
    //     //     total += a.getCount();
    //     // });

    //     // 추천 로직
    //     // int random;
    //     // random = (int)Math.random() * total;
    //     // int categoryNumber;
    //     // if(random < ) 

        
    //}
}
