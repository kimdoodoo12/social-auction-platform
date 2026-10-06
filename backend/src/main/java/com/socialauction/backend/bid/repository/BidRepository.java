package com.socialauction.backend.bid.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.bid.dto.BidDto;
import com.socialauction.backend.bid.dto.BidResultDto;
import com.socialauction.backend.bid.entity.BidEntity;

@Repository 
public interface  BidRepository extends JpaRepository<BidEntity,Integer> {
    

    Page<BidEntity>  findByAuctionEntity_AuctionId(
        Integer auctionId,
        Pageable pageable
    );




    //현재 입찰기록 최근 기록 기준으로 출력 
    // 회원: 해당 입찰을 한 회원을 찾기
    // 경매: 해당 입찰이 속한 경매를 찾기
    // 결제: 같은 경매에 참여한 같은 회원의 결제찾기 결제가 없어도 입찰이 나오도록 LEFT JOIN을 사용
    @Query( 

        value = """ 
        SELECT b.auction_id as auctionId, m.name as name, m.phone as phone, b.bid_price as bidPrice, a.end_time as endTime, p.payment_status as paymentStatus FROM bid b JOIN member m ON b.member_id = m.member_id JOIN auction a ON b.auction_id = a.auction_id LEFT JOIN payment p ON p.auction_id = b.auction_id AND p.member_id = b.member_id WHERE b.auction_id = :auctionId ORDER BY b.bid_time DESC limit 1
        """
            ,
            nativeQuery = true
    )
    Optional<BidResultDto> findResultByid(
        @Param("auctionId") Integer auctionId
    );




    // * 새 입찰 레코드 추가
    @Modifying
    @Query(
        value = "INSERT INTO bid (auction_id, member_id, bid_price, bid_time) VALUES (:auctionId, :memberId, :bidPrice, NOW())",
        nativeQuery = true
    )
    int insertBid(
        @Param("auctionId") Integer auctionId,
        @Param("memberId") Integer memberId,
        @Param("bidPrice") Integer bidPrice
    );

    
}
