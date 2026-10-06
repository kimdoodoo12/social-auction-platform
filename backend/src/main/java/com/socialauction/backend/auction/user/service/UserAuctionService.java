package com.socialauction.backend.auction.user.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;
import com.socialauction.backend.auction.user.dto.AuctionDetailProjection;
import com.socialauction.backend.auction.user.dto.AuctionFindDto;
import com.socialauction.backend.bid.repository.BidRepository;
import com.socialauction.backend.products.admin.dto.RecentBid;
import com.socialauction.backend.products.admin.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class UserAuctionService {

    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;

    // * 경매 페이지 정보 - 상품
    public AuctionDetailProjection findUserPageAuction(Integer auctionId){
        AuctionDetailProjection dto =  auctionRepository.UserPageAuctions(auctionId).orElse(null);
    
        return dto;
    }


    // * 경매 페이지 정보 - 최근 5개 입찰 기록
    public List<RecentBid> findUserPageBid(int auctionId){
        
        List<RecentBid> dto = productRepository.findRecentBids(auctionId);
        return dto;
    }




    // *--경매 로직--------------------------------------------------------------------

    // * 경매 마감 시간 확인
    public boolean chekDeadline(LocalDateTime endTime){
        LocalDateTime time = endTime;
        // 현재 시각이 마감시간 보다 이전 시간이면 true
        return LocalDateTime.now().isBefore(time);
        
    }

    public int insertBid(AuctionConfirmDto auctionConfirmDto){
        int insert = bidRepository.insertBid(auctionConfirmDto.getAuctionId(), auctionConfirmDto.getMemberId(), auctionConfirmDto.getPrice());

        return insert;  
    }


    @Transactional 
    public boolean auctionConfirmed(AuctionConfirmDto auctionConfirmDto){
        
    
        // * 경매 마감 시간 확인
        LocalDateTime endtime = auctionRepository.findEndTimeById(auctionConfirmDto.getAuctionId()).orElse(null);
        if(!chekDeadline(endtime)){
            return false;
        }
        
        // * 경매 id에 해당하는 레코드 잠금  
        AuctionEntity auctionEntity = auctionRepository.findByIdForUpdate(auctionConfirmDto.getAuctionId()).orElse(null);

         // * 경매 마감 시간 재 확인 - 잠금되고 대기했다가 마감 시간이 지난 상황 대비
        if(!chekDeadline(auctionEntity.getEndTime())){
            return false;
        }

        // * 현재 최고가보다 최소 1,000원 높아야 한다
        if (auctionConfirmDto.getPrice() - auctionEntity.getTopPrice() < 1000) {
            return false;
        }


        // * 레코드 추가된 개수로 반환
        int count = insertBid(auctionConfirmDto);
        
        // * 추가된 레코드 1개가 아니면
        if(count != 1 ){
            return false;
        }

        // * 경매 현재가 가격 변경
        auctionEntity.setTopPrice(auctionConfirmDto.getPrice());

        return  true;

    }

    
}   
