package com.socialauction.backend.auction.user.service;

import org.springframework.stereotype.Service;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.bid.repository.BidRepository;
import com.socialauction.backend.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor 
public class AuctionCloseService {
    
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final BidRepository bidRepository;


    // // * 경매 종료 
    // public boolean closeAucton(Integer auctionId){

    //     // ? 종료 넘길때 스케줄 endtime 일때 넘기나? 그렇게 넘기면 시간 검증 할 필요 있나?


    //     // * 경매 상태 변경
    //     AuctionEntity auctionEntity =  auctionRepository.findById(auctionId).orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));
    //     auctionEntity.setAuctionStatus("완료");

    //     // * 낙찰자 선정
    //     Long userId = bidRepository.findWinningMemberId(auctionId).orElseThrow(() -> new IllegalArgumentException("입찰을 찾을 수 없습니다."));

    //     // * 
    // }
        
        

}
