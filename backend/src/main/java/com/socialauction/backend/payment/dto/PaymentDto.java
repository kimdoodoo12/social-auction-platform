package com.socialauction.backend.payment.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.payment.entity.PaymentEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class PaymentDto {

    private Integer paymentId;
    private int paymentPrice;
    private boolean paymentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 경매번호 (FK)
    private Integer auctionId;

    // 회원번호 (FK)
    private Long memberId;

    // Entity ->DTO 
    public static PaymentDto from(PaymentEntity paymentEntity){
        return PaymentDto.builder()
            .paymentId(paymentEntity.getPaymentId())
            .paymentPrice(paymentEntity.getPaymentPrice())
            .paymentStatus(paymentEntity.isPaymentStatus())
            .createdAt(paymentEntity.getCreatedAt())
            .updatedAt(paymentEntity.getUpdatedAt())
            .auctionId(paymentEntity.getAuctionEntity().getAuctionId())
            .memberId(paymentEntity.getMemberEntity().getMemberId())
            .build();
    }

    public PaymentEntity toEntity(){
        return PaymentEntity.builder()
                    .paymentPrice(this.paymentPrice)
                    .paymentStatus(this.paymentStatus)
                    .build();

    }

}

