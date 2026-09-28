package com.socialauction.backend.member.repository;


import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialauction.backend.member.entity.MemberEntity;

@Repository 
public interface MemberRepository extends JpaRepository<MemberEntity,Integer> {

    // 이름으로 자료 검색
    List<MemberEntity> findByName(String name);

    // 이메일로 검색 , 이메일은 겹치지 않으므로 list대신 Optional로 감싼다.
    Optional<MemberEntity> findByEmail(String email);
}
