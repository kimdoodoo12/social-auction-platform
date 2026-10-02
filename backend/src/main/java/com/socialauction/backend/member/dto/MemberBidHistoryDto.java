package com.socialauction.backend.member.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringExclude;

import com.socialauction.backend.bid.dto.BidDto;
import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.payment.dto.PaymentDto;
import com.socialauction.backend.payment.entity.PaymentEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class MemberBidHistoryDto {
    private Integer memberId;
    private String loginId;
    private String name;
    private String email;
    
    // 가입일
    private LocalDateTime createdAt;

    // 입찰횟수 , 역참조한 입찰기록 테이블에서 회원 번호로 몇개인지 
    private Integer bcount;

    // 낙찰횟수 , 결제 테이블에서 결제 상태가 1이고 회원번호동일한거 가져옴
    private Integer pcount;

    private String role;
    private LocalDateTime lockedAt;

    // 미낙찰 번호
    private Integer notpay;

    // 입찰내역 전체 조회 
    // @Builder .Default
    // // @ToString .Exclude
    // public  List<BidDto> bidDtos = new ArrayList<>();

    @Builder .Default
    @ToString .Exclude
    public  List<MBResultDto> payDtos = new ArrayList<>();

    @Builder .Default
    @ToString.Exclude
    private  List<MemberBhistory> bidHistory = new ArrayList<>();


    // entity -> dto 
    public static  MemberBidHistoryDto from(MemberEntity memberEntity){
        return MemberBidHistoryDto.builder()
            .memberId(memberEntity.getMemberId())
            .loginId(memberEntity.getLoginId())
            .name(memberEntity.getName())
            .email(memberEntity.getEmail())
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
