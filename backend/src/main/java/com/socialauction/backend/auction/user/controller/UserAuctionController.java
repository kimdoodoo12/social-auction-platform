package com.socialauction.backend.auction.user.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.auction.queue.AuctionQueue;
import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;
import com.socialauction.backend.auction.user.dto.AuctionBidRequest;
import com.socialauction.backend.global.exception.LoginRequiredException;
import com.socialauction.backend.auction.user.dto.AuctionDetailProjection;
import com.socialauction.backend.auction.user.dto.AuctionFindDto;
import com.socialauction.backend.auction.user.dto.ProductDetailProjection;
import com.socialauction.backend.auction.user.service.UserAuctionService;
import com.socialauction.backend.global.jwt.JWTutil;
import com.socialauction.backend.products.admin.dto.RecentBid;
import com.socialauction.backend.products.user.dto.ProductDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor 
@RequestMapping ("/ieum/user/auction")
public class UserAuctionController {

    private final UserAuctionService userAuctionService;
    private final JWTutil jwtUtil;
    private final AuctionQueue auctionQueue;

    // * ---사용자------------------------------------------------------------------------------------
    
    // * 단일 경매 정보 조회
    @GetMapping("/{id}")
    public AuctionDetailProjection findUserPageAuction(@PathVariable (name = "id") int auctionId) {
    
        return userAuctionService.findUserPageAuction(auctionId);
    }

    @GetMapping("/{id}/images")
    public List<String> findImages(@PathVariable("id") Integer auctionId) {
        return userAuctionService.findProductImages(auctionId);
    }

    // * 입찰 정보 조회
    @GetMapping("/bid")
    public List<RecentBid> findUserPageBid(@RequestParam("id") int auctionId) {
        return userAuctionService.findUserPageBid(auctionId);
    }

    // * 경매 입찰 
    // ! 사용종료
    // @PostMapping ("insert")
    // public boolean auctionConfirmed(@RequestBody AuctionConfirmDto auctionConfirmDto) {
        
    //     return userAuctionService.auctionConfirmed(auctionConfirmDto);
    // }
    

    // * 경매 상품 정보
    @GetMapping("/product/{id}")
    public ProductDetailProjection findDetailProduct(
            @PathVariable(name = "id") Integer auctionId,
            @CookieValue(name = "accessToken", required = false) String token) {

        Long memberId = token == null
            ? null
            : jwtUtil.getMnoFromToken(token);

        return userAuctionService.findDetailProduct(auctionId, memberId);
    }

    // * 첫입찰 
    // ! 사용종료
    // @PostMapping("firstBid")
    // public boolean firstBid(@RequestBody AuctionConfirmDto auctionConfirmDto) {
        
    //     return userAuctionService.firstBid(auctionConfirmDto);
    // }

    

    // * sse 적용된 입찰
    @PostMapping("/sse/Bid")
    public CompletableFuture<Boolean> placeBid(
            @Valid @RequestBody AuctionBidRequest request,
            @CookieValue(name = "accessToken", required = false) String token) {
        Long memberId = token == null ? null : jwtUtil.getMnoFromToken(token);
        if (memberId == null) {
            throw new LoginRequiredException();
        }

        // ! 큐 실행 전에 인증된 ID를 확정한다. 작업 스레드는 HTTP 쿠키에 접근하지 않는다.
        AuctionConfirmDto auctionConfirmDto = new AuctionConfirmDto(
            request.auctionId(), request.price(), memberId
        );
        return auctionQueue.submitBid(auctionConfirmDto, () -> userAuctionService.placeBid(auctionConfirmDto));
    }
    
    // 연관 상품 조회
    @GetMapping("/product/{id}/related")
    public List<ProductDto> findRelatedProducts(@PathVariable("id") Integer productId){
        return userAuctionService.findRelatedProducts(productId);
    }
    
}
