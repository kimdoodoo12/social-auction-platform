package com.socialauction.backend.member.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.member.entity.MemberEntity;



@Repository 
public interface MemberRepository extends JpaRepository<MemberEntity,Integer> {

    // 이름으로 자료 검색
    List<MemberEntity> findByName(String name);

    // 이메일로 검색 , 이메일은 겹치지 않으므로 list대신 Optional로 감싼다.
    Optional<MemberEntity> findByEmail(String email);

    // 이름 + 이메일 + 상태 + 가입일 검색 어 
    @Query("""
    SELECT m
    FROM MemberEntity m
    WHERE (:name IS NULL OR m.name = :name)
      AND (:email IS NULL OR m.email = :email)
      AND (:role IS NULL OR m.role = :role)
      AND (:startDate IS NULL OR m.createdAt >= :startDate)
      AND (:endDate IS NULL OR m.createdAt <= :endDate)
    """)
    Page<MemberEntity> userSearch(
            @Param("name") String name,
            @Param("email") String email,
            @Param("role") String role,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
