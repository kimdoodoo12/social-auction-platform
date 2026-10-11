package com.socialauction.backend.global.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.socialauction.backend.auction.user.dto.AuctionConfirmDto;
import com.socialauction.backend.auction.user.service.UserAuctionService;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class AuctionSseService {

    private final UserAuctionService userAuctionService;

    private final Map<Integer, List<SseEmitter>> emitters = new ConcurrentHashMap<>();


    public SseEmitter subscribe(Integer auctionId ){
        SseEmitter emitter = new SseEmitter(); // 2-1 : SseEmitter 객체 생성 
        
        emitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 2-4: 안전하게 클라이언트 연결이 비정상이면 리스트에서 삭제

        // * 리액트에서 새로고침(F5) 중복되는 구독 신청으로 문제발생  
        emitter.onCompletion( () -> emitters.remove(emitter) );
        emitter.onTimeout( () -> emitters.remove(emitter) );
        emitter.onError( (e) -> emitters.remove(emitter) );
        
        return emitter; // 2-3 : 생성된 객체를 반환 

    }

    public void broadcastPrice(Integer auctionId, Object priceData) {
        List<SseEmitter> roomEmitters = emitters.get(auctionId);
        if (roomEmitters == null || roomEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : roomEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("priceUpdate") // 리액트에서 들을 이벤트 이름
                        .data(priceData));
            } catch (IOException e) {
                removeEmitter(auctionId, emitter);
            }
        }
    }

    private void removeEmitter(Integer auctionId, SseEmitter emitter) {
        List<SseEmitter> roomEmitters = emitters.get(auctionId);
        if (roomEmitters != null) {
            roomEmitters.remove(emitter);
            if (roomEmitters.isEmpty()) {
                emitters.remove(auctionId);
            }
        }
    }


    
}
