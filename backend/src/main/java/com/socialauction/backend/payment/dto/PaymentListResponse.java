package com.socialauction.backend.payment.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data @Builder 
public class PaymentListResponse {
    
    Integer auctionId;

    String productName;

    String organizationName;

    String memeberName;

    Integer paymentPrice;

    LocalDateTime createdAt;

    boolean paymentStatus;
}
