package com.socialauction.backend.auction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.socialauction.backend.auction.dto.AuctionDto;
import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
@Transactional 
public class AuctionService {
    
    private final AuctionRepository auctionRepository;

    public Page<AuctionDto> auctionFindAll(int page, int size){
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("auctionId").ascending()
        );

        Page<AuctionEntity> auctionEntities = auctionRepository.findAll(pageable);

        

        Page<AuctionDto> auctiomDto = auctionEntities.map((entity) -> { 
            AuctionDto dto;
            dto = AuctionDto.from(entity);

            Integer topPrice = auctionRepository.findTopPriceByAuctionId(entity.getAuctionId()); 
            dto.setTopPrice(topPrice != null ? topPrice : 0);

            Integer bidCount = auctionRepository.countBidsByAuctionId(entity.getAuctionId());
            
            dto.setBidCount(bidCount);
            
            
            return dto;
        
        } );

        return auctiomDto;
        
    }
}
