package com.socialauction.backend.member.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.member.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// 회원 정보 등록시 필요한 정보 
@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class UserDto {
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;

    public MemberEntity toEntity(){
        return MemberEntity.builder()
            .loginId(this.loginId)

            // 암호 구현 해야됨. 
            .password(this.password)
            .name(this.name)
            .email(this.email)
            .phone(this.phone)
            .build();
    }

    // entity -> Dto
    public static UserDto from(MemberEntity memberEntity){
        return UserDto.builder()
            .loginId(memberEntity.getLoginId())
            .password(memberEntity.getPassword())
            .name(memberEntity.getName())
            .email(memberEntity.getEmail())
            .phone(memberEntity.getPhone())
            .build();
    }
}
