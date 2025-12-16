package com.example.elicesecondproject.mall.domain.auth.controller;

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
    private final MemberService memberService;
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // templates/auth/login.html
    }

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
}
