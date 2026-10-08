package com.socialauction.backend.auction.user.service;

import org.springframework.stereotype.Service;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.bid.repository.BidRepository;
import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.member.repository.MemberRepository;
import com.socialauction.backend.payment.dto.PaymentDto;
import com.socialauction.backend.payment.entity.PaymentEntity;
import com.socialauction.backend.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ClosingAuctionService {

    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;


    // * 결제 생성 메서드
    public boolean createPayment(PaymentDto paymentDto ){

        // * 받은 DTO 엔티티로 변환 
        PaymentEntity paymentEntity = paymentDto.toEntity();

        // * 엔티티에 유저 ID 넣기  .getReferenceById가 ID 값만 품고 있는 (프록시) 객체를 만듬, DB 조회x
        MemberEntity memberProxy = memberRepository.getReferenceById(paymentDto.getMemberId());
        paymentEntity.setMemberEntity(memberProxy);

        AuctionEntity auctionEntity = auctionRepository.getReferenceById(paymentDto.getAuctionId());
        paymentEntity.setAuctionEntity(auctionEntity);

        PaymentEntity savedEntity = paymentRepository.save(paymentEntity);
        
        if(savedEntity.getPaymentId()>=1){
            return true;
        }
        
        return false;
    }

    // * 경매 종료 
    public void closeAuction(int auctionId){



        // * 경매 상태 변경
        AuctionEntity auctionEntity =  auctionRepository.findById(auctionId).orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
        auctionEntity.setAuctionStatus("완료");

        // * 낙찰자 선정
        Long userId = bidRepository.findWinningMemberId(auctionId).orElseThrow(() -> new IllegalArgumentException("입찰을 찾을 수 없습니다."));
        Integer price = auctionEntity.getTopPrice();
        
        // *결제 레코드 생성
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setAuctionId(auctionId); paymentDto.setMemberId(userId); paymentDto.setPaymentPrice(price);
        paymentDto.setPaymentStatus(false);
        
        boolean result = createPayment(paymentDto);

        if (result == true) {
            return ;
        }
        

    }
}
