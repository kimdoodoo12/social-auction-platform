package com.socialauction.backend.member.admin.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.member.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class MemberDto {
    private Long memberId;
    private String loginId;
    private String name;
    private String email;
    private String phone;
    
    // 가입일
    private LocalDateTime createdAt;

    // 입찰횟수 , 역참조한 입찰기록 테이블에서 회원 번호로 몇개인지 
    private Integer bcount;

    // 낙찰횟수 , 결제 테이블에서 결제 상태가 1이고 회원번호동일한거 가져옴
    private Integer pcount;

    private String role;
    private LocalDateTime lockedAt;


    public MemberEntity toEntity(){
        return MemberEntity.builder()
                        .loginId(this.loginId)
                        .name(this.name)
                        .email(this.email)
                        .phone(this.phone)
                        .build();
    }

    // entity -> dto 
    public static  MemberDto from(MemberEntity memberEntity){
        return MemberDto.builder()
            .memberId(memberEntity.getMemberId())
            .loginId(memberEntity.getLoginId())
            .name(memberEntity.getName())
            .email(memberEntity.getEmail())
            .phone(memberEntity.getPhone())
            .createdAt(memberEntity.getCreatedAt())
            .role(memberEntity.getRole())
            .lockedAt(memberEntity.getLockedAt())

            // 입찰횟수  
            .bcount(memberEntity.getBidEntities().size())

            // 낙찰횟수 
            .pcount((int) memberEntity.getPaymentEntities().stream()
            .filter(paymentEntity -> paymentEntity.isPaymentStatus()) 
            .count())

            .build();
    }


}
