package com.socialauction.backend.bid.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.socialauction.backend.bid.dto.BidDto;
import com.socialauction.backend.bid.dto.BidResultDto;
import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.bid.repository.BidRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@Transactional 
@RequiredArgsConstructor 
public class BidService {
    private final BidRepository bidRepository;

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

    //조회 기준을 아직 정확히 안함
    public BidResultDto bidResultFind(Integer auctionId){
        BidResultDto bidDto = bidRepository.findResultByid(auctionId).orElse(null);
        
        
        return bidDto;
    }
}
