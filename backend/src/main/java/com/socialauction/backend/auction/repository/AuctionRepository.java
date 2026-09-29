package com.socialauction.backend.auction.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.auction.dto.AuctionFindAllDto;
import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.bid.dto.BidResultDto;

@Repository 
public interface AuctionRepository extends JpaRepository<AuctionEntity,Integer> {



    //경매 조회 - 현재 최고가 쿼리
    @Query(
    value = "SELECT MAX(b.bid_price) FROM bid b WHERE b.auction_id = :auctionId",
    nativeQuery = true
    )
    Integer findTopPriceByAuctionId(@Param("auctionId") Integer auctionId);

    //경매 조회 - 현재 최고가 입찰한 사람이름
    @Query(
        value = """
            SELECT m.name
            FROM bid b
            JOIN member m ON b.member_id = m.member_id
            WHERE b.auction_id = :auctionId
            ORDER BY b.bid_price DESC, b.bid_time DESC, b.bid_id DESC
            LIMIT 1
            """,
        nativeQuery = true
    )
    Optional<String> findTopBidNameByAuctionId(
        @Param("auctionId") Integer auctionId
    );

    


    //경매조회 - 입찰 횟수
    @Query(
    value = "SELECT COUNT(*) FROM bid WHERE auction_id = :auctionId",
    nativeQuery = true
    )
    Integer countBidsByAuctionId(@Param("auctionId") Integer auctionId);
    


}


