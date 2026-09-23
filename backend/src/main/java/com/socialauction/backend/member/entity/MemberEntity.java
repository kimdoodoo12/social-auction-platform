package com.socialauction.backend.member.entity;

import java.time.LocalDateTime;

import com.socialauction.backend.global.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "member")
@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class MemberEntity extends BaseTime{
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer memberId;

    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String role;

    private LocalDateTime lockedAt;
    
    // 관리자 인지 회원인지 
    @Builder.Default
    @Column(nullable = false)
    private boolean status = false;
}
