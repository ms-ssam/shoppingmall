package com.example.elicesecondproject.mall.domain.auth.store;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.auth.entity.RefreshToken;
import com.example.elicesecondproject.mall.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DbOnlyRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    @Transactional
    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findValidToken(String refreshTokenHash) {
        return refreshTokenRepository.findByRefreshTokenHashAndIsRevokedFalse(refreshTokenHash)
                .filter(rt -> !rt.isExpired());
    }

    @Override
    @Transactional
    public void revokeToken(String refreshTokenHash) {
        refreshTokenRepository.findByRefreshTokenHashAndIsRevokedFalse(refreshTokenHash)
                .ifPresent(RefreshToken::revoke);
    }

    @Override
    @Transactional
    public void revokeByMember(Member member) {
        // 여러 개 있을 수도 있다고 가정하면 List 조회
        var tokens = refreshTokenRepository.findAllByMemberAndIsRevokedFalse(member);

        for (RefreshToken rt : tokens) {
            rt.revoke();
        }
    }
}
