package com.example.elicesecondproject.mall.domain.member.controller;

import com.example.elicesecondproject.mall.domain.member.dto.request.UpdateMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("")
public class MemberViewController {
    private final MemberService memberService;

//    // 내 정보 조회 -> 마이페이지 index에 회원 정보 있음
//    @GetMapping("/profile")
//    public String profile(@AuthenticationPrincipal MemberDetail principal,
//                          Model model) {
//
//        Long memberId = principal.getMember().getId();
//        MemberProfileResponse member = memberService.getMyProfile(memberId);
//
//        model.addAttribute("member", member);
//        return "mypage/mypage-profile";
//    }
    @GetMapping("/mypage/profile")
    public String editProfileForm(@AuthenticationPrincipal MemberDetail principal,
                                  Model model) {

        Long memberId = principal.getMember().getId();
        MemberProfileResponse member = memberService.getMyProfile(memberId);

        // 폼 객체에 기본값 세팅 (UpdateMemberRequest DTO 사용)
        UpdateMemberRequest form = new UpdateMemberRequest();
        form.setNickname(member.getNickname());
        form.setPhone(member.getPhone()); // 필드명에 맞게 수정

        model.addAttribute("member", member); // 화면 상단 정보
        model.addAttribute("form", form);     // 수정 폼

        return "mypage/mypage-profile-edit";
    }

    // 회원 정보 수정 처리
    @PutMapping("/mypage/profile")
    public String editProfile(@AuthenticationPrincipal MemberDetail principal,
                              @Valid @ModelAttribute("form") UpdateMemberRequest form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Long memberId = principal.getMember().getId();

        if (bindingResult.hasErrors()) {
            // 다시 정보 불러와서 폼 화면으로
            MemberProfileResponse member = memberService.getMyProfile(memberId);
            model.addAttribute("member", member);
            return "mypage/mypage-profile-edit";
        }

        memberService.updateMyProfile(memberId, form);
        redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");

        return "redirect:/mypage/profile";
    }
}
