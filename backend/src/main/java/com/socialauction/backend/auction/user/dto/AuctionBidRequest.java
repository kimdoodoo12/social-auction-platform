package com.socialauction.backend.auction.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// ! 이전 클라이언트가 memberId를 보내더라도 입찰자 신원으로 사용하지 않는다.
@JsonIgnoreProperties("memberId")
public record AuctionBidRequest(
    @NotNull Integer auctionId,
    @NotNull @Positive Integer price
) {}
