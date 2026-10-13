package com.socialauction.backend.auction.user.service;

import com.socialauction.backend.interest.entity.InterestEntity;
import com.socialauction.backend.interest.repository.InterestRepository;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.text.html.parser.Entity;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.repository.AuctionRepository;
import com.socialauction.backend.auction.scheduler.AuctionScheduler;
import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;
import com.socialauction.backend.auction.user.dto.AuctionDetailProjection;
import com.socialauction.backend.auction.user.dto.AuctionFindDto;
import com.socialauction.backend.auction.user.dto.ProductDetailProjection;
import com.socialauction.backend.bid.repository.BidRepository;
import com.socialauction.backend.interest.service.InterestService;
import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.member.repository.MemberRepository;
import com.socialauction.backend.products.admin.dto.RecentBid;
import com.socialauction.backend.products.admin.repository.ProductRepository;
import com.socialauction.backend.products.entity.ProductEntity;
import com.socialauction.backend.products.user.dto.ProductDto;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class UserAuctionService {

    private final InterestRepository interestRepository;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final BidRepository bidRepository;
    private final AuctionScheduler auctionScheduler;
    private final ApplicationEventPublisher eventPublisher;
    private final MemberRepository memberRepository;
    private final InterestService interestService;

    public List<String> findProductImages(Integer auctionId) {
        return auctionRepository.findProductImages(auctionId);
    }

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



    // * 경매 입찰 
    @Transactional 
    public boolean auctionConfirmed(AuctionConfirmDto auctionConfirmDto, AuctionEntity auctionEntity){
        
    
        // * 경매 마감 시간 확인
        // LocalDateTime endtime = auctionRepository.findEndTimeById(auctionConfirmDto.getAuctionId()).orElse(null);
        // if(!chekDeadline(endtime)){
        //     return false;
        // }
        
        // * 경매 id에 해당하는 레코드 잠금  
        // AuctionEntity auctionEntity = auctionRepository.findByIdForUpdate(auctionConfirmDto.getAuctionId()).orElse(null);

        // * 경매 마감 시간 재 확인 - 잠금되고 대기했다가 마감 시간이 지난 상황 대비
        if(!chekDeadline(auctionEntity.getEndTime())){
            return false;
        }


        if (auctionEntity.getTopPrice() == null) {
            // 첫 입찰: 상품 시작가 이상인지 확인
            if (auctionConfirmDto.getPrice() < auctionEntity.getProductEntity().getStartPrice()) {
                return false;
            }
        } else {
            // 이후 입찰: 현재 최고가 + 호가 단위(1,000원) 이상인지 확인
            if (auctionConfirmDto.getPrice() - auctionEntity.getTopPrice() < 1000) {
                return false;
            }
        }

        // // * 현재 최고가보다 최소 1,000원 높아야 한다
        // if (auctionConfirmDto.getPrice() - auctionEntity.getTopPrice() < 1000) {
        //     return false;
        // }


        // * 레코드 추가된 개수로 반환
        int count = insertBid(auctionConfirmDto);
        
        // * 추가된 레코드 1개가 아니면
        if(count != 1 ){
            return false;
        }

        // * 경매 현재가 가격 변경
        Long memberId = auctionConfirmDto.getMemberId();
        Integer productId = auctionEntity.getProductEntity().getProductId();

        InterestEntity interestEntity = interestRepository
                .findByMemberEntity_MemberIdAndProductEntity_ProductId(memberId, productId)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        if (interestEntity != null) {
            // 2-A. 기존 관심 기록이 있는 경우: 점수 +2 증가
            interestEntity.setInterestScore(interestEntity.getInterestScore() + 2.0);
            interestEntity.setLastViewedAt(now);
        } else {
            // 2-B. 관심 기록이 없던 경우: 새 관심 엔티티 생성 후 저장
            interestEntity = InterestEntity.builder()
                    .memberEntity(memberRepository.findById(memberId).orElse(null))// 입찰한 MemberEntity 객체
                    .productEntity(auctionEntity.getProductEntity())
                    .interestScore(7.0) // 조회 5점 + 입찰 가산 2점 = 7점
                    .viewCount(1)
                    .lastViewedAt(now)
                    .build();
            
            interestRepository.save(interestEntity);
        }

        return  true;

    }

    

    // * 경매 첫 입찰
    @Transactional 
    public boolean firstBid(AuctionConfirmDto auctionConfirmDto, AuctionEntity auctionEntity){

        
        // * 대기상태이면 진행으로 변경 아니면 false
        if(auctionEntity.getAuctionStatus().equals("대기")){
            auctionEntity.setAuctionStatus("진행");
        }else{
            return false;
        }

        // * 시작 시간, 종료 시간 지정
        auctionEntity.setStartTime(LocalDateTime.now());
        //! 나중에 시간 바꾸기
        auctionEntity.setEndTime(LocalDateTime.now().plusMinutes(3));

        // * 스케줄러 등록
        auctionScheduler.scheduleEnd(auctionConfirmDto.getAuctionId(), auctionEntity.getEndTime());
        
        // * 입찰 메서드
        boolean result = auctionConfirmed(auctionConfirmDto,auctionEntity);

        if (result == true) {
            return true;
        }
        return false;
    }

    // // ! 탈란드 해봐야함, orElse로만 처리하는게 맞나
    // // * 경매 상품,기관 상세정보
    // public ProductDetailProjection findDetailProduct(Integer auctionId){
    //     return auctionRepository.findProductDetail(auctionId).orElse(null);
    // } 


    // * 첫 입찰, 경매중 입찰 한곳에서 처리
    //! 사용종료
    // public boolean bidAction(AuctionConfirmDto auctionConfirmDto){
    //     AuctionEntity auctionEntity = auctionRepository.findById(auctionConfirmDto.getAuctionId()).orElseThrow();
        
    //     if(auctionEntity.getAuctionStatus().equals("대기")){
    //         return firstBid(auctionConfirmDto);
    //     }else if (auctionEntity.getAuctionStatus().equals("진행")) {
    //         return auctionConfirmed(auctionConfirmDto);
    //     }else{
    //         return false;
    //     }

    // }

    // * placeBid 입찰을 하고 성공하면
    // * 이벤트 발행 후 DB 커밋 
    // * AuctionEventListener 실행
    // * 해당 경매 구독자들에게 priceUpdate 전송 → 브라우저가 받아 화면 갱신
     
    @Transactional 
    public Boolean placeBid(AuctionConfirmDto auctionConfirmDto) {

        AuctionEntity auctionEntity = auctionRepository.findByIdForUpdate(auctionConfirmDto.getAuctionId())
            .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));

        boolean result = false;

        // 2. 상태에 따라 바로 분기
        if ("대기".equals(auctionEntity.getAuctionStatus())) {
            result = firstBid(auctionConfirmDto, auctionEntity);
        } else if ("진행".equals(auctionEntity.getAuctionStatus())) {
            result = auctionConfirmed(auctionConfirmDto, auctionEntity);
        }
        
        //입찰 성공시 
        if (result==true) {
            eventPublisher.publishEvent(new AuctionConfirmDto(
            auctionConfirmDto.getAuctionId(),
            auctionConfirmDto.getPrice(),
            auctionConfirmDto.getMemberId()
            ));
            return true;
        }

        return false;
        
       
    }
    // * 경매 상품,기관 상세정보
    public ProductDetailProjection findDetailProduct(
            Integer auctionId,
            Long memberId) {

        ProductDetailProjection detail = auctionRepository
            .findProductDetail(auctionId)
            .orElse(null);

        // 상세 정보가 있고 로그인한 회원이면 관심 기록 저장
        if (detail != null && memberId != null) {
            MemberEntity memberEntity =
                memberRepository.getReferenceById(memberId);

            ProductEntity productEntity =
                productRepository.getReferenceById(detail.getProductId());

            interestService.recordView(memberEntity, productEntity);
        }

        return detail;
    }


    // 연관된 상품 조회
    public List<ProductDto> findRelatedProducts(Integer productId){
        ProductEntity productEntity = productRepository.findById(productId).orElse(null);
        Integer organizationId = productEntity.getOrganizationEntity().getOrganizationId();
        Integer categoryId = productEntity.getCategoryEntity().getCategoryId();
        return productRepository.findRelatedProduct(productId, organizationId, categoryId);
    }

    
}   
