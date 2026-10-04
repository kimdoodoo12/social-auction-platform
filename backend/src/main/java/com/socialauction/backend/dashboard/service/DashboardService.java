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

    

    
}
