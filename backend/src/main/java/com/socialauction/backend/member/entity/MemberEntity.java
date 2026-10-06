package com.socialauction.backend.member.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.global.BaseTime;
import com.socialauction.backend.payment.entity.PaymentEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity 
@Table (name = "member")
@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class MemberEntity extends BaseTime{
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long memberId;

    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String role;

    private LocalDateTime lockedAt;
    
    // 관리자 인지 회원인지 
    @Builder.Default
    @Column(nullable = false)
    private boolean status = false;

    // 입찰기록 테이블 역참조
    @OneToMany (mappedBy = "memberEntity" , fetch = FetchType.LAZY)
    @Builder .Default
    @ToString .Exclude  
    List<BidEntity> bidEntities = new ArrayList<>();

    // 결제 테이블 역참조
    @OneToMany (mappedBy = "memberEntity" , fetch = FetchType.LAZY)
    @Builder .Default
    @ToString .Exclude  
    List<PaymentEntity> paymentEntities = new ArrayList<>();


}
