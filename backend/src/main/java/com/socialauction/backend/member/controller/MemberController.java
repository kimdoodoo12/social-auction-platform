package com.socialauction.backend.member.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;

import com.socialauction.backend.member.dto.MemberBidHistoryDto;
import com.socialauction.backend.member.dto.MemberDto;
import com.socialauction.backend.member.dto.UserDto;
import com.socialauction.backend.member.service.MemberService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController 
@RequiredArgsConstructor 
public class MemberController {
    private final MemberService memberService;

    // 회원관리 관리자 페이지 (전체 조회)
    @GetMapping("/admin/user/manage")
    public Page<MemberDto> memberfindAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "4") int size
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
    public boolean userUpdate(@PathVariable (name = "userid") int userid , @RequestBody UserDto userDto) {
        return memberService.userUpdate(userid,userDto);
    }

    // 회원 정지 기능 
    @PutMapping("/user/stop/{userid}")
    public boolean userStop(@PathVariable (name = "userid")int userid) {
        return memberService.userStop(userid);
    }

    // 회원 상세 페이지 
    @GetMapping("/user/detail/info")
    public MemberBidHistoryDto userDetailInfo(@RequestParam (name = "userid")int userid){
        return memberService.userDetailInfo(userid);
    }
    
    
    
    
    
    
}
