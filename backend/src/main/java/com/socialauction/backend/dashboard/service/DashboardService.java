package com.socialauction.backend.dashboard.service;

import org.springframework.stereotype.Service;

import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.member.repository.MemberRepository;
import com.socialauction.backend.organization.repository.OrganizationRepository;
import com.socialauction.backend.products.admin.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor  
public class DashboardService {
    private final ProductRepository pRepository;
    private final AuctionRepository aRepository;
    private final MemberRepository mRepository;
    private final OrganizationRepository oRepository;

    

    // 새로등록된 상품은 사용자쪽 상품 쿼리를 재사용


    // 전체상품 카운트

    // 경매대기 카운트

    // 진행경매 카운트

    // 경매종료 카운트

    // 전체회원 카운트

    // 협약중인 기관 카운트

    // 진행중인 경매는

    // 최근 입찰만 

}
