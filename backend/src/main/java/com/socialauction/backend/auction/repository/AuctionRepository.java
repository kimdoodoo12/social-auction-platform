package com.socialauction.backend.auction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.auction.admin.dto.AuctionFindAllDto;
import com.socialauction.backend.auction.admin.dto.AuctionSearchDto;
import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.auction.user.dto.AuctionDetailProjection;
import com.socialauction.backend.auction.user.dto.AuctionFindDto;

@Repository
public interface AuctionRepository extends JpaRepository<AuctionEntity, Integer> {

    // * 상품 번호로 경매 조회
    AuctionEntity findByProductEntity_ProductId(Integer productId);


    // * 경매 조회 - 현재 최고가
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


    // * 경매 조회 - 현재 최고가 입찰한 사람 이름
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


    // * 경매 조회 - 입찰 횟수
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


    // * 경매 조회 - 입찰자 수
    @Query(
        value = """
            SELECT COUNT(DISTINCT member_id)
            FROM bid
            WHERE auction_id = :auctionId
            """,
        nativeQuery = true
    )
    Integer countUserAuctionId(
        @Param("auctionId") Integer auctionId
    );


    

    

   

    @Query(value = "SELECT a FROM AuctionEntity a " +
                   "JOIN FETCH a.productEntity p " +
                   "LEFT JOIN FETCH p.organizationEntity o " +
                   "WHERE (:keyword IS NULL " +
                   "   OR p.name LIKE CONCAT('%', :keyword, '%') " +
                   "   OR CAST(a.auctionId AS string) LIKE CONCAT('%', :keyword, '%')) " +
                   "AND (:status IS NULL OR a.auctionStatus = :status) " +
                   "AND (:organization IS NULL OR o.name = :organization) " +
                   "AND (:startDate IS NULL OR a.startTime >= :startDate) " +
                   "AND (:endDate IS NULL OR a.endTime <= :endDate)",
           countQuery = "SELECT COUNT(a) FROM AuctionEntity a " +
                   "JOIN a.productEntity p " +
                   "LEFT JOIN p.organizationEntity o " +
                   "WHERE (:keyword IS NULL " +
                   "   OR p.name LIKE CONCAT('%', :keyword, '%') " +
                   "   OR CAST(a.auctionId AS string) LIKE CONCAT('%', :keyword, '%')) " +
                   "AND (:status IS NULL OR a.auctionStatus = :status) " +
                   "AND (:organization IS NULL OR o.name = :organization) " +
                   "AND (:startDate IS NULL OR a.startTime >= :startDate) " +
                   "AND (:endDate IS NULL OR a.endTime <= :endDate)")
    Page<AuctionEntity> searchAuctions(
        @Param("keyword") String keyword,
        @Param("status") String status,
        @Param("organization") String organization,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );


// * 사용자 -------------------------------------------------------------------------------

    // * 사용자 화면 - 단일 경매 상품 정보 조회 쿼리
    @Query(value = """
    SELECT 
        p.name AS productName,
        p.description AS productContent,
        p.start_price AS startPrice,
        a.auction_status AS status,
        a.end_time AS endTime,
        a.top_price AS topPrice,
        
        COUNT(b.bid_id) AS bidCount,
        COUNT(DISTINCT b.member_id) AS bidMemberCount
    FROM auction a 
    JOIN products p ON a.product_id = p.product_id
    LEFT JOIN bid b ON a.auction_id = b.auction_id
    WHERE a.auction_id = :auctionId
    """, nativeQuery = true)
Optional<AuctionDetailProjection> UserPageAuctions(@Param("auctionId") Integer auctionId);


// * 마감 시간 확인
@Query("SELECT a.endTime FROM AuctionEntity a WHERE a.id = :id")
Optional<LocalDateTime> findEndTimeById(@Param("id") Integer auctionid);


// * sql 레코드 잠금
@Query(
    value = "SELECT * FROM auction WHERE auction_id = :id FOR UPDATE",
    nativeQuery = true
)
Optional<AuctionEntity> findByIdForUpdate(@Param("id") Integer auctionId);

//todo
// * 경매 상세 설명  
// @Query ("SELECT ")


}




