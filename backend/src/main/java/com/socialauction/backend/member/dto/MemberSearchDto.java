package com.socialauction.backend.member.dto;

import java.time.LocalDateTime;

import com.socialauction.backend.member.entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Data @Builder 
public class MemberSearchDto {
    private String name;
    private String email;
    private String role;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
