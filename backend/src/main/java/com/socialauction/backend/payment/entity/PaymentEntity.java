package com.socialauction.backend.payment.entity;

import com.socialauction.backend.member.entity.MemberEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "payment")
@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class PaymentEntity {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer paymentId;


    private int paymentPrice;
    private boolean paymentStatus;

    

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "memberId")
    private MemberEntity memberEntity;
    

}
