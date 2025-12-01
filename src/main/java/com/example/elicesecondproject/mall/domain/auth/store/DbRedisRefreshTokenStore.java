package com.example.elicesecondproject.mall.domain.auth.store;

import com.example.elicesecondproject.mall.domain.Member.entity.Member;
import com.example.elicesecondproject.mall.domain.auth.entity.RefreshToken;
import com.example.elicesecondproject.mall.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DbRedisRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;

    private String key(String tokenHash) {
        return "RT:" + tokenHash;
    }

    @Override
    @Transactional
    public void save(RefreshToken refreshToken) {

        refreshTokenRepository.save(refreshToken);

        long ttl = Duration.between(LocalDateTime.now(), refreshToken.getExpiresAt()).getSeconds();

        if (ttl > 0) {
            redisTemplate.opsForValue()
                    .set(key(refreshToken.getRefreshTokenHash()), "VALID", ttl, TimeUnit.SECONDS);
        }
    }

    @Override
    public Optional<RefreshToken> findValidToken(String refreshTokenHash) {

        String cacheKey = key(refreshTokenHash);

        // 1) Redis 캐시에서 먼저 확인
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // Redis에서 히트 → DB에서도 다시 검증
            return refreshTokenRepository.findByRefreshTokenHashAndIsRevokedFalse(refreshTokenHash)
                    .filter(rt -> !rt.isExpired());
        }

        // 2) 캐시 미스 → DB 조회
        Optional<RefreshToken> tokenOpt = refreshTokenRepository
                .findByRefreshTokenHashAndIsRevokedFalse(refreshTokenHash)
                .filter(rt -> !rt.isExpired());

        // 3) 유효하면 Redis에 다시 캐싱
        tokenOpt.ifPresent(rt -> {
            long ttl = Duration.between(LocalDateTime.now(), rt.getExpiresAt()).getSeconds();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(cacheKey, "VALID", ttl, TimeUnit.SECONDS);
            }
        });

        return tokenOpt;
    }

    @Override
    @Transactional
    public void revokeToken(String refreshTokenHash) {
        refreshTokenRepository.findByRefreshTokenHashAndIsRevokedFalse(refreshTokenHash)
                .ifPresent(RefreshToken::revoke);

        redisTemplate.delete(key(refreshTokenHash));
    }

    @Override
    @Transactional
    public void revokeByMember(Member member) {
        // 여러 개 있을 수도 있다고 가정하면 List 조회
        var tokens = refreshTokenRepository.findAllByMemberAndIsRevokedFalse(member);

        for (RefreshToken rt : tokens) {
            rt.revoke();
            redisTemplate.delete(key(rt.getRefreshTokenHash()));
        }
    }
}
