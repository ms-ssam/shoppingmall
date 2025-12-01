package com.example.elicesecondproject.mall.domain.auth.controller;


import com.example.elicesecondproject.mall.domain.member.dto.AddMemberRequest;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import com.example.elicesecondproject.mall.domain.auth.dto.response.TokenResponse;
import com.example.elicesecondproject.mall.domain.auth.dto.response.AccessTokenResponse;
import com.example.elicesecondproject.mall.domain.auth.dto.response.AuthTokens;
import com.example.elicesecondproject.mall.domain.auth.dto.request.LoginRequest;
import com.example.elicesecondproject.mall.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AddMemberRequest request) {
        memberService.save(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response,
            HttpServletRequest httpRequest
    ) {
        String userAgent = httpRequest.getHeader("User-Agent"); // 기기 정보
        AuthTokens tokens = authService.createAccessTokenAndRefreshToken(request, userAgent);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                .httpOnly(true) // 자바 스크립트에서 수정 차단
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 현재 하드코딩
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new TokenResponse(tokens.getAccessToken(), "Bearer", 1800));
    }

    @PostMapping("/reissue")
    public ResponseEntity<AccessTokenResponse> reissue(
            @CookieValue("refreshToken") String refreshToken
    ) {
        String newAccessToken = authService.reissue(refreshToken);
        return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        authService.revokeToken(refreshToken);

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // 즉시 만료
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.noContent().build();
    }
}
