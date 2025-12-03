package com.example.elicesecondproject.mall.domain.member.service;

import com.example.elicesecondproject.mall.domain.member.dto.request.PasswordChangeRequest;
import com.example.elicesecondproject.mall.domain.member.dto.request.WithdrawMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.dto.request.UpdateMemberRequest;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.dto.request.AddMemberRequest;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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

        String nickname = validateAndNormalizeNickname(request.getNickname());
        String phone = validateAndNormalizePhone(request.getPhone());

        member.updateProfile(nickname, phone);
    }

    // 닉네임 유효성 검증 및 앞뒤 공백 제거
    private String validateAndNormalizeNickname(String nickname) {
        String trimmed = nickname.trim();
        if(trimmed.length() < 2 || trimmed.length() > 20) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_NICKNAME_LENGTH);
        }
        return trimmed;
    }

    // 전화번호 유효성 검증
    private String validateAndNormalizePhone(String phone) {

        // 숫자만 남기기
        String digits = phone.replaceAll("[^0-9]", "");

        // 길이 체크 (11자리)
        if (digits.length() != 11) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_PHONE_FORMAT);
        }

        // 010으로 시작하는 번호만 가능
        if (!digits.startsWith("010")) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_PHONE_FORMAT);
        }

        return digits;
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 현재 비밀번호 확인
        if(!bCryptPasswordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }

        // 2. 새로운 비밀번호 정책 검증
        validationNewPassword(request.getNewPassword(), member.getPassword());

        // 3. 새 비밀번호, 비밀번호 확인 일치 확인
        if(!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_NOT_MATCH);
        }

        member.updatePassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
    }

    // 비밀번호 정책 검증
    private void validationNewPassword(String password, String encodedCurrentPassword) {

        // 1. 길이 8자 이상
        if (password.length() < 8){
            throw new BusinessException(ErrorCode.MEMBER_INVALID_PASSWORD_FORMAT);
        }

        // 2. 영어, 숫자, 특수문자 최소 1개씩 포함
        if (!password.matches(".*[A-Za-z].*")) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_PASSWORD_FORMAT);
        }

        if (!password.matches(".*\\d.*")) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_PASSWORD_FORMAT);
        }

        if (!password.matches(".*[@$!%*#?&].*")) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_PASSWORD_FORMAT);
        }

        // 3. 기존 비밀번호랑 동일하면 안됨
        if(bCryptPasswordEncoder.matches(password, encodedCurrentPassword)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(Long memberId, WithdrawMemberRequest request) {
        Member member =  memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 확인
        if(!bCryptPasswordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }

        member.withdraw();
    }
}
