package com.socialauction.backend.global.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class RedisTokenService {
    private final StringRedisTemplate stringRedisTemplate;

    // refresh 토큰 저장 함수 
    public void setRefreshToken(Long mno , String token){
        stringRedisTemplate.opsForValue().set("RT:"+mno, token , Duration.ofDays(7));
    }

    //  refresh 토큰 조회 함수 
    public String getRefreshToken(Long mno){
        return stringRedisTemplate.opsForValue().get("RT:"+mno);
    }


    public boolean deleteRefreshToken(Long mno){
        return stringRedisTemplate.delete("RT:"+mno);
    }


}
