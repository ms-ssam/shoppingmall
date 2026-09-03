package com.example.elicesecondproject.mall.domain.member.dto.response;

import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse { // 마이페이지, 관리자 조회용
    private Long id;
    private String email;
    private String name;
    private String nickname; // 리뷰 작성 시 노출되는 이름
    private String phone;

    private Role role;
    private MemberStatus status;
    //private boolean emailVerified; // 이메일 인증 도입 시

    //private AuthProvider provider; // 소셜로그인 도입 시

    private LocalDateTime lastLoginAt;
}
