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
        AuctionDetailProjection dto =  auctionRepository.UserPageAuctions(auctionId)
        .orElseThrow(() -> new IllegalArgumentException("해당 경매를 찾을 수 없습니다."));
    

        // ! 주의: UserPageAuctions 쿼리에 COUNT()가 들어있어서, 없는 경매 번호를 조회해도
        // 빈 객체가 아니라 내용물(productName 등)이 전부 null인 줄이 반환
        if (dto.getProductName() == null) {
            throw new IllegalArgumentException("해당 경매를 찾을 수 없습니다.");
        }

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
        
    
        
        // * 경매 id에 해당하는 레코드 잠금  
        // AuctionEntity auctionEntity = auctionRepository.findByIdForUpdate(auctionConfirmDto.getAuctionId()).orElse(null);

        // * 경매 마감 시간 재 확인 - 잠금되고 대기했다가 마감 시간이 지난 상황 대비
        if (!auctionEntity.isWaiting()
                && (auctionEntity.getEndTime() == null || !chekDeadline(auctionEntity.getEndTime()))) {
            throw new IllegalStateException("경매 마감 시간이 지났습니다.");
        }

        // * 대기 경매의 topPrice에는 시작가가 들어 있을 수 있어 상태로 첫 입찰을 판단한다.
        if (auctionEntity.isWaiting()) {
            if (auctionConfirmDto.getPrice() < auctionEntity.getProductEntity().getStartPrice()) {
                throw new IllegalArgumentException("첫 입찰 금액은 상품 시작가 이상이어야 합니다.");
            }
        } else {
            if (auctionConfirmDto.getPrice() - auctionEntity.getTopPrice() < 1000) {
                throw new IllegalArgumentException("입찰 금액은 현재 최고가보다 최소 1,000원 이상 높아야 합니다.");
            }
        }


        // * 레코드 추가된 개수로 반환
        int count = insertBid(auctionConfirmDto);
        
        // * 추가된 레코드 1개가 아니면
        if (count != 1) {
        throw new IllegalStateException("입찰 내역 저장에 실패했습니다.");
    }

        // ! 입찰 저장과 같은 트랜잭션에서 최고가를 갱신해 다음 요청의 중복 가격을 차단한다.
        auctionEntity.setTopPrice(auctionConfirmDto.getPrice());

        // * 입찰 회원의 관심 점수를 갱신한다.
        Long memberId = auctionConfirmDto.getMemberId();
        Integer productId = auctionEntity.getProductEntity().getProductId();

        InterestEntity interestEntity = interestRepository
                .findByMemberEntity_MemberIdAndProductEntity_ProductId(memberId, productId)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        interestEntity.setInterestScore(interestEntity.getInterestScore() + 3.0);
        interestEntity.setLastViewedAt(now);
            
            interestRepository.save(interestEntity);

        return  true;

    }

    

    // * 경매 첫 입찰
    @Transactional 
    public boolean firstBid(AuctionConfirmDto auctionConfirmDto, AuctionEntity auctionEntity){

        
        if (!auctionEntity.isWaiting()) {
        throw new IllegalStateException("대기 상태인 경매만 첫 입찰이 가능합니다.");
        }
        

        // * 대기 상태에서 시작가 기준으로 검증·저장한 뒤 진행 상태로 전환한다.
        auctionConfirmed(auctionConfirmDto, auctionEntity);

        // * 시작 시간, 종료 시간 지정
        LocalDateTime now = LocalDateTime.now();
        //! 나중에 시간 바꾸기
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
        auctionEntity.start(now, endTime);

        // * 스케줄러 등록
        auctionScheduler.scheduleEnd(auctionConfirmDto.getAuctionId(), endTime);
        
        return true;
       
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

        if (auctionConfirmDto.getAuctionId() == null || auctionConfirmDto.getPrice() == null || auctionConfirmDto.getMemberId() == null) {
        throw new IllegalArgumentException("경매 번호, 입찰 금액, 회원 번호는 필수입니다.");
    }

        AuctionEntity auctionEntity = auctionRepository.findByIdForUpdate(auctionConfirmDto.getAuctionId())
            .orElseThrow(() -> new IllegalArgumentException("경매를 찾을 수 없습니다."));


        // 2. 상태에 따라 바로 분기
        if ("대기".equals(auctionEntity.getAuctionStatus())) {
            firstBid(auctionConfirmDto, auctionEntity);
        } else if ("진행".equals(auctionEntity.getAuctionStatus())) {
            auctionConfirmed(auctionConfirmDto, auctionEntity);
        } else {
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }

        eventPublisher.publishEvent(new AuctionConfirmDto(
            auctionConfirmDto.getAuctionId(),
            auctionConfirmDto.getPrice(),
            auctionConfirmDto.getMemberId()
        ));

        return true;
        
       
    }
    // * 경매 상품,기관 상세정보
    public ProductDetailProjection findDetailProduct(
            Integer auctionId,
            Long memberId) {

        ProductDetailProjection detail = auctionRepository
            .findProductDetail(auctionId)
            .orElseThrow(() -> new IllegalArgumentException("해당 경매의 상품 정보를 찾을 수 없습니다."));

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
