package com.socialauction.backend.global.authority;



import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.socialauction.backend.global.jtw.JWTutil;
import com.socialauction.backend.member.admin.service.MUserService;
import com.socialauction.backend.member.admin.service.MemberService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Component @RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {
    private final JWTutil jwtUtil;
    private final MemberService memberService;
    private final MUserService mUserService;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        // 브라우저의 CORS 사전 확인 요청 
        if("OPTIONS".equals(request.getMethod())){
            return true;
        }
        String token = null;

        if(request.getCookies() != null){
            for(Cookie cookie : request.getCookies()){
                if("accessToken".equals(cookie.getName())){
                    token = cookie.getValue();
                    break;
                } // if end 
            } // for end
        }// if end
        Long memberId = token == null ? null : jwtUtil.getMnoFromToken(token);

        if(memberId == null){
            response.setStatus(401);    // 로그인 필요 or 토큰 만료 
            return false;
        }
        if(!mUserService.isAdmin(memberId)){ 
            response.setStatus(403); // 403
            return false; 
        } //controller 실행차단
        return true;    
    }
}
