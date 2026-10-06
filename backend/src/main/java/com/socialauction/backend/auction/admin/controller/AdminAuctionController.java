package com.socialauction.backend.auction.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.socialauction.backend.auction.admin.dto.AuctionDetailDto;
import com.socialauction.backend.auction.admin.dto.AuctionFindAllDto;
import com.socialauction.backend.auction.admin.dto.AuctionSearchDto;
import com.socialauction.backend.auction.admin.service.AdminAuctionService;
import com.socialauction.backend.auction.user.dto.AuctionFindDto;
import com.socialauction.backend.bid.dto.BidDto;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor 
@RequestMapping ("/ieum/admin/auction")
public class AdminAuctionController {

    private final AdminAuctionService auctionService;

    // * 관리자

    // * 전체 경매 정보
   @GetMapping("")
    public Page<AuctionFindAllDto> auctionFindAll(@RequestParam(name="page", defaultValue = "0") int page, 
                                    @RequestParam(name="size", defaultValue = "8") int size) {
            return auctionService.auctionFindAll(page, size);
            
        }

    // * 세부 경매 정보
    @GetMapping("/detail/{id}")
    public AuctionDetailDto auctionDetailFind(@PathVariable("id") int auctionId) {
        return auctionService.auctionDetailFind(auctionId);
    }


    // * 경매 검색
    @GetMapping("/search")
    public Page<AuctionFindAllDto> auctionSearch(@ModelAttribute  AuctionSearchDto auctionSearchDto,
        @RequestParam(name = "page") int page,
        @RequestParam(name = "size") int size) {
        return auctionService.auctionSearch(auctionSearchDto,page,size);
    }



}
