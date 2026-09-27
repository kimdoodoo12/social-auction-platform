package com.socialauction.backend.organization.dto.admin;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor 
public class OrganizationProductResponse {
    private Integer productId;
    private String name;
    private Integer startPrice;
    private Integer currentPrice;
    private String auctionStatus;
    private LocalDateTime createdAt;
}
