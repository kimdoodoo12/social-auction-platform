package com.socialauction.backend.member.admin.service;

import com.socialauction.backend.global.encryption.AESencryption;
import com.socialauction.backend.global.encryption.AESdecryption;
import org.springframework.beans.factory.annotation.Value;

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

    // AES 암호화 키
    @Value("${aes.secret}")
    private String key;
    // 비크립트(암호화 사용) 라이브러리 객체주입
     private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입 
    public boolean signup(UserDto userDto){
        MemberEntity memberEntity = userDto.toEntity();
        // DB에 저장할 아이디와 이름을 암호화한다.
        memberEntity.setLoginId(AESencryption.AES(memberEntity.getLoginId(), key));
        memberEntity.setName(AESencryption.AES(memberEntity.getName(), key));

        // 암호화
        String password = passwordEncoder.encode(userDto.getPassword());
        memberEntity.setPassword(password);

        MemberEntity savedEntity = memberRepository.save(memberEntity);
        if(savedEntity.getMemberId() >= 1){return true;}
        return false;
    } // 회원가입 end

    // 로그인 
    public MemberDto login(UserDto userDto){
        MemberEntity memberEntity = memberRepository.findByLoginId(AESencryption.AES(userDto.getLoginId(), key));
        if(memberEntity == null) return null;

        boolean result = passwordEncoder.matches(userDto.getPassword(), memberEntity.getPassword());

        if(result == false) return null; // 로그인 실패 

        MemberDto dto = MemberDto.from(memberEntity);
        dto.setLoginId(AESdecryption.AESDecryption(dto.getLoginId(), key));
        dto.setName(AESdecryption.AESDecryption(dto.getName(), key));
        return dto;
    } // 로그인 end


    // 아이디 중복 여부
    public boolean userfindid(String newid){
        MemberEntity memberEntity = memberRepository.findByLoginId(AESencryption.AES(newid, key));
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
