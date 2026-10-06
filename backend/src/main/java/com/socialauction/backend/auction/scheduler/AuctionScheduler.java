package com.socialauction.backend.auction.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.socialauction.backend.auction.user.service.ClosingAuctionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class AuctionScheduler {
    private final TaskScheduler taskScheduler;
    private final ClosingAuctionService closingAuctionService;

    public void scheduleEnd(int auctionId, LocalDateTime endTime){
        Instant instant = endTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        
        taskScheduler.schedule(() -> closingAuctionService.closeAuction(auctionId), instant);
    }
}
