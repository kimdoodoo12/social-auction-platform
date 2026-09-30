package com.socialauction.backend.auction.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.auction.dto.AuctionDetailDto;
import com.socialauction.backend.auction.dto.AuctionFindAllDto;
import com.socialauction.backend.auction.dto.AuctionSearchDto;
import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.products.admin.dto.ImageDto;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 

public class AuctionService {
    
    private final AuctionRepository auctionRepository;

    // 경매 전체 조회 기능 , 페이징
    @Transactional (readOnly = true)
    public Page<AuctionFindAllDto> auctionFindAll(int page, int size){
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("auctionId").ascending()
        );

        Page<AuctionEntity> auctionEntities = auctionRepository.findAll(pageable);


        Page<AuctionFindAllDto> auctiomDto = auctionEntities.map((entity) -> { 
            AuctionFindAllDto dto;
            dto = AuctionFindAllDto.from(entity);

            Integer topPrice = auctionRepository.findTopPriceByAuctionId(entity.getAuctionId()); 
            dto.setTopPrice(topPrice != null ? topPrice : 0);

            Integer bidCount = auctionRepository.countBidsByAuctionId(entity.getAuctionId());
            
            dto.setBidCount(bidCount);
            
            
            return dto;
        
        } );

        return auctiomDto;
        
    }

    //경매 상세 조회
    @Transactional (readOnly = true)
    public AuctionDetailDto auctionDetailFind(Integer auctionId){
        AuctionEntity auctioneEntity = auctionRepository.findById(auctionId).orElse(null);
        
        AuctionDetailDto auctionDetailDto = AuctionDetailDto.from(auctioneEntity);
        
        Integer topPrice = auctionRepository.findTopPriceByAuctionId(auctioneEntity.getAuctionId()); 
        auctionDetailDto.getAuctionFindAllDto().setTopPrice(topPrice != null ? topPrice : 0);

        Integer bidCount = auctionRepository.countBidsByAuctionId(auctioneEntity.getAuctionId());
            
        auctionDetailDto.getAuctionFindAllDto().setBidCount(bidCount);


        //이미지 꺼내기
        List<ImageDto> imageDtos = auctioneEntity.getProductEntity().getImageEntity().stream().map(ImageDto::from).toList();
        //이미지 저장
        auctionDetailDto.setImageList(imageDtos);

        // 최고가 입찰 쿼리문으로 가져오기
        String userName = auctionRepository.findTopBidNameByAuctionId(auctionId).orElse(null);
        auctionDetailDto.setUserName(userName);

        return auctionDetailDto;
    }

    public Page<AuctionFindAllDto> auctionSearch(AuctionSearchDto auctionSearchDto, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<AuctionEntity> result =  auctionRepository.searchAuctions(auctionSearchDto.getKeyword(), auctionSearchDto.getStatus(), auctionSearchDto.getOrganization(), auctionSearchDto.getStartDate(), auctionSearchDto.getEndDate(), pageable);
        
        return result.map(entity -> {
        // (1) 현재 순회 중인 entity를 기본 DTO로 변환
        AuctionFindAllDto dto = AuctionFindAllDto.from(entity);

        // (2) '현재 entity'의 auctionId를 꺼내서 최고가 조회 후 DTO에 세팅
        Integer topPrice = auctionRepository.findTopPriceByAuctionId(entity.getAuctionId()); 
        dto.setTopPrice(topPrice != null ? topPrice : 0);

        // (3) '현재 entity'의 auctionId를 꺼내서 입찰 수 조회 후 DTO에 세팅
        Integer bidCount = auctionRepository.countBidsByAuctionId(entity.getAuctionId());
        dto.setBidCount(bidCount != null ? bidCount : 0);

        // (4) 완성된 dto 반환 
        return dto;
    });

    
    }
}
