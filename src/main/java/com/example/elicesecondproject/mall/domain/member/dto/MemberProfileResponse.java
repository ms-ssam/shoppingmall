package com.example.elicesecondproject.mall.domain.member.dto;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MemberProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname; // 리뷰 작성 시 노출되는 이름
    private String phone;

    public static MemberProfileResponse from(Member member) {
        return MemberProfileResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .build();
    }
}
