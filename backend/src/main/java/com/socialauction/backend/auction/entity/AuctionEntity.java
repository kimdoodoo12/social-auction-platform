package com.socialauction.backend.auction.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.global.BaseTime;
import com.socialauction.backend.products.entity.ProductEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity 
@Table (name = "auction")
@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class AuctionEntity  {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer auctionId;

    @Column 
    private LocalDateTime startTime;

    @Column 
    private LocalDateTime endTime;

    @Column 
    private String auctionStatus;

    @OneToOne
    @JoinColumn (name = "product_id")
    private ProductEntity productEntity;


    @OneToMany (mappedBy = "auctionEntity")
    @ToString.Exclude
    @Builder.Default
    private List<BidEntity> bidList = new ArrayList<>();
}
