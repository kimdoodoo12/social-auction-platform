package com.socialauction.backend.dashboard.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.member.repository.MemberRepository;
import com.socialauction.backend.organization.repository.OrganizationRepository;
import com.socialauction.backend.payment.dto.PaymentListResponse;
import com.socialauction.backend.payment.repository.PaymentRepository;
import com.socialauction.backend.products.user.dto.ProductDto;
import com.socialauction.backend.products.user.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor  
public class DashboardService {
    private final ProductRepository productRepository;
    private final AuctionRepository aRepository;
    private final MemberRepository mRepository;
    private final OrganizationRepository oRepository;
    private final PaymentRepository paymentRepository;

    

    // 새로등록된 상품은 사용자쪽 상품 쿼리를 재사용
    List<ProductDto> findNewProduct(){
        Pageable pageable = PageRequest.of(0, 5, Sort.by("created_at").descending());
        return productRepository.findProduct("new", pageable).getContent();
    }

    // 최근 낙찰만
    List<PaymentListResponse> findPaymentListResponses(){
        // 동적으로 개수와 정렬을 위한 Pageable 생성
        Pageable pageable = PageRequest.of(0, 5, Sort.by("created_at").descending());

        
        return paymentRepository.findAllPayment(pageable).getContent();
    }

    // 진행중인 경매는 사용자쪽 메인(상품) 쿼리를 재사용

    // 전체상품 카운트
    

    // 경매대기 카운트

    // 진행경매 카운트

    // 경매종료 카운트

    // 전체회원 카운트

    // 협약중인 기관 카운트

}
