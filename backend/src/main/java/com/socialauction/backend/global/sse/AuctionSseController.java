package com.socialauction.backend.global.sse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@RestController @RequiredArgsConstructor 
@RequestMapping ("/api/sse") @CrossOrigin (origins = "*")
public class AuctionSseController {
    
    private final AuctionSseService auctionSseService;


    @GetMapping ( value = "/{auctionId}/subscribe" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Integer auctionId){
        return auctionSseService.subscribe(auctionId);
    }



}
