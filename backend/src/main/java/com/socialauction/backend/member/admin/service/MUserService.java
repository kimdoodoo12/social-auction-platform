package com.socialauction.backend.member.admin.service;

import org.springframework.stereotype.Service;

import com.socialauction.backend.member.admin.dto.MemberDto;
import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.member.repository.MemberRepository;
import com.socialauction.backend.member.user.dto.UserDto;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class MUserService {
    private final MemberRepository memberRepository;
    // 비크립트(암호화 사용) 라이브러리 객체주입
     private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입 
    public boolean signup(UserDto userDto){
        MemberEntity memberEntity = userDto.toEntity();

        // 암호화
        String password = passwordEncoder.encode(userDto.getPassword());
        memberEntity.setPassword(password);

        MemberEntity savedEntity = memberRepository.save(memberEntity);
        if(savedEntity.getMemberId() >= 1){return true;}
        return false;
    } // 회원가입 end

    // 로그인 
    public MemberDto login(UserDto userDto){
        MemberEntity memberEntity = memberRepository.findByLoginId(userDto.getLoginId());
        if(memberEntity == null) return null;

        boolean result = passwordEncoder.matches(userDto.getPassword(), memberEntity.getPassword());

        if(result == false) return null; // 로그인 실패 

        return MemberDto.from(memberEntity);
    } // 로그인 end


    // 아이디 중복 여부
    public boolean userfindid(String newid){
        MemberEntity memberEntity = memberRepository.findByLoginId(newid);
        if(memberEntity == null)return false;
        return true;
    } // 아이디 중복 여부 

    // 이메일 인증 



    // 관리자 여부 확인 
    public boolean isAdmin(Long memberId){
        MemberEntity memberEntity = memberRepository.findById(memberId).orElse(null);
        if(memberEntity == null)return false;
        if(memberEntity.isStatus() == true ){ return true; }
        return false;
    }
}
