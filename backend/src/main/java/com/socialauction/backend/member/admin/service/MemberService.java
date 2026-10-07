package com.socialauction.backend.member.admin.service;

import com.socialauction.backend.bid.repository.BidRepository;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.socialauction.backend.bid.dto.BidDto;
import com.socialauction.backend.bid.entity.BidEntity;
import com.socialauction.backend.member.admin.controller.MemberController;
import com.socialauction.backend.member.admin.dto.MemberBidInfo;
import com.socialauction.backend.member.admin.dto.MemberBuyProduct;
import com.socialauction.backend.member.admin.dto.MemberDto;
import com.socialauction.backend.member.admin.dto.MemberInfoDetail;
import com.socialauction.backend.member.admin.dto.MemberPayment;
import com.socialauction.backend.member.admin.dto.MemberSearchDto;
import com.socialauction.backend.member.entity.MemberEntity;
import com.socialauction.backend.member.repository.MemberRepository;
import com.socialauction.backend.member.user.dto.UserDto;
import com.socialauction.backend.payment.dto.PaymentDto;
import com.socialauction.backend.payment.entity.PaymentEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;


@Service 
@RequiredArgsConstructor
public class MemberService { 
    private final MemberRepository memberRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();





    // 회원관리 관리자 페이지 (전체 조회)
    @Transactional 
    public Page<MemberDto> memberfindAll(int page, int size){

            Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("memberId").ascending()
            );

            Page<MemberEntity> members =
                memberRepository.findAll(pageable);

            return members.map(MemberDto::from);
        } // memberfindall end

    // 회원정지 기능
    @Transactional 
    public boolean userStop(Long userid){
        MemberEntity memberEntity = memberRepository.findById(userid).orElse(null);
        if(memberEntity != null){
            memberEntity.setRole("정지");
            return true;
        }
        return false;
    } // userStop() end
    // 회원정지 복구
    @Transactional 
    public boolean userNoStop(Long userid){
        MemberEntity memberEntity = memberRepository.findById(userid).orElse(null);
        if(memberEntity != null){
            memberEntity.setRole("정상");
            return true;
        }
        return false;
    } // userStop() end


    // 회원별 상세 페이지
    public MemberInfoDetail userDetailInfo(Long userid){

        MemberEntity memberEntity = memberRepository.findById(userid).orElse(null);

        if(memberEntity == null){ return null; }
        MemberInfoDetail mDto = MemberInfoDetail.from(memberEntity);


        // 최근 입찰 내역
        // 1. 해당 회원이 입찰한 최근 상품 목록 조회
        List<MemberBuyProduct> productList =
                memberRepository.findRecentBidProducts(userid);

        // 2. 회원번호 + 상품번호로 입찰 상세 조회
        productList.forEach((product) -> {
            int productId = product.getProductId();
            MemberBidInfo history =
                    memberRepository
                            .findBidHistory(userid, productId)
                            .orElse(null);

            // 조회된 입찰 내역이 있으면 배열에 추가
            if(history != null){
                mDto.getBidHistory().add(history);
            }
        });

        // 최근 낙찰내역 5개
        mDto.getPayDtos().addAll(
            memberRepository.findWinHistory(userid)
        );
        // 미결제 개수
        mDto.setNotpay(
                memberRepository.notPay(
                        mDto.getMemberId()
                )
        );
        System.out.println(mDto.getNotpay());
        return mDto;
    }

    // 회원조회 이름 + 아이디 + 상태 + 가입일 
    @Transactional(readOnly = true)
    public Page<MemberDto> userSearch(
            MemberSearchDto dto,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<MemberEntity> result =
            memberRepository.userSearch(
                    dto.getName(),
                    dto.getEmail(),
                    dto.getRole(),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    pageable
            );

        return result.map(MemberDto::from);
    }


    // 최근입찰내역 5개 
    @Transactional (readOnly = true)
    public List<MemberBidInfo> findRecentBidHistory (Long memberId){
        List<MemberBuyProduct> productlist = memberRepository.findRecentBidProducts(memberId);

        return productlist.stream().map(product ->
             memberRepository.findBidHistory(
                memberId, 
                product.getProductId()
            ).orElse(null)
    ).filter(Objects::nonNull).toList();
    } // 최근입찰내역 end


    //====================================================================
    //=======================================================================================================================

     // 회원가입
    public boolean userSignup(UserDto userDto){
        MemberEntity memberEntity = userDto.toEntity();

        String password = passwordEncoder.encode(userDto.getPassword());
        memberEntity.setPassword(password);
        if( memberEntity.getMemberId() >= 1 )return true;
        return false;
    } // userSignup() end

    // 회원정보 수정 
    @Transactional 
    public boolean userUpdate(Long userid, UserDto userDto){
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


    //====================================================================
    //=======================================================================================================================

    // 로그인 
    public UserDto login(UserDto userDto){
        MemberEntity memberEntity = memberRepository.findByLoginId(userDto.getLoginId());
        if(memberEntity == null) return null;

        boolean result = passwordEncoder.matches(userDto.getPassword(), memberEntity.getPassword());

        if(result == false) return null;

        return UserDto.from(memberEntity);
    }


    // 내정보 조회 
    public UserDto getMyInfo(Long mno){
        Optional<MemberEntity> optional = memberRepository.findById(mno);
        if(optional.isPresent()){
            MemberEntity memberEntity = optional.get();
            return UserDto.from(memberEntity);
        }
        return null;
    }



    // 관리자 여부 
    public boolean isAdmin(Long memberId){
        return memberRepository.findById(memberId)
                            .map(MemberEntity::isStatus).orElse(false);
    }










    //====================================================================
    //=======================================================================================================================


}
