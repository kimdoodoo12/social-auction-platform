package com.socialauction.backend.global.sse;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;
import com.socialauction.backend.auction.user.dto.AuctionDetailProjection;
import com.socialauction.backend.auction.user.service.UserAuctionService;

import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor 
public class AuctionEventListener {
    
    private final AuctionSseService auctionSseService;
    private final UserAuctionService userAuctionService;

    // DB 커밋이 완료된 직후 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidSuccess(AuctionConfirmDto bid) {
        // 리액트로 보낼 데이터 (Map이나 DTO 사용)
         Integer auctionId = bid.getAuctionId();

        AuctionDetailProjection auction =
                userAuctionService.findUserPageAuction(auctionId);

        Map<String, Object> data = new HashMap<>();
        data.put("price", auction.getTopPrice());
        data.put("bidderName", bid.getMemberId());
        data.put("bidCount", auction.getBidCount());
        data.put("bidMemberCount", auction.getBidMemberCount());
        data.put("status", auction.getStatus());
        data.put("endTime", auction.getEndTime());
        data.put("recentBids", userAuctionService.findUserPageBid(auctionId));

        auctionSseService.broadcastPrice(auctionId, data);
    }
}
