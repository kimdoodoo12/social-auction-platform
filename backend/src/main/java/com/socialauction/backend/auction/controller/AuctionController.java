package com.socialauction.backend.auction.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.socialauction.backend.auction.dto.AuctionDetailDto;
import com.socialauction.backend.auction.dto.AuctionFindAllDto;
import com.socialauction.backend.auction.dto.AuctionSearchDto;
import com.socialauction.backend.auction.service.AuctionService;
import com.socialauction.backend.bid.dto.BidDto;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor 
@RequestMapping ("/auction")
public class AuctionController {

    private final AuctionService auctionService;

   @GetMapping("")
    public Page<AuctionFindAllDto> auctionFindAll(@RequestParam(name="page", defaultValue = "0") int page, 
                                    @RequestParam(name="size", defaultValue = "8") int size) {
            return auctionService.auctionFindAll(page, size);
            
        }

    @GetMapping("/detail/{id}")
    public AuctionDetailDto auctionDetailFind(@PathVariable("id") int auctionId) {
        return auctionService.auctionDetailFind(auctionId);
    }

    @GetMapping("/search")
    public Page<AuctionFindAllDto> auctionSearch(@ModelAttribute  AuctionSearchDto auctionSearchDto,
        @RequestParam(name = "page") int page,
        @RequestParam(name = "size") int size) {
        return auctionService.auctionSearch(auctionSearchDto,page,size);
    }
    
    
    

}
