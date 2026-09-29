package com.socialauction.backend.auction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.auction.entity.AuctionEntity;

@Repository
public interface AuctionRepository extends JpaRepository<AuctionEntity, Integer> {

    // 상품 번호로 경매 조회
    AuctionEntity findByProductEntity_ProductId(Integer productId);


    // 경매 조회 - 현재 최고가
    @Query(
        value = """
            SELECT MAX(b.bid_price)
            FROM bid b
            WHERE b.auction_id = :auctionId
            """,
        nativeQuery = true
    )
    Integer findTopPriceByAuctionId(
        @Param("auctionId") Integer auctionId
    );


    // 경매 조회 - 현재 최고가 입찰한 사람 이름
    @Query(
        value = """
            SELECT m.name
            FROM bid b
            JOIN member m ON b.member_id = m.member_id
            WHERE b.auction_id = :auctionId
            ORDER BY b.bid_price DESC,
                     b.bid_time DESC,
                     b.bid_id DESC
            LIMIT 1
            """,
        nativeQuery = true
    )
    Optional<String> findTopBidNameByAuctionId(
        @Param("auctionId") Integer auctionId
    );


    // 경매 조회 - 입찰 횟수
    @Query(
        value = """
            SELECT COUNT(*)
            FROM bid
            WHERE auction_id = :auctionId
            """,
        nativeQuery = true
    )
    Integer countBidsByAuctionId(
        @Param("auctionId") Integer auctionId
    );

}