package com.socialauction.backend.products.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.products.dto.ProductAuctionSummary;
import com.socialauction.backend.products.entity.ProductEntity;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {

    @Override
    @EntityGraph(attributePaths = "organizationEntity")
    Page<ProductEntity> findAll(Pageable pageable);

    // 기존 경매/입찰 엔티티를 조회만 하고, 입찰이 없는 경매도 포함한다.
    @Query("""
            select a.productEntity.productId as productId,
                   a.auctionStatus as status,
                   max(b.bidPrice) as currentPrice
            from AuctionEntity a
            left join a.bidList b
            where a.productEntity.productId in :productIds
            group by a.productEntity.productId, a.auctionId, a.auctionStatus
            """)
    List<ProductAuctionSummary> findAuctionSummaries(
            @Param("productIds") List<Integer> productIds);
}
