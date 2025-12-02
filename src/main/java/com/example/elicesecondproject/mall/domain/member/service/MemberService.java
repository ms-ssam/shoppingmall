package com.example.elicesecondproject.mall.domain.member.service;

import com.example.elicesecondproject.mall.domain.member.dto.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.dto.UpdateMemberRequest;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.dto.AddMemberRequest;
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

    // 내 정보 수정 - 닉네임(PROFEDIT-F-02), 전화번호 수정(PROFEDIT-F-03)
    @Transactional
    public void updateMyProfile(Long memberId, UpdateMemberRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String nickname = validateAndNormalizeNickname(request.getNickname());
        String phone = validateAndNormalizePhone(request.getPhone());

        member.updateProfile(nickname, phone);
    }

    private String validateAndNormalizeNickname(String nickname) {
        String trimmed = nickname.trim();
        if(trimmed.length() < 2 || trimmed.length() > 20) {
            throw new BusinessException(ErrorCode.MEMBER_INVALID_NICKNAME_LENGTH);
        }
        return trimmed;
    }

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


    // 회원 탈퇴
}
