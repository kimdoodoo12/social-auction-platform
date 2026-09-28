package com.socialauction.backend.bid.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.bid.dto.BidDto;
import com.socialauction.backend.bid.dto.BidResultDto;
import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.bid.service.BidService;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor 
@RequestMapping ("/auction")
public class BidController {
    
    private final BidService bidService;

    @GetMapping("/detail")
    public Page<BidDto> bidFindAll( @RequestParam Integer auctionId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "7") int size
        ) {
            return bidService.bidFindAll(auctionId, page, size);
            
        }

        
    @GetMapping("/bidDetail")
    public BidResultDto bidResultfind(@RequestParam Integer auctionId) {
        return bidService.bidResultFind(auctionId);
    }
    
}
