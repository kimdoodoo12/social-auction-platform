package com.socialauction.backend.dashboard.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.dashboard.dto.DashboardResponse;
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

    // 대시보드 DTO로 조립하여 반환
    public DashboardResponse findDashboardResponse(){
        List<ProductDto> newProductDtos = findNewProduct();
        List<ProductDto> endProductDtos = findEndProduct();
        List<PaymentListResponse> paymentListResponses = findPaymentListResponses();
        Integer productCount = findProductCount();
        Integer auctionWaitingCount = findAuctionCount("대기");
        Integer auctionBiddingCount = findAuctionCount("진행");
        Integer auctionEndCount = findAuctionCount("완료");
        Integer memberCount = findMemberCount();
        Integer organizationCount = findOrganziationCount();

        return DashboardResponse.builder()
            .newProductDtos(newProductDtos)
            .endProductDtos(endProductDtos)
            .paymentListResponses(paymentListResponses)
            .productCount(productCount)
            .auctionWatingCount(auctionWaitingCount)
            .auctionBiddingCount(auctionBiddingCount)
            .auctionEndCount(auctionEndCount)
            .memberCount(memberCount)
            .OrganizationCount(organizationCount)
            .build();
    }
    

    // 새로등록된 상품은 사용자쪽 상품 쿼리를 재사용
    public List<ProductDto> findNewProduct(){
        Pageable pageable = PageRequest.of(0, 5, Sort.by("created_at").descending());
        return productRepository.findProduct("new", pageable).getContent();
    }
    // 진행중인 경매는 사용자쪽 메인(상품) 쿼리를 재사용
    public List<ProductDto> findEndProduct(){
        Pageable pageable = PageRequest.of(0, 5, Sort.by("end_time").descending());
        return productRepository.findProduct("ending", pageable).getContent();
    }

    // 최근 낙찰만
    public List<PaymentListResponse> findPaymentListResponses(){
        // 동적으로 개수와 정렬을 위한 Pageable 생성
        Pageable pageable = PageRequest.of(0, 5, Sort.by("created_at").descending());

        
        return paymentRepository.findAllPayment(pageable).getContent();
    }

    // 전체상품 카운트
    public Integer findProductCount(){
        return productRepository.findProductCount();
    }

    // 경매대기 카운트 // 진행경매 카운트 // 경매종료 카운트
    public Integer findAuctionCount(String agreementStatus){
        return aRepository.findAuctionCount(agreementStatus);
    }

    // 전체회원 카운트
    public Integer findMemberCount(){
        return mRepository.findMemberCount();
    }

    // 협약중인 기관 카운트
    public Integer findOrganziationCount(){
        return oRepository.findOrganizationCount();
    }
}
