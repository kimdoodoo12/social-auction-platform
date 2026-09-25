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
        Page<ProductEntity> products = productRepository.findAll(pageable);
        if (products.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, products.getTotalElements());
        }

        List<Integer> productIds = new ArrayList<>();
        products.getContent().forEach(product -> {
            productIds.add(product.getProductId());
        });

        Map<Integer, String> images = new HashMap<>();
        imageRepository.findRepresentImg(productIds)
                .forEach(image -> {
                    images.put(
                            image.getProductEntity().getProductId(),
                            image.getImage()
                    );
                });

        Map<Integer, ProductAuctionSummary> auctions = new HashMap<>();
        productRepository.findAuctionSummaries(productIds).forEach(summary -> {
            auctions.put(summary.getProductId(), summary);
        });

        List<ProductListResponse> responses = new ArrayList<>();
        products.getContent().forEach(product -> {
            ProductAuctionSummary auction = auctions.get(product.getProductId());
            Integer currentPrice = auction != null && auction.getCurrentPrice() != null
                    ? auction.getCurrentPrice() : product.getStartPrice();
            String status = auction != null ? auction.getStatus() : "경매 대기";

            responses.add(ProductListResponse.from(
                    product, images.get(product.getProductId()), currentPrice, status));
        });

        return new PageImpl<>(responses, pageable, products.getTotalElements());
    }
}
