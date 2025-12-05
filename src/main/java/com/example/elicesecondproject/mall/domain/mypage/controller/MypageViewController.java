package com.example.elicesecondproject.mall.domain.mypage.controller;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageViewController {
    private final MemberService memberService;

    @GetMapping
    public String mypagePage(@AuthenticationPrincipal MemberDetail principal,
                             Model model
    ){
        Long memberId = principal.getMember().getId();
        MemberProfileResponse response = memberService.getMyProfile(memberId);

        model.addAttribute("member",response);

        return "mypage/mypage-index";
    }

    @GetMapping("/profile")
    public String getProfile(@AuthenticationPrincipal MemberDetail principal,
                             Model model
    ){
        Long memberId = principal.getMember().getId();
        MemberProfileResponse response = memberService.getMyProfile(memberId);

        model.addAttribute("member",response);

        return "mypage/mypage-profile";
    }

    @PutMapping("profile/nickname")
    public String updateNickname(@AuthenticationPrincipal MemberDetail principal,
                                 Model model
    ){
        return "mypage/mypage-update-nickname";
    }
}
