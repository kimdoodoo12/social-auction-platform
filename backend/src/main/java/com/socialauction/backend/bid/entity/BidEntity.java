package com.socialauction.backend.bid.entity;

import java.time.LocalDateTime;

import com.socialauction.backend.auction.entity.AuctionEntity;
import com.socialauction.backend.global.BaseTime;
import com.socialauction.backend.member.entity.MemberEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "bid")
@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class BidEntity extends BaseTime {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer bidId;

    @Column 
    private Integer bidPrice;

    @Column 
    private LocalDateTime bidTime;



    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "auction_id")
    private AuctionEntity auctionEntity;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "member_id")
    private MemberEntity memberEntity;
}
