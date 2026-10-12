package com.socialauction.backend.global.jtw;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.socialauction.backend.member.admin.service.MUserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class JWTutil {
    private final MUserService mUserService;



    @Value("${jwt.secret}") 
    private String key;

    private SecretKey secretkey;

    @PostConstruct      // 객체 생성시 의존성 @value 가 완료된 후에 아래 메소드가 1번 호출 되도록 하는 어노테이션
    public void init(){
        this.secretkey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    // jwt access 토큰 생성 
    public String createAccessToken(Long mno){
        String jwt = Jwts.builder()
                        .claim("type", "ACCESS")
                        .claim("authority", mUserService.isAdmin(mno) ? "ADMIN" : "USER" ) // 권한 확인후 권한 등록
                        .subject(mno+"")
                        .issuedAt(new Date())
                        .expiration(new Date(new Date().getTime() + 1000L * 60 * 20))   // 20분
                        .signWith(secretkey)
                        .compact();
        System.out.println(jwt);
        return jwt;
    } // 토큰 생성 end



    // jwt 토큰 검증 메소드
    public Long getMnoFromToken(String token){
        return getMnoFromToken(token, "ACCESS");
    }
    public Long getRefreshMnoFromToken(String token){
        return getMnoFromToken(token, "REFRESH");
    }
    private Long getMnoFromToken(String token, String expectedType){
        try{
            Claims claims = Jwts.parser()   
                            .verifyWith(secretkey)
                            .build()
                            // 토큰 넘기기
                            .parseSignedClaims(token)
                            .getPayload();
            if (!expectedType.equals(claims.get("type", String.class))) return null;
            Long mno = Long.parseLong(claims.getSubject());
            System.out.println(mno);
            return mno;
        }catch(Exception e){return null;}
    } // 토큰 검증 메소드 end

    // jwt Refresh 토큰 생성 메서드 
    public String createRefreshToken(Long mno){
        return Jwts.builder()
                    .claim("type", "REFRESH")
                    .subject(mno+"")
                    .issuedAt(new Date())
                    .expiration(new Date(new Date().getTime() + 1000L * 60 * 60 * 23))  // 23시간
                    .signWith(secretkey)
                    .compact();
    } // refresh토큰 생성 메서드end

    //관리자 여부 확인 함수 
    public boolean isAdminFromToken(String token){
        try{
            Claims claims = Jwts.parser()
                                .verifyWith(secretkey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
            if(!"ADMIN".equals(claims.get("authority",String.class))) return false;
            return true;
                            
        }catch(Exception e){ return false; }
    } // 관리자 여부 확인



}
