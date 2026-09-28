package com.socialauction.backend.member.service;

import com.socialauction.backend.member.controller.MemberController;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialauction.backend.bid.dto.BidDto;
import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.member.dto.MemberBidHistoryDto;
import com.socialauction.backend.member.dto.MemberDto;
import com.socialauction.backend.member.dto.UserDto;
import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.member.repository.MemberRepository;
import com.socialauction.backend.payment.dto.PaymentDto;
import com.socialauction.backend.payment.entity.PaymentEntity;

import org.springframework.transaction.annotation.Transactional;


@Service 
public class MemberService {
    @Autowired 
    private MemberRepository memberRepository;



    // // 회원관리 관리자 페이지 (전체 조회)
    // public List<MemberDto> memberfindAll(){
    //     List<MemberEntity> memberEntities = memberRepository.findAll();

    //     memberEntities.stream().map(MemberDto::from).toList();

    //     return memberEntities.stream().map(MemberDto::from).toList();
    // } // memberfindall end

    // 회원관리 관리자 페이지 (전체 조회)
    @Transactional 
    public Page<MemberDto> memberfindAll(int page, int size){

            Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("memberId").descending()
            );

            Page<MemberEntity> members =
                memberRepository.findAll(pageable);

            return members.map(MemberDto::from);
        } // memberfindall end


    // 회원관리 관리자 페이지 (개별 조회 : 이름)
    @Transactional (readOnly = true)
    public List<MemberDto> memberfindName(String name) {
        List<MemberEntity> list = memberRepository.findByName(name);
        List<MemberDto> list2 = list.stream().map(MemberDto::from).toList();
        return list2;
    }// memberfindName() end


    // 회원관리 관리자 페이지 (개별조회 : 이메일)
    @Transactional (readOnly = true)
    public MemberDto memberfindEmail(String email) {
        MemberEntity memberEntity =  memberRepository.findByEmail(email).orElse(null);
        MemberDto memberDto = MemberDto.from(memberEntity);
        return memberDto;
    }// memberfindEmail() end


    // 회원관리 관리자 페이지 (개별조회 이름 + 이메일 )
    @Transactional (readOnly = true)
    public List<MemberDto> memberfindDetail(String search){

        // 이름으로 찾기
        List<MemberEntity> list = memberRepository.findByName(search);
        if(!list.isEmpty()){
            List<MemberDto> list2 = list.stream().map(MemberDto::from).toList();
            return list2;
        }

        // 이름으로 검색결과 없으면 이메일로 찾음 
        MemberEntity memberEntity = memberRepository.findByEmail(search).orElse(null);

        if(memberEntity != null){
            List<MemberDto> list2 = new ArrayList<>();
            list2.add(MemberDto.from(memberEntity));
            return list2;
        }
        return new ArrayList<>();
    }// memberfindDetail() end

    // 회원등록 (로그인) / 암호화 아직 구현 안함. / 아이디,비번,이름,이메일,연락처만 등록
    public boolean userSignup(UserDto userDto){
        MemberEntity memberEntity = memberRepository.save(userDto.toEntity());
        if(memberEntity.getMemberId() >= 1 ){ return true;}
        return false;
    } // userSignup() end

    // 회원정보 수정 
    @Transactional 
    public boolean userUpdate(int userid, UserDto userDto){
        MemberEntity memberEntity = memberRepository.findById(userid).orElse(null);
        if(memberEntity== null){
            return  false;
        }
        memberEntity.setLoginId(userDto.getLoginId());
        memberEntity.setPassword(userDto.getPassword());
        memberEntity.setEmail(userDto.getEmail());
        memberEntity.setName(userDto.getName());
        memberEntity.setPhone(userDto.getPhone());
        return true;
    } // userUpdate() end

    // 회원정지 기능
    @Transactional 
    public boolean userStop(int userid){
        MemberEntity memberEntity = memberRepository.findById(userid).orElse(null);
        if(memberEntity != null){
            memberEntity.setRole("정지");
            return true;
        }
        return false;
    } // userStop() end

    // 회원별 상세 페이지
    public MemberBidHistoryDto userDetailInfo(int userid){
        MemberEntity memberEntity = memberRepository.findById(userid).orElse(null);
        MemberBidHistoryDto mDto = MemberBidHistoryDto.from(memberEntity);
        memberEntity.getBidEntities().forEach((aaa)->{
            BidDto bidDto = BidDto.from(aaa);
            mDto.getBidDtos().add(bidDto);
        });
        memberEntity.getPaymentEntities().forEach((aaa)->{
            PaymentDto paymentDto = PaymentDto.from(aaa);
            mDto.getPayDtos().add(paymentDto);
        });
        return mDto;
    }


}
