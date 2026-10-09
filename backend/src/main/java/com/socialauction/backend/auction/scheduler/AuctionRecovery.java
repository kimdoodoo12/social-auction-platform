package com.socialauction.backend.auction.scheduler;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.auction.user.service.ClosingAuctionService;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class AuctionRecovery {
    private final AuctionRepository auctionRepository;
    private final AuctionScheduler auctionScheduler;
    private final ClosingAuctionService closingAuctionService;

    @EventListener(ApplicationReadyEvent.class)
    public void recover(){

        // 진행중인 경매만 갖고오기
        List<AuctionEntity> auctions = auctionRepository.findAllBiddingAuction();

        // 모든 진행상태인 경매를 갖고와서 반복문
        for (AuctionEntity auction: auctions){
            // 만약 그 경매가 이미 시간이 지났다면
            if(auction.getEndTime().isAfter(LocalDateTime.now()) || auction.getEndTime().isEqual(LocalDateTime.now())){
                closingAuctionService.closeAuction(auction.getAuctionId());
                continue;
            }
            auctionScheduler.scheduleEnd(auction.getAuctionId(), auction.getEndTime());
            
        }
    }
}
