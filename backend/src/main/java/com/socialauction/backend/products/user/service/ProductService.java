package com.socialauction.backend.products.user.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.dto.ProductRecommendDto;
import com.socialauction.backend.products.user.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service("userProductService") 
@RequiredArgsConstructor 
public class ProductService {
    private final ProductRepository productRepository;
    
    // 추천 상품 조회
    public ProductDto findRecommendProduct( Long memberId ) {
        // 카테고리, 입찰횟수를 담는 dto
        List<ProductRecommendDto> recommendDtos = productRepository.findCount(memberId);
        int total = 0;
    // public List<ProductDto> findRecommendProduct( Long memberId ) {
    //     // 카테고리, 입찰횟수를 담는 dto
    //     // List<ProductRecommendDto> recommendDtos = productRepository.findcount(memberId);
    //     // private int total = 0;
    //     // recommendDtos.forEach( a -> {
    //     //     a.getCategoryId();
    //     //     total += a.getCount();
    //     // });

        for ( int i = 0; i < recommendDtos.size(); i++ ) {
            total += recommendDtos.get(i).getCount(); // 총 입찰 횟수
        }
        // 입찰 횟수가 없는경우 바로 로직을 적용하지 않은 랜덤 상품 추천
        if ( total == 0 ) { return productRepository.findRecommend(null); }

        // 추천 로직(사용자가 많이 입찰한 카테고리의 상품을 추천)
        int random; // 랜덤값 저장
        random = (int) (Math.random() * total) + 1; // 랜덤 숫자 범위 : 1~total
        Integer categoryNumber = 0;; // 추천할 카테고리 번호
        int recent = 0; // 이전값

        // 입찰 횟수를 모두 더해서 총 입찰 횟수를 구하고,
        // 카테고리 별 입찰횟수만큼 가중치를 가짐
        // 예) 카테고리1의 입찰횟수가 13, 총 입찰횟수가 50
        // 카테고리1이 나올 확률은 13/50
        // 그러므로 입찰횟수가 많을 수록 그 카테고리가 나올 확률이 올라감
        int currentTotal = 0;
        for ( int i = 0; i < recommendDtos.size(); i++ ) {
            currentTotal += recommendDtos.get(i).getCount();
            if( random <= currentTotal && random > recent) {
                categoryNumber = recommendDtos.get(i).getCategoryId();
                break;
            }
            recent = currentTotal;
        }
        ProductDto productDto = productRepository.findRecommend(categoryNumber);
        return productDto;
    }

    // 마감 임박 상품 조회
    public String findEndProduct(@RequestParam String param) {
        return "bb";
    }
    
    // 인기 상품 조회
    public List<ProductDto> findPopularProduct( ) {
        return productRepository.findPopularProduct();
    }
    
    // 새로 등록된 상품 조회
    public List<ProductDto> findNewProduct( ) {
        Pageable pageable = PageRequest.of(0, 4, Sort.by("current_price"));
        return productRepository.findNewProduct(pageable).getContent();
    }

    // 상품 목록 조회
    public Page<ProductDto> findList(
        Pageable pageable,
        String productName, Integer organizationId, String auctionStatus,
        Integer categoryId, Integer lowprice, Integer highprice, String sort
    ) {
        Page<ProductDto> list = productRepository.findProductList(
            productName, organizationId, auctionStatus,
            categoryId, lowprice, highprice, sort, pageable);
        return list;
    }
}
