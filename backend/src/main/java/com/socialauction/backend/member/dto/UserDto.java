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

    private LocalDateTime createdAt;
    private long memberId;

    public MemberEntity toEntity(){
        return MemberEntity.builder()
            .loginId(this.loginId)
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
            // 비번은 Dto로 변환 안함. 
            .name(memberEntity.getName())
            .email(memberEntity.getEmail())
            .phone(memberEntity.getPhone())
            .createdAt(memberEntity.getCreatedAt())
            .memberId(memberEntity.getMemberId())
            .build();
    }
}
