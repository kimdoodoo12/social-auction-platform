package com.socialauction.backend.member.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.member.admin.dto.MemberBidInfoDto;
import com.socialauction.backend.member.admin.dto.MemberBuyProductDto;
import com.socialauction.backend.member.admin.dto.MemberPayment;
import com.socialauction.backend.member.entity.MemberEntity;



@Repository 
public interface MemberRepository extends JpaRepository<MemberEntity,Long> {


    // 임시


    // 아이디로 자료 검색 
    MemberEntity findByLoginId(String loginId);

    // 이름으로 자료 검색
    List<MemberEntity> findByName(String name);

    // 이메일로 검색 , 이메일은 겹치지 않으므로 list대신 Optional로 감싼다.
    Optional<MemberEntity> findByEmail(String email);

    // 이름 + 이메일 + 상태 + 가입일 검색 어 
    @Query("""
    SELECT m
    FROM MemberEntity m
    WHERE (:name IS NULL OR m.name = :name)
      AND (:email IS NULL OR m.email = :email)
      AND (:role IS NULL OR m.role = :role)
      AND (:startDate IS NULL OR m.createdAt >= :startDate)
      AND (:endDate IS NULL OR m.createdAt <= :endDate)
    """)
    Page<MemberEntity> userSearch(
            @Param("name") String name,
            @Param("email") String email,
            @Param("role") String role,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 낙찰 + 결제 안한거 / 미결제 
     @Query(
        value = """
            SELECT COUNT(*)
            FROM payment
            WHERE member_id = :memberId
            AND payment_status = 0
            """,
        nativeQuery = true
    )
    int notPay(@Param("memberId") Long memberId);


    // ============================
    // 회원이 입찰한 상품 목록 조회 / 최대 5개 
    @Query(
        value = """
            SELECT
                p.product_id AS productId,
                p.name AS productName,
                MAX(b.bid_time) AS lastBidTime
            FROM bid b
            JOIN auction a
                ON b.auction_id = a.auction_id
            JOIN products p
                ON a.product_id = p.product_id
            WHERE b.member_id = :memberId
            GROUP BY
                p.product_id,
                p.name
            ORDER BY lastBidTime DESC
            LIMIT 5
            """,
        nativeQuery = true
    )
    List<MemberBuyProductDto> findRecentBidProducts(
        @Param("memberId") Long memberId
    );


    // 2. 회원PK + 상품PK
    // 상품명 / 내 입찰가 / 최종가 / 결과 / 입찰시각
    // =========================================================
    @Query(
        value = """
            SELECT
                mybid.bid_id AS bidId,

                p.product_id AS productId,

                p.name AS productName,

                mybid.bid_price AS myBidPrice,

                (
                    SELECT MAX(b2.bid_price)
                    FROM bid b2
                    WHERE b2.auction_id = mybid.auction_id
                ) AS finalPrice,

                CASE

                    WHEN NOW() < a.end_time
                        AND mybid.bid_price = (
                            SELECT MAX(b3.bid_price)
                            FROM bid b3
                            WHERE b3.auction_id = mybid.auction_id
                        )
                    THEN '최고입찰'

                    WHEN NOW() < a.end_time
                    THEN '밀렸어요'

                    WHEN NOW() >= a.end_time
                        AND mybid.bid_price = (
                            SELECT MAX(b4.bid_price)
                            FROM bid b4
                            WHERE b4.auction_id = mybid.auction_id
                        )
                    THEN '낙찰'

                    ELSE '미낙찰'

                END AS result,

                mybid.bid_time AS bidTime

            FROM (

                SELECT
                    b.auction_id,
                    b.member_id,
                    b.bid_price,
                    b.bid_time,
                    b.bid_id

                FROM bid b

                JOIN auction a2
                    ON b.auction_id = a2.auction_id

                WHERE b.member_id = :memberId
                  AND a2.product_id = :productId

                

            ) mybid

            JOIN auction a
                ON mybid.auction_id = a.auction_id

            JOIN products p
                ON a.product_id = p.product_id

            WHERE p.product_id = :productId
            ORDER BY mybid.bid_time DESC , mybid.bid_id DESC
            """,
        nativeQuery = true
    )
    List<MemberBidInfoDto> findBidHistory(
        @Param("memberId") Long memberId,
        @Param("productId") int productId
    );


    // 회원별 최근 낙찰내역 5개
    @Query(
        value = """
            SELECT
                a.auction_id AS auctionId,
                p.name AS product,
                o.name AS organization,
                pay.payment_price AS paymentPrice,
                a.end_time AS winningDate,

                CASE
                    WHEN pay.payment_status = 1
                    THEN '결제 완료'
                    ELSE '미결제'
                END AS paymentStatus

            FROM payment pay

            JOIN auction a
                ON pay.auction_id = a.auction_id

            JOIN products p
                ON a.product_id = p.product_id

            JOIN organization o
                ON p.organization_id = o.organization_id

            WHERE pay.member_id = :memberId
            AND pay.payment_status = 1

            ORDER BY a.end_time DESC

            LIMIT 5
            """,
        nativeQuery = true
    )
    List<MemberPayment> findWinHistory(
        @Param("memberId") Long memberId
    );

    
}

//(:email IS NULL OR m.email = :email)
