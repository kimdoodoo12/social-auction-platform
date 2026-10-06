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
import com.socialauction.backend.auction.user.service.UserAuctionService;
import com.socialauction.backend.products.admin.dto.RecentBid;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
@CrossOrigin (origins = "http://localhost:5173")
@RequiredArgsConstructor 
@RequestMapping ("/user/auction")
public class UserAuctionController {

    private final UserAuctionService auctionService;

    // * ---사용자------------------------------------------------------------------------------------
    
    // * 단일 경매 정보 조회
    @GetMapping("/{id}")
    public AuctionDetailProjection findUserPageAuction(@PathVariable (name = "id") int auctionId) {
    
        return auctionService.findUserPageAuction(auctionId);
    }

    @GetMapping("/bid")
    public List<RecentBid> getMethodName(@RequestParam("id") int auctionId) {
        return auctionService.findUserPageBid(auctionId);
    }

    @PostMapping ("insert")
    public boolean auctionConfirmed(@RequestBody AuctionConfirmDto auctionConfirmDto) {
        
        return auctionService.auctionConfirmed(auctionConfirmDto);
    }
    
}
