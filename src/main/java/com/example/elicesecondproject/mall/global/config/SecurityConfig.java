package com.example.elicesecondproject.mall.global.config;

import com.example.elicesecondproject.mall.global.jwt.JwtAuthenticationFilter;
import com.example.elicesecondproject.mall.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration          // 스프링 설정 클래스임을 나타냄
@RequiredArgsConstructor
@EnableWebSecurity      // 시큐리티 설정을 활성화하는 어노테이션
@EnableMethodSecurity(securedEnabled = true) // 메서드에서 권환 체크 기능을 사용 가능하게 함
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    // 정적 파일(js, css, html)에는 스프링 시큐리티를 무시. WebSecurityCustomizer(ignoring())는 아예 필터를 타지 않는다.
    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring() .requestMatchers("/static/**");
    }


    @Bean // 패스워드 인코더
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // AuthenticationManager를 빈에서 사용하기 위해 등록.
    // AuthenticationManager.authenticate를 통해 입력 아이디/비밀번호와 DB의 아이디/비밀번호를 비교
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //요청이 들어올때 어떤 보안 필터를 적용하는지를 정의
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)      // CSRF는 세션 + 쿠키 기반 웹 페이지 폼 전송에서 사용.
                // JWT + REST API + SessionCreationPolicy.STATELESS 구조를 사용하므로 비활성화
                //.formLogin(AbstractHttpConfigurer::disable) // 스프링 기본 로그인 폼 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // 요청 헤더에 Authorization: Basic 방식 비활성화. Bearer {JWT} 방식만 사용할 것임

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 서버가 세션을 가지고 있지 않게 STATELESS 설정
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**"/*, "/swagger-ui.html"*/).permitAll() // swagger는 권한없이 이용 (개발 단계 이후 삭제)
                        .requestMatchers("/login", "/signup", "/").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class); // UsernamePasswordAuthenticationFilter 필터 앞에 JwtAuthenticationFilter를 추가

        return http.build();
    }
}

