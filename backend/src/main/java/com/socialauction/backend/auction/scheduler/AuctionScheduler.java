package com.socialauction.backend.auction.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.socialauction.backend.auction.queue.AuctionQueue;
import com.socialauction.backend.auction.user.service.ClosingAuctionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class AuctionScheduler {
    private final TaskScheduler taskScheduler;
    private final ClosingAuctionService closingAuctionService;
    private final AuctionQueue auctionQueue;

    public void scheduleEnd(int auctionId, LocalDateTime endTime){
        Instant instant = endTime.atZone(ZoneId.systemDefault()).toInstant();
        
        taskScheduler.schedule(() -> auctionQueue.submitClose(auctionId), instant);
    }
}
