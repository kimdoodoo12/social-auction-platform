package com.socialauction.backend.member.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.socialauction.backend.member.dto.MemberBidHistoryDto;
import com.socialauction.backend.member.dto.MemberDto;
import com.socialauction.backend.member.dto.MemberSearchDto;
import com.socialauction.backend.member.dto.UserDto;
import com.socialauction.backend.member.service.MUserService;
import com.socialauction.backend.member.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController 
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor 
public class MemberController {
    private final MemberService memberService;
    private final MUserService mUserService;

    // 회원관리 관리자 페이지 (전체 조회)
    @GetMapping("/admin/user/manage")
    public Page<MemberDto> memberfindAll(
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size", defaultValue = "4") int size
    ) {
        return memberService.memberfindAll(page, size);
    } // memberfindAll() end

    // // 회원관리 관리자 페이지 (전체 조회)
    // @GetMapping ("/admin/user/manage")
    // public List<MemberDto> memberfindAll(){
    //     return memberService.memberfindAll();
    // } // memberfindAll() end

    // 회원관리 관리자 페이지 (개별 조회 : 이름)
    @GetMapping("/admin/user/name")
    public List<MemberDto> memberfindName(@RequestParam(name = "name")  String name) {
        return memberService.memberfindName(name);
    }// memberfindName() end

    // 회원관리 관리자 페이지 (개별조회 : 이메일)
    @GetMapping("/admin/user/email")
    public MemberDto memberfindEmail(@RequestParam(name = "email")  String email) {
        return memberService.memberfindEmail(email);
    }// memberfindEmail() end

    // 회원관리 관리자 페이지 (개별조회 이름 + 이메일)
    @GetMapping("/admin/user/detail")
        public List<MemberDto> memberfindDetail(@RequestParam (name = "detail") String search){
        return memberService.memberfindDetail(search);
    }// memberfindDetail() end

    
    // 회원등록 ( 암호화 아직 구현 안함 ) 아이디,비번,연락처,이메일,이름만 등록
    @PostMapping("/user/singup")
    public boolean userSignup(@RequestBody UserDto userDto){
        return memberService.userSignup(userDto);
    }

    // 회원정보수정( 등록때 한것만 수정할 수 있도록 )
    @PutMapping("/user/update/{userid}")
    public boolean userUpdate(@PathVariable ("userid") int userid , @RequestBody UserDto userDto) {
        return memberService.userUpdate(userid,userDto);
    }

    // 회원 정지 기능 
    @PutMapping("/user/stop/{userid}")
    public boolean userStop(@PathVariable ("userid")int userid) {
        return memberService.userStop(userid);
    }
    // 회원 정지 풀기 
    @PutMapping("/user/normal/{userid}")
    public boolean userNoStop(@PathVariable ("userid")int userid) {
        return memberService.userNoStop(userid);
    }

    // 회원 상세 페이지 
    @GetMapping("/user/detail/info/{userid}")
    public MemberBidHistoryDto userDetailInfo(@PathVariable ("userid")int userid){
        return memberService.userDetailInfo(userid);
    }
    
    // 이름 + 아이디 + 가입일 + 전체 상태 
    @GetMapping("/admin/user/search")
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

    // 로그인
    @PostMapping("/user/login")
    public boolean login(@RequestBody UserDto userDto , HttpSession session) {
        MemberDto result = mUserService.login(userDto);
        if(result == null) return false;

        session.setAttribute("login_member", result);
        
        return true;
    }
    
    // 로그아웃 
    @PostMapping("/user/logout")
    public boolean logout(HttpSession httpSession) {
        httpSession.invalidate();
        
        return true;
    }

    // 아이디 중복 여부
    @GetMapping("/user/signup/findid")
    public boolean userfindid(@RequestBody String newid) {
        return mUserService.userfindid(newid);
    }
    
    
    
    
    
    
    
    
}
