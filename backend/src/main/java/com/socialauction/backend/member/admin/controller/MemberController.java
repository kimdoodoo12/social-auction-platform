package com.socialauction.backend.member.admin.controller;


import java.time.Duration;
import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.socialauction.backend.global.jtw.JWTutil;
import com.socialauction.backend.member.admin.dto.MemberDto;
import com.socialauction.backend.member.admin.dto.MemberInfoDetail;
import com.socialauction.backend.member.admin.dto.MemberSearchDto;
import com.socialauction.backend.member.admin.service.MUserService;
import com.socialauction.backend.member.admin.service.MemberService;
import com.socialauction.backend.member.admin.service.RedisTokenService;
import com.socialauction.backend.member.user.dto.UserDto;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController 
@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@RequestMapping("/ieum/admin/member")
@RequiredArgsConstructor 
public class MemberController {
    private final MemberService memberService;
    private final MUserService mUserService;
    private final  JWTutil jwtUtil;
    private final RedisTokenService redisTokenService;
    

    // 회원관리 관리자 페이지 (전체 조회)
    @GetMapping("/manage")
    public Page<MemberDto> memberfindAll(
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size", defaultValue = "4") int size
    ) {
        return memberService.memberfindAll(page, size);
    } // memberfindAll() end


    // 회원정보수정( 등록때 한것만 수정할 수 있도록 )
    @PutMapping("/update/{userid}")
    public boolean userUpdate(@PathVariable ("userid") Long userid , @RequestBody UserDto userDto) {
        return memberService.userUpdate(userid,userDto);
    }

    // 회원 정지 기능 
    @PutMapping("/stop/{userid}")
    public boolean userStop(@PathVariable ("userid")Long userid) {
        return memberService.userStop(userid);
    }
    // 회원 정지 풀기 
    @PutMapping("/user/normal/{userid}")
    public boolean userNoStop(@PathVariable ("userid")Long userid) {
        return memberService.userNoStop(userid);
    }

    // 회원 상세 페이지 
    @GetMapping("/detail/info/{userid}")
    public MemberInfoDetail userDetailInfo(@PathVariable ("userid")Long userid){
        return memberService.userDetailInfo(userid);
    }
    
    // 이름 + 아이디 + 가입일 + 전체 상태 
    @GetMapping("/search")
    public Page<MemberDto> userSearch(
        @ModelAttribute MemberSearchDto dto,
        @RequestParam(name = "page") int page,
        @RequestParam(name = "size") int size
    ) {
        return memberService.userSearch(dto, page, size);
    }
    // ============================================================================================= //
    // 사용자 / 세션

    // 회원가입 
    @PostMapping("/user/signup")
    public boolean signup(@RequestBody UserDto userDto) {
        return mUserService.signup(userDto);
    }

    // // 로그인
    // @PostMapping("/user/login")
    // public boolean login(@RequestBody UserDto userDto , HttpSession session) {
    //     MemberDto result = mUserService.login(userDto);
    //     if(result == null) return false;

    //     session.setAttribute("login_member", result);
        
    //     return true;
    // }
    
    // // 로그아웃 
    // @PostMapping("/user/logout")
    // public boolean logout(HttpSession httpSession) {
    //     httpSession.invalidate();
        
    //     return true;
    // }



    // ============================================================================================= //
    // 회원가입 , 로그인 , 인증 
    @PostMapping("/user/login")
    public UserDto login (@RequestBody UserDto userDto , HttpServletResponse response){
        UserDto result = memberService.login(userDto);
        if(result == null) return null;      // 로그인 실패 

        String accessToken = jwtUtil.createAccessToken(result.getMemberId());
        String refreshToken = jwtUtil.createRefreshToken(result.getMemberId());

        // refreshToken만 레디스에 저장 
        redisTokenService.setRefreshToken(result.getMemberId(), refreshToken);

        ResponseCookie cookie1 = ResponseCookie.from("accessToken",accessToken)
                                                .path("/").maxAge(Duration.ofMinutes(20))
                                                .httpOnly(true).secure(false).sameSite("Lax").build();

        ResponseCookie cookie2 = ResponseCookie.from("refreshToken",refreshToken)
                                                .path("/").maxAge(Duration.ofDays(7))
                                                .httpOnly(true).secure(false).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie1.toString() );
        response.addHeader(HttpHeaders.SET_COOKIE, cookie2.toString() );

        System.out.println("accessToken = " + accessToken);
        System.out.println("refreshToken = " + refreshToken);

        System.out.println("cookie1 = " + cookie1);
        System.out.println("cookie2 = " + cookie2);

        return result;
    }



    // 내정보 조회 
    @GetMapping("/user/me")
    public UserDto getMyInfo(@CookieValue (value = "accessToken" , required = false)String token) {
        if(token == null)return null;
        //=======================================
        Long loginMno = jwtUtil.getMnoFromToken(token);
        if (loginMno == null) return null;

        return memberService.getMyInfo(loginMno);
    }
    
    

    // 로그아웃 (초기화)
    @PostMapping("/user/logout")
    public boolean logout(@CookieValue (value = "accessToken" , required = false )String accessToken , @CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if(accessToken != null || refreshToken != null){
            Long mno = jwtUtil.getMnoFromToken(accessToken);
            if (mno == null) mno = jwtUtil.getRefreshMnoFromToken(refreshToken);

            // 레디스내 refresh토큰 삭제
            if (mno != null) redisTokenService.deleteRefreshToken(mno);
        }
        
        // 쿠키삭제 
        ResponseCookie cookie1 = ResponseCookie.from("accessToken" , "")
                                            .path("/").maxAge(0)
                                            .httpOnly(true).secure(false).build();
        ResponseCookie cookie2 = ResponseCookie.from("refreshToken" , "")
                                            .path("/").maxAge(0)
                                            .httpOnly(true).secure(false).build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie1.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie2.toString());
        return true;
    }
    


    // 토큰 만료 될때
    @PostMapping("/reissue")
    public UserDto reissue(@CookieValue(value = "refreshToken" , required = false)String refreshToken , HttpServletResponse response ) {
        if(refreshToken == null)return null;

        Long mno = jwtUtil.getRefreshMnoFromToken(refreshToken);
        if (mno == null) return null;
        String savedRefreshToken = redisTokenService.getRefreshToken(mno);
        if(savedRefreshToken == null || !refreshToken.equals(savedRefreshToken)) return null;

        String newAccessToken = jwtUtil.createAccessToken(mno);
        String newRefreshToken = jwtUtil.createRefreshToken(mno);

        redisTokenService.setRefreshToken ( mno , newRefreshToken );

        ResponseCookie cookie1 = ResponseCookie.from("accessToken", newAccessToken)
                                        .path("/").maxAge(Duration.ofMinutes(20))
                                        .httpOnly(true).secure(false).sameSite("Lax").build();
        ResponseCookie cookie2 = ResponseCookie.from("refreshToken" , newRefreshToken)
                                        .path("/").maxAge(Duration.ofDays(7))
                                        .httpOnly(true).secure(false).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie1.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie2.toString());
        
        return memberService.getMyInfo(mno);
    }
    














    //+++++++++==============================================================================================

    // 아이디 중복 여부
    @GetMapping("/user/signup/findid")
    public boolean userfindid(@RequestParam(name = "newid") String newid) {
        return mUserService.userfindid(newid);
    }
    
    
    
    
    
    
    
    
}
