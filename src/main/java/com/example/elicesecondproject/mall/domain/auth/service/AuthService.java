package com.example.elicesecondproject.mall.domain.auth.service;

import com.example.elicesecondproject.mall.domain.Member.entity.Member;
import com.example.elicesecondproject.mall.domain.Member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.Member.service.MemberDetailService;
import com.example.elicesecondproject.mall.domain.auth.dto.response.AuthTokens;
import com.example.elicesecondproject.mall.domain.auth.dto.request.LoginRequest;
import com.example.elicesecondproject.mall.domain.auth.entity.RefreshToken;
import com.example.elicesecondproject.mall.domain.auth.store.DbOnlyRefreshTokenStore;
import com.example.elicesecondproject.mall.domain.auth.store.DbRedisRefreshTokenStore;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.jwt.JwtProvider;
import com.example.elicesecondproject.mall.global.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    //private final DbRedisRefreshTokenStore refreshTokenStore;
    private final DbOnlyRefreshTokenStore refreshTokenStore; // 현재는 db에만 저장
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final MemberDetailService memberDetailService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidityInMilliseconds;

    @Transactional
    public AuthTokens createAccessTokenAndRefreshToken(LoginRequest request, String userAgent) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        MemberDetail userDetails = (MemberDetail) authentication.getPrincipal();
        Member member = userDetails.getMember();

        refreshTokenStore.revokeByMember(member);

        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        String hash = HashUtil.sha256(refreshToken);
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(refreshTokenValidityInMilliseconds));

        RefreshToken tokenEntity = RefreshToken.builder()
                .member(member)
                .refreshTokenHash(hash)
                .memberAgent(userAgent)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();

        refreshTokenStore.save(tokenEntity);

        return new AuthTokens(accessToken, refreshToken);
    }


    public String reissue(String refreshToken) {

        // 1. 토큰 형식/서명/만료 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 2. 토큰에서 username(subject) 꺼내기
        String email = jwtProvider.getUsername(refreshToken);

        // 3. DB/Redis에서 refreshToken 해시 검증
        String hash = HashUtil.sha256(refreshToken);
        RefreshToken token = refreshTokenStore.findValidToken(hash)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 4. 새 Access Token 발급 (여기서 권한은 UserDetailsService 통해 다시 조회 or member.role 사용)
        UserDetails userDetails = memberDetailService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        return jwtProvider.createAccessToken(authentication);
    }

    @Transactional
    public void revokeToken(String refreshToken) {

        String hash = HashUtil.sha256(refreshToken);

        refreshTokenStore.revokeToken(hash);
    }
}
