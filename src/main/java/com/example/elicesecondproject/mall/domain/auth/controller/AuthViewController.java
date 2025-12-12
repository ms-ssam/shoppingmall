package com.example.elicesecondproject.mall.domain.auth.controller;

import com.example.elicesecondproject.mall.domain.auth.service.AuthService;
import com.example.elicesecondproject.mall.domain.member.dto.request.AddMemberRequest;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;
    private final MemberService memberService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // templates/auth/login.html
    }

    /*@PostMapping("/login")
    public String login(
            @Valid LoginRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        // 1) 폼 검증 에러 (이메일/비밀번호 NotBlank 같은 것들)
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            String encoded = UriUtils.encode(errorMessage, StandardCharsets.UTF_8);
            return "redirect:/login?error=" + encoded;
        }

        String userAgent = httpRequest.getHeader("User-Agent");

        AuthTokens tokens;
        try {
            // 2) 실제 로그인 시도 (아이디/비번 틀림 등은 여기서 예외 발생하도록)
            tokens = authService.createAccessTokenAndRefreshToken(request, userAgent);
        } catch (BusinessException e) { // 프로젝트에서 쓰는 예외 타입으로 바꿔줘도 됨
            String encoded = UriUtils.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/login?error=" + encoded;
        }

        // 3) 토큰 쿠키에 세팅
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", tokens.getAccessToken())
                .httpOnly(true)
                .secure(false) // HTTPS면 true
                .path("/")
                .maxAge(30 * 60)    // 30분
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", tokens.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 로그인 성공 → 메인 페이지 이동
        return "redirect:/";
    }*/


    @GetMapping("/signup")
    public String signupPage() {
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid AddMemberRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        memberService.save(request);

        // FlashAttribute → redirect 이후 1회만 유지됨
        redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다!");

        // 메인 페이지로 이동
        return "redirect:/";
    }



    /*@PostMapping("/logout")
    public String logout(HttpServletResponse response) {

        // ACCESS_TOKEN 삭제
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true)
                .secure(false)   // HTTPS면 true
                .path("/")
                .maxAge(0)       // 즉시 만료
                .sameSite("Lax")
                .build();

        // REFRESH_TOKEN 삭제
        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return "redirect:/login"; // 로그아웃 후 로그인 페이지로 이동
    }*/


}
