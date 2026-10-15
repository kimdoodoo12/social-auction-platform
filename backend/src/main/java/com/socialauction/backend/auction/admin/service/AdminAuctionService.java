package com.socialauction.backend.auction.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.auction.admin.dto.AuctionDetailDto;
import com.socialauction.backend.auction.admin.dto.AuctionFindAllDto;
import com.socialauction.backend.auction.admin.dto.AuctionSearchDto;
import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.auction.repository.AuctionRepository.AdminBidStatsProjection;
import com.socialauction.backend.global.PageResponse;
import com.socialauction.backend.products.admin.dto.ImageDto;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AdminAuctionService {
    
    private final AuctionRepository auctionRepository;

    @Transactional(readOnly = true)
    public PageResponse<AuctionFindAllDto> auctionFindAll(int page, int size) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("auctionId").ascending()
        );
        // 검색 조건에 모두 null을 넘겨 searchAuctions 한 방 쿼리를 그대로 재사용
        return new PageResponse<>(auctionRepository.searchAuctions(null, null, null, null, null, pageable));
    }

    //* */ 2.  경매 상세 조회 (기존 8번 쿼리 -> 단 2번 쿼리로 해결!)
    @Transactional(readOnly = true)
    public AuctionDetailDto auctionDetailFind(Integer auctionId) {
        // [쿼리 1] Auction + Product + Category + Organization + Image를 FETCH JOIN으로 한 번에 조회
        AuctionEntity auctionEntity = auctionRepository.findDetailById(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 경매를 찾을 수 없습니다. id=" + auctionId));

        // 이미 메모리에 다 올라와 있으므로 추가 쿼리(LAZY 로딩)가 전혀 발생하지 않음
        AuctionDetailDto auctionDetailDto = AuctionDetailDto.from(auctionEntity);
        List<ImageDto> imageDtos = auctionEntity.getProductEntity().getImageEntity()
            .stream()
            .map(ImageDto::from)
            .toList();
        auctionDetailDto.setImageList(imageDtos);

        // [쿼리 2] 최고가, 입찰수, 참여자수, 최고입찰자 이름을 단 1번의 쿼리로 조회
        AdminBidStatsProjection stats = auctionRepository.findBidStatsByAuctionId(auctionId);
        auctionDetailDto.getAuctionFindAllDto().setTopPrice(stats.getTopPrice() != null ? stats.getTopPrice() : 0);
        auctionDetailDto.getAuctionFindAllDto().setBidCount(stats.getBidCount() != null ? stats.getBidCount() : 0);
        auctionDetailDto.setUserName(stats.getTopBidUserName());
        auctionDetailDto.setCountUser(stats.getCountUser() != null ? stats.getCountUser() : 0);

        return auctionDetailDto;
    }

    //* */ 3. 경매 검색 기능 (기존 18번 쿼리 -> 단 1번 쿼리로 해결!)
    @Transactional(readOnly = true)
    public PageResponse<AuctionFindAllDto> auctionSearch(AuctionSearchDto auctionSearchDto, int page, int size) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("auctionId").ascending()
        );

        return new PageResponse<>(auctionRepository.searchAuctions(
            auctionSearchDto.getKeyword(),
            auctionSearchDto.getStatus(),
            auctionSearchDto.getOrganization(),
            auctionSearchDto.getStartDate(),
            auctionSearchDto.getEndDate(),
            pageable
        ));
    }

    
}
