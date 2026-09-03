package com.example.elicesecondproject.mall.domain.review.controller;

import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.review.dto.request.ReviewSearchCondition;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewAdminResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reviews")
public class AdminReviewViewController {

    private final ReviewAdminService reviewAdminService;

    @GetMapping
    public String reviewList(
            @ModelAttribute ReviewSearchCondition condition,
            Pageable pageable,
            Model model
    ) {
        Page<ReviewAdminResponse> reviews = reviewAdminService.searchReviews(condition, pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("condition", condition);

        model.addAttribute("menu", "review");
        model.addAttribute("pageTitle", "리뷰 관리");

        return "admin/review/review-list";
    }

    @DeleteMapping("/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId,
                               @AuthenticationPrincipal MemberDetail principal,
                               RedirectAttributes redirectAttributes) {

        reviewAdminService.deleteReviewAsAdmin(reviewId, principal.getMember().getId());

        redirectAttributes.addFlashAttribute("message", "리뷰를 삭제했습니다.");
        return "redirect:/admin/reviews";
    }
}
