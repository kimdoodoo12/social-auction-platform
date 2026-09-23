package com.socialauction.backend.bid.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "bid")
@NoArgsConstructor @AllArgsConstructor @Data @Builder 
public class BidEntity {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer bidId;

    @Column 
    private Integer bidPrice;

    @Column 
    private LocalDateTime bidTime;







    // @ManyToOne(fetch = Fetch)
    // @JoinColumn 
    // private Auc
}
