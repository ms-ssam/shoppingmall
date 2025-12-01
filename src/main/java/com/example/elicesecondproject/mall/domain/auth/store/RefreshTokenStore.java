package com.example.elicesecondproject.mall.domain.auth.store;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenStore {

    void save(RefreshToken refreshToken);

    Optional<RefreshToken> findValidToken(String refreshTokenHash);

    void revokeToken(String refreshTokenHash);

    void revokeByMember(Member member);
}
