package com.socialauction.backend.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.bid.dto.BidDto;
import com.socialauction.backend.bid.dto.BidResultDto;
import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.bid.repository.BidRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class BidService {
    private final BidRepository bidRepository;

    //입찰 기록 전체 조회
    @Transactional (readOnly = true)
    public Page<BidDto> bidFindAll(Integer auctionId, int page, int size){
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("bidTime").descending()
        );

        Page<BidEntity> bids =
            bidRepository.findByAuctionEntity_AuctionId(auctionId, pageable);

            
            
        return bids.map(BidDto::from);
    }
    //최종 낙찰 결과 
    //조회 쿼리 추후에 바꿀 예정 
    @Transactional (readOnly = true)
    public BidResultDto bidResultFind(Integer auctionId){
        BidResultDto bidDto = bidRepository.findResultByid(auctionId).orElse(null);
        
        
        return bidDto;
    }
}
