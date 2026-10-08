package com.socialauction.backend.auction.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;
import com.socialauction.backend.auction.user.dto.AuctionDetailProjection;
import com.socialauction.backend.auction.user.dto.AuctionFindDto;
import com.socialauction.backend.auction.user.dto.ProductDetailProjection;
import com.socialauction.backend.auction.user.service.UserAuctionService;
import com.socialauction.backend.products.admin.dto.RecentBid;

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

    // * ---사용자------------------------------------------------------------------------------------
    
    // * 단일 경매 정보 조회
    @GetMapping("/{id}")
    public AuctionDetailProjection findUserPageAuction(@PathVariable (name = "id") int auctionId) {
    
        return userAuctionService.findUserPageAuction(auctionId);
    }

    // * 입찰 정보 조회
    @GetMapping("/bid")
    public List<RecentBid> findUserPageBid(@RequestParam("id") int auctionId) {
        return userAuctionService.findUserPageBid(auctionId);
    }

    // * 경매 입찰 
    @PostMapping ("insert")
    public boolean auctionConfirmed(@RequestBody AuctionConfirmDto auctionConfirmDto) {
        
        return userAuctionService.auctionConfirmed(auctionConfirmDto);
    }
    

    // * 경매 상품 정보
    @GetMapping("/product/{id}")
    public ProductDetailProjection findDetailProduct(@PathVariable (name = "id") Integer auctionId) {
        return userAuctionService.findDetailProduct(auctionId);
    }

    // // * 첫입찰 
    // @PostMapping("firstBid")
    // public boolean firstBid(@RequestBody AuctionConfirmDto auctionConfirmDto) {
        
    //     return entity;
    // }
    
    
}
