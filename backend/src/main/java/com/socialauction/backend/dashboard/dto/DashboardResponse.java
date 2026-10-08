package com.socialauction.backend.dashboard.dto;


import java.util.List;

import com.socialauction.backend.payment.dto.PaymentListResponse;
import com.socialauction.backend.products.user.dto.ProductDto;

import lombok.Builder;
import lombok.Data;

@Data @Builder 
public class DashboardResponse {
    
    List<ProductDto> productDtos;

    List<PaymentListResponse> paymentListResponses;

    Integer productCount;
    Integer auctionWatingCount;
    Integer auctionBiddingCount;
    Integer auctionEndCount;
    Integer memberCount;
    Integer OrganizationCount;

}
