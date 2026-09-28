package com.socialauction.backend.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.auction.entity.AuctionEntity;

@Repository 
public interface AuctionRepository extends JpaRepository <AuctionEntity, Integer> {
    AuctionEntity findByProductEntity_ProductId(Integer productId);
}
