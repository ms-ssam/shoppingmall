package com.example.elicesecondproject.mall.domain.auth;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.service.MemberDetailService;
import com.example.elicesecondproject.mall.domain.auth.dto.request.LoginRequest;
import com.example.elicesecondproject.mall.domain.auth.dto.response.AuthTokens;
import com.example.elicesecondproject.mall.domain.auth.entity.RefreshToken;
import com.example.elicesecondproject.mall.domain.auth.service.AuthService;
import com.example.elicesecondproject.mall.domain.auth.store.DbOnlyRefreshTokenStore;
import com.example.elicesecondproject.mall.domain.auth.store.DbRedisRefreshTokenStore;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.jwt.JwtProvider;
import com.example.elicesecondproject.mall.global.util.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private DbOnlyRefreshTokenStore refreshTokenStore;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MemberDetailService memberDetailService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("정상적인 요청으로 accessToken, refreshToken 반환")
    void create_access_token_and_refresh_token_success() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password123!");
        String userAgent = "Test-Agent";

        Member member = Member.builder()
                .email("test@example.com")
                .password("encoded")
                .build();

        MemberDetail userDetails = new MemberDetail(member);

        Authentication fakeAuth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(fakeAuth);
        when(jwtProvider.createAccessToken(any(Authentication.class))).thenReturn("mock-access-token");
        when(jwtProvider.createRefreshToken(any(Authentication.class))).thenReturn("mock-refresh-token");

        // when
        AuthTokens tokens = authService.createAccessTokenAndRefreshToken(request, userAgent);

        // then (값 검증)
        assertEquals("mock-access-token", tokens.getAccessToken());
        assertEquals("mock-refresh-token", tokens.getRefreshToken());

        // then
        //(store.save() 호출 검증)
        verify(refreshTokenStore, times(1)).save(any(RefreshToken.class));

        // (authenticate 호출 검증)
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // (access/refresh 토큰 생성 검증)
        verify(jwtProvider, times(1)).createAccessToken(fakeAuth);
        verify(jwtProvider, times(1)).createRefreshToken(fakeAuth);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호가 틀린 경우")
    void create_access_token_and_refresh_token_fail_bad_credentials() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword!");
        String userAgent = "Test-Agent";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.createAccessTokenAndRefreshToken(request, userAgent)
        );

        // 로그인 실패용 ErrorCode로 매핑되었는지 확인
        assertTrue(exception.getMessage().contains("아이디 또는 비밀번호가 올바르지 않습니다."));


        // 실패했으므로 토큰 생성/저장 로직은 전혀 호출되면 안 됨
        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(refreshTokenStore, never()).save(any());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void create_access_token_and_refresh_token_fail_user_not_found() {
        // given
        LoginRequest request = new LoginRequest("notfound@example.com", "password123!");
        String userAgent = "Test-Agent";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.createAccessTokenAndRefreshToken(request, userAgent)
        );

        // 메시지나 ErrorCode 확인
        assertTrue(exception.getMessage().contains("아이디 또는 비밀번호가 올바르지 않습니다."));

        verify(jwtProvider, never()).createAccessToken(any());
        verify(jwtProvider, never()).createRefreshToken(any());
        verify(refreshTokenStore, never()).save(any());
    }

    @Test
    @DisplayName("정상적인 refreshToken이면 accessToken 반환")
    void reissue_success() {
        // --- Given ---
        String rawRefreshToken = "raw-refresh-token";
        String hash = HashUtil.sha256(rawRefreshToken);
        String email = "test@example.com";

        Member member = Member.builder()
                .email(email)
                .password("encoded")
                .role(Role.USER)
                .build();

        RefreshToken token = RefreshToken.builder()
                .member(member)
                .refreshTokenHash(hash)
                .memberAgent("userAgent")
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(100000)))
                .isRevoked(false)
                .build();

        MemberDetail userDetails = new MemberDetail(member);

        when(jwtProvider.validateToken(rawRefreshToken)).thenReturn(true);
        when(jwtProvider.getUsername(rawRefreshToken)).thenReturn(email);
        when(refreshTokenStore.findValidToken(hash)).thenReturn(Optional.ofNullable(token));
        when(memberDetailService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtProvider.createAccessToken(any(Authentication.class))).thenReturn("new-access-token");

        // --- When ---
        String result = authService.reissue(rawRefreshToken);

        // --- Then ---
        assertEquals("new-access-token", result);

        verify(jwtProvider).validateToken(rawRefreshToken);
        verify(jwtProvider).getUsername(rawRefreshToken);
        verify(refreshTokenStore).findValidToken(hash);
        verify(memberDetailService).loadUserByUsername(email);
        verify(jwtProvider).createAccessToken(any(Authentication.class));
    }

    @Test
    @DisplayName("유효하지 않는 refreshToken이면 실패")
    void reissue_fail_invalid_refresh_token() {

        // given
        String rawRefreshToken = "invalid";

        when(jwtProvider.validateToken(rawRefreshToken)).thenReturn(false);

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.reissue(rawRefreshToken)
        );

        assertTrue(exception.getMessage().contains(ErrorCode.REFRESH_TOKEN_INVALID.getMessage()));

        verify(jwtProvider).validateToken(rawRefreshToken);
    }

    @Test
    @DisplayName("데이터 베이스에 토큰이 없을 때")
    void reissue_fail_not_found_refresh_token() {
        // given
        String rawRefreshToken = "raw-refresh-token";
        String email = "test@example.com";
        String hash = HashUtil.sha256(rawRefreshToken);

        when(jwtProvider.validateToken(rawRefreshToken)).thenReturn(true);
        when(jwtProvider.getUsername(rawRefreshToken)).thenReturn(email);
        when(refreshTokenStore.findValidToken(hash)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.reissue(rawRefreshToken)
        );

        assertTrue(exception.getMessage().contains(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage()));

        verify(jwtProvider).validateToken(rawRefreshToken);
        verify(jwtProvider).getUsername(rawRefreshToken);
        verify(refreshTokenStore).findValidToken(hash);
    }

}
