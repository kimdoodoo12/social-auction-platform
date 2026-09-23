package com.socialauction.backend.auction.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "auction")
@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class AuctionEntity {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer auctionId;

    @Column 
    private LocalDateTime startTime;

    @Column 
    private LocalDateTime endTime;

    @Column 
    private String auctionStatus;

    // @OneToOne(mappedBy = "auctionEntity")
    // @JoinColumn 
    // private Auc
}
