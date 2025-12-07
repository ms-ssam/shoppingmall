package com.example.elicesecondproject.mall.domain.mypage.controller;

import com.example.elicesecondproject.mall.domain.member.dto.request.UpdateMemberRequest;
import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.service.MemberService;
import com.example.elicesecondproject.mall.domain.review.dto.request.UpdateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewDetailResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MypageViewController {
    private final MemberService memberService;
    private final ReviewService reviewService;

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

    @GetMapping("/profile/nickname-phone")
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

        return "mypage/mypage-profile-nickname-phone-edit";
    }

    @PutMapping("/profile/nickname-phone")
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
            return "mypage/mypage-profile-nickname-phone-edit";
        }

        memberService.updateMyProfile(memberId, form);
        redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");

        return "redirect:/mypage/profile";
    }

    @GetMapping("/reviews")
    public String getMyReview(Model model,
                              Pageable pageable,
                              @AuthenticationPrincipal MemberDetail principal
    ){
        Page<MyReviewResponse> reviews = reviewService.getReviewsByMember(principal.getMember().getId(), pageable);
        model.addAttribute("reviews",reviews);
        return "mypage/mypage-reviews";
    }

    @GetMapping("/reviews/{reviewId}")
    public String editMyReviewForm(@PathVariable Long reviewId,
                                   @AuthenticationPrincipal MemberDetail principal,
                                   Model model) {

        Long memberId = principal.getMember().getId();
        MyReviewDetailResponse review = reviewService.getMyReviewDetail(reviewId, memberId);

        model.addAttribute("review", review);

        return "mypage/mypage-review-edit";
    }

    @PutMapping("/reviews/{reviewId}")
    public String updateMyReview(@PathVariable Long reviewId,
                                 @Valid @ModelAttribute UpdateReviewRequest request,
                                 @RequestParam(required = false) MultipartFile imageFile,
                                 @RequestParam(required = false, defaultValue = "false") boolean deleteImage,
                                 @AuthenticationPrincipal MemberDetail principal) {

        reviewService.updateMyReview(reviewId,
                principal.getMember().getId(),
                request,
                imageFile,
                deleteImage);

        return "redirect:/mypage/reviews";
    }

    @DeleteMapping("/reviews/{reviewId}")
    public String deleteMyReview(@PathVariable Long reviewId,
                                 @AuthenticationPrincipal MemberDetail principal

    ) {
        reviewService.softDeleteReview(reviewId, principal.getMember().getId());

        return "redirect:/mypage/reviews";
    }
}
