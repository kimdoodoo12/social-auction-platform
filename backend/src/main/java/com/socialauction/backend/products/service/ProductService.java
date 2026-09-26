package com.socialauction.backend.products.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.products.dto.ProductAuctionSummary;
import com.socialauction.backend.products.dto.ProductListResponse;
import com.socialauction.backend.products.entity.ProductEntity;
import com.socialauction.backend.products.repository.ImageRepository;
import com.socialauction.backend.products.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;

    // 상품 첫 화면 조회
    @Transactional(readOnly = true)
    public Page<ProductListResponse> findAll(Pageable pageable) {
        // 전체 가져오기
        Page<ProductEntity> products = productRepository.findAll(pageable);
        // 비어있으면 빈 배열 반환
        if (products.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, products.getTotalElements());
        }

        // productId만 저장하는 배열
        List<Integer> productIds = new ArrayList<>();
        products.getContent().forEach(product -> {
            productIds.add(product.getProductId());
        });

        // productId와 image로 맵 구성
        Map<Integer, String> images = new HashMap<>();
        imageRepository.findRepresentImg(productIds)
                .forEach(image -> {
                    images.put(
                            image.getProductEntity().getProductId(),
                            image.getImage()
                    );
                });
        
        // 경매(상태, 현재가) 정보 가져오기 
        Map<Integer, ProductAuctionSummary> auctions = new HashMap<>();
        productRepository.findAuctionSummaries(productIds).forEach(summary -> {
            auctions.put(summary.getProductId(), summary);
        });


        List<ProductListResponse> responses = new ArrayList<>();
        // page정보 빼고 product정보만 꺼내오는거
        products.getContent().forEach(product -> {
            // id와 맞는 auction 정보
            ProductAuctionSummary auction = auctions.get(product.getProductId());
            // 검증 후 현재가 or 시작가 반환
            Integer currentPrice = auction != null && auction.getCurrentPrice() != null
                    ? auction.getCurrentPrice() : product.getStartPrice();
            // 검증 후 현재상태 or "경매대기" 반환
            String status = auction != null ? auction.getStatus() : "경매 대기";
            // 각각 넣어서 배열에 추가
            responses.add(ProductListResponse.from(
                    product, images.get(product.getProductId()), currentPrice, status));
        });
        // 추가한 배열들 반환
        return new PageImpl<>(responses, pageable, products.getTotalElements());
    }
}
