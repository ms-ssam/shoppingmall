package com.example.elicesecondproject.mall.domain.auth.controller;

import com.example.elicesecondproject.mall.domain.member.dto.request.AddMemberRequest;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    public String signupPage(Model model) {
        model.addAttribute("form", new AddMemberRequest());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("form") AddMemberRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            memberService.save(request);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.DUPLICATE_EMAIL) {
                bindingResult.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
                return "auth/signup";
            }
            throw e;
        }

        // FlashAttribute → redirect 이후 1회만 유지됨
        redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다!");

        // 메인 페이지로 이동
        return "redirect:/";
    }
}
