package com.socialauction.backend.member.admin.service;

import com.socialauction.backend.global.encryption.AESencryption;
import com.socialauction.backend.global.PageResponse;
import com.socialauction.backend.global.encryption.AESdecryption;
import org.springframework.beans.factory.annotation.Value;

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
import com.socialauction.backend.member.admin.dto.MemberBidInfoDto;
import com.socialauction.backend.member.admin.dto.MemberBuyProductDto;
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

    // AES 암호화 키
    @Value("${aes.secret}")
    private String key;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();





    // 회원관리 관리자 페이지 (전체 조회)
    @Transactional 
    public PageResponse<MemberDto> memberfindAll(int page, int size){

            Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("memberId").ascending()
            );

            Page<MemberEntity> members =
                memberRepository.findAll(pageable);

             // ③ Entity를 DTO로 변환하고 복호화
            Page<MemberDto> response = members.map(member -> {
                MemberDto dto = MemberDto.from(member);

                dto.setLoginId(
                    AESdecryption.AESDecryption(dto.getLoginId(), key)
                );

                dto.setName(
                    AESdecryption.AESDecryption(dto.getName(), key)
                );

                return dto;
            });

            // ④ 최종 반환할 때 PageResponse로 감싸기
            return new PageResponse<>(response);
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
        mDto.setLoginId(AESdecryption.AESDecryption(mDto.getLoginId(), key));
        mDto.setName(AESdecryption.AESDecryption(mDto.getName(), key));


        // 최근 입찰 내역
        // 1. 해당 회원이 입찰한 최근 상품 목록 조회
        List<MemberBuyProductDto> productList =
                memberRepository.findRecentBidProducts(userid);

        // 2. 회원번호 + 상품번호로 입찰 상세 조회
        productList.forEach((product) -> {
            int productId = product.getProductId();
            List<MemberBidInfoDto> histories =
                    memberRepository
                            .findBidHistory(userid, productId);

            // 조회된 입찰 내역이 있으면 배열에 추가
            if(histories != null){
                mDto.getBidHistory().addAll(histories);
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

    @Transactional(readOnly = true)
    public PageResponse<MemberDto> userSearch(
            MemberSearchDto dto,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by("memberId").ascending()
        );

        // Repository 결과는 Page로 받기
        Page<MemberEntity> result =
                memberRepository.userSearch(
                    dto.getName() == null
                        ? null
                        : AESencryption.AES(dto.getName(), key),
                    dto.getEmail(),
                    dto.getRole(),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    pageable
                );

        // DTO 변환 및 복호화
        Page<MemberDto> response = result.map(member -> {
            MemberDto memberDto = MemberDto.from(member);

            memberDto.setLoginId(
                AESdecryption.AESDecryption(memberDto.getLoginId(), key)
            );

            memberDto.setName(
                AESdecryption.AESDecryption(memberDto.getName(), key)
            );

            return memberDto;
        });

        // API 응답 형태로 변환
        return new PageResponse<>(response);
    }


    // 최근입찰내역 5개 
    @Transactional (readOnly = true)
    public List<MemberBidInfoDto> findRecentBidHistory (Long memberId){
        List<MemberBuyProductDto> productlist = memberRepository.findRecentBidProducts(memberId);

        return productlist.stream()
            .flatMap(product ->         // flatmap() : 상품마다 반환되는 입찰 목록들을 하나의 목록으로 합침.
                    memberRepository
                            .findBidHistory(memberId, product.getProductId())
                            .stream()
            )
            .toList();
    } // 최근입찰내역 end


    //====================================================================
    //=======================================================================================================================

     // 회원가입
    public boolean userSignup(UserDto userDto){
        MemberEntity memberEntity = userDto.toEntity();
        // DB에 저장할 아이디와 이름을 암호화한다.
        memberEntity.setLoginId(AESencryption.AES(memberEntity.getLoginId(), key));
        memberEntity.setName(AESencryption.AES(memberEntity.getName(), key));

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
        memberEntity.setLoginId(AESencryption.AES(userDto.getLoginId(), key));
        memberEntity.setPassword(userDto.getPassword());
        memberEntity.setEmail(userDto.getEmail());
        memberEntity.setName(AESencryption.AES(userDto.getName(), key));
        memberEntity.setPhone(userDto.getPhone());
        return true;
    } // userUpdate() end


    //====================================================================
    //=======================================================================================================================

    // 로그인 
    public UserDto login(UserDto userDto){
        MemberEntity memberEntity = memberRepository.findByLoginId(AESencryption.AES(userDto.getLoginId(), key));
        if(memberEntity == null) return null;

        boolean result = passwordEncoder.matches(userDto.getPassword(), memberEntity.getPassword());

        if(result == false) return null;

        UserDto dto = UserDto.from(memberEntity);
        dto.setLoginId(AESdecryption.AESDecryption(dto.getLoginId(), key));
        dto.setName(AESdecryption.AESDecryption(dto.getName(), key));
        return dto;
    }


    // 내정보 조회 
    public UserDto getMyInfo(Long mno){
        Optional<MemberEntity> optional = memberRepository.findById(mno);
        if(optional.isPresent()){
            MemberEntity memberEntity = optional.get();
            UserDto dto = UserDto.from(memberEntity);
            dto.setLoginId(AESdecryption.AESDecryption(dto.getLoginId(), key));
            dto.setName(AESdecryption.AESDecryption(dto.getName(), key));
            return dto;
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
