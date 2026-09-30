package com.socialauction.backend.products.admin.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Builder @Data 
public class TotalDto {
    List<RecentBid> recentBid;
    ProductAuctionInfo productAuctionInfo;
    ProductDto productDto;
}
