package com.socialauction.backend.global.sse;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;

import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor 
public class AuctionEventListener {
    
    private final AuctionSseService auctionSseService;

    // DB 커밋이 완료된 직후 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidSuccess(AuctionConfirmDto auctionConfirmDto) {
        // 리액트로 보낼 데이터 (Map이나 DTO 사용)
        Map<String, Object> data = Map.of(
            "price", auctionConfirmDto.getPrice(),
            "bidderName", auctionConfirmDto.getMemberId()
        );

        auctionSseService.broadcastPrice(auctionConfirmDto.getAuctionId(), data);
    }
}
