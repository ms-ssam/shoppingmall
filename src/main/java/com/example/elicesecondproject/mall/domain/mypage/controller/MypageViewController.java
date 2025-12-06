package com.example.elicesecondproject.mall.domain.mypage.controller;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("profile/nickname")
    public String updateNickname(@AuthenticationPrincipal MemberDetail principal,
                                 Model model
    ){
        return "mypage/mypage-update-nickname";
    }

    @GetMapping("/reviews")
    public String getMyReview(Model model,
                              Pageable pageable,
                              @AuthenticationPrincipal MemberDetail principal
    ){
        Page<MyReviewResponse> reviews = reviewService.getReviewsByMember(principal.getMember().getId(), pageable);
        model.addAttribute("reviews",reviews);
        return "mypage/mypage-review";
    }

    @GetMapping("/reviews/{reviewId}/edit")
    public String editMyReviewForm(@PathVariable Long reviewId,
                                   @AuthenticationPrincipal MemberDetail principal,
                                   Model model) {

        Long memberId = principal.getMember().getId();
        MyReviewDetailResponse review = reviewService.getMyReviewDetail(reviewId, memberId);

        model.addAttribute("review", review);

        return "mypage/mypage-review-edit";
    }

    //TODO: 공부 다시 하기/
    @PutMapping("/reviews/{reviewId}")
    /*@PostMapping("/reviews/{reviewId}")*/
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
}
