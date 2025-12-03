package com.example.elicesecondproject.mall.global.jwt;

import com.example.elicesecondproject.mall.domain.member.service.MemberDetailService;
import com.example.elicesecondproject.mall.global.common.MemberConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final MemberDetailService memberDetailService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidityInMilliseconds;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidityInMilliseconds;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream() // 인증 유저의 권한(role)을 토큰에 담아주기 위해 가져옴. STATELESS라 서버가 유저의 정보를 가지고 있지 않기 때문에 권한 정보도 포함해서 보내줘야함.
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validityDate = new Date(now + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(authentication.getName())                          // JWT의 Payload의 subject에 username을 넣음
                .claim(MemberConstants.AUTH_CLAIM, authorities)     // JWT의 Payload에 role 정보를 넣음
                .signWith(key)                                              // SecretKey를 넣음. (JWT가 변조 검증용)
                .expiration(validityDate)                                   // JWT의 만료 시간 넣음
                .compact();
    }

    // Refresh Token은 재발급 용도만 담당하므로 authorities(권한)은 필요없음
    public String createRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date validityDate = new Date(now + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(authentication.getName())
                .signWith(key)
                .expiration(validityDate)
                .compact();
    }


    // 토큰으로 인증된 유저 객체 가져오기 (accessToken용)
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (claims.get(MemberConstants.AUTH_CLAIM) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 권한을 GrantedAuthority 타입으로 변환해야 스프링 시큐리티에 사용 가능
        String email = claims.getSubject();

        UserDetails principal = memberDetailService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(
                principal,
                "",
                principal.getAuthorities()
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // JWT의 Payload(Claims)를 꺼내오는 함수
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)            // SecretKey 유효한지 검증
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}

