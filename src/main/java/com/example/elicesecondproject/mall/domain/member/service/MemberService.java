package com.example.elicesecondproject.mall.domain.member.service;

import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.member.dto.request.PasswordChangeRequest;
import com.example.elicesecondproject.mall.domain.member.dto.request.UpdateMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.request.WithdrawMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.dto.request.AddMemberRequest;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.FieldValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public Long save(AddMemberRequest dto) {
        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .nickname(dto.getNickname())
                .phone(dto.getPhone())
                .role(Role.USER)
                .build();

        Cart cart = Cart.create();
        member.assignCart(cart);

        return memberRepository.save(member).getId();
    }

    // 내 정보 조회
    public MemberProfileResponse getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MemberProfileResponse.from(member);
    }

    // 내 정보 수정 - 닉네임, 전화번호
    @Transactional
    public void updateMyProfile(Long memberId, UpdateMemberRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String nickname = request.getNickname().trim();
        String phone = request.getPhone();

        member.updateProfile(nickname, phone);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if(!bCryptPasswordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {

            throw new FieldValidationException("currentPassword", "", "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새로운 비밀번호와 기존 비밀번호 일치하면 안됨.
        if(bCryptPasswordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new FieldValidationException("newPassword", "", "새로운 비밀번호가 기존 비밀번호와 동일합니다.");
        }

        member.updatePassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(Long memberId, WithdrawMemberRequest request) {
        Member member =  memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 확인
        if(!bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new FieldValidationException("password", "", "비밀번호가 일치하지 않습니다.");
        }

        member.withdraw();
    }
}
