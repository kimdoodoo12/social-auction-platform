package com.socialauction.backend.global.authority;
// package com.socialauction.backend.global;


// import org.springframework.stereotype.Component;
// import org.springframework.web.servlet.HandlerInterceptor;

// import com.socialauction.backend.member.dto.JWTutil;
// import com.socialauction.backend.member.service.MemberService;

// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;


// @Component @RequiredArgsConstructor
// public class AdminInterceptor implements HandlerInterceptor {
//     private final JWTutil jwtUtil;
//     private final MemberService memberService;

//     @Override
//     public boolean preHandle(
//         HttpServletRequest request,
//         HttpServletResponse response,
//         Object handler
//     ) throws Exception {
//         // 브라우저의 CORS 사전 확인 요청 
//         if("OPTIONS".equals(request.getMethod())){
//             return true;
//         }
//         String token = null;

//         if(request.getCookies() != null){
//             for(Cookie cookie : request.getCookies()){
//                 if("accessToken".equals(cookie.getName())){
//                     token = cookie.getValue();
//                     break;
//                 } // if end 
//             } // for end
//         }// if end
//         Long memberId = token == null ? null : jwtUtil.getMnoFromToken(token);

//         if(memberId == null){
//             response.setStatus(401);    // 로그인 필요 or 토큰 만료 
//             return false;
//         }
//         return true;    //controller 실행 허용 
//     }
// }
