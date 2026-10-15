package com.socialauction.backend.bid.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
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
import com.socialauction.backend.global.PageResponse;
import com.socialauction.backend.global.encryption.AESdecryption;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class BidService {
    private final BidRepository bidRepository;

    @Value  ("${aes.secret}") 
    private String key;

    //입찰 기록 전체 조회
    @Transactional (readOnly = true)
    public PageResponse<BidDto> bidFindAll(Integer auctionId, int page, int size){
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("bidTime").descending()
        );

        Page<BidEntity> bids = bidRepository.findByAuctionEntity_AuctionId(auctionId, pageable);

            
        return new PageResponse<>(bids.map(bid -> {
        BidDto dto = BidDto.from(bid);
        if (dto.getName() != null) {
            dto.setName(AESdecryption.AESDecryption(dto.getName(), key));
        }
        return dto;
        }));
    }
    //최종 낙찰 결과 
    //! 조회 쿼리 추후에 바꿀 예정 
    @Transactional (readOnly = true)
    public BidResultDto bidResultFind(Integer auctionId){
        BidResultDto bidDto = bidRepository.findResultByid(auctionId).orElse(null);
        bidDto.setName(AESdecryption.AESDecryption(bidDto.getName(), key));
        
        
        return bidDto;
    }
}
