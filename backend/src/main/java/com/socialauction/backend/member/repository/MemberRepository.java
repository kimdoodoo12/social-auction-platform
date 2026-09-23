package com.socialauction.backend.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.member.entity.MemberEntity;

@Repository 
public interface MemberRepository extends JpaRepository<MemberEntity,Integer> {
    
}
