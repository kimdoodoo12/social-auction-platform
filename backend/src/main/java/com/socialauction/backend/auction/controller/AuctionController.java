package com.socialauction.backend.auction.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.auction.dto.AuctionDetailDto;
import com.socialauction.backend.auction.dto.AuctionFindAllDto;
import com.socialauction.backend.auction.service.AuctionService;
import com.socialauction.backend.bid.dto.BidDto;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequiredArgsConstructor 
@RequestMapping ("/auction")
public class AuctionController {

    private final AuctionService auctionService;

   @GetMapping("")
    public Page<AuctionFindAllDto> auctionFindAll(@RequestParam(defaultValue = "0") int page, 
                                    @RequestParam(defaultValue = "8") int size) {
            return auctionService.auctionFindAll(page, size);
            
        }

    @GetMapping("/detail/{id}")
    public AuctionDetailDto auctionDetailFind(@PathVariable("id") int auctionId) {
        return auctionService.auctionDetailFind(auctionId);
    }
    
    

}
