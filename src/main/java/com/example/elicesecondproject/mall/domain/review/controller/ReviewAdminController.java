package com.example.elicesecondproject.mall.domain.review.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.review.dto.request.ReviewSearchCondition;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewAdminResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewAdminService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Secured("ROLE_ADMIN")
public class ReviewAdminController {
    private final ReviewAdminService reviewAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewAdminResponse>>> getProductReviews(@ModelAttribute ReviewSearchCondition condition,
                                                                                    @PageableDefault(size = 20)
                                                                                    Pageable pageable
    ){
        Page<ReviewAdminResponse> reviews = reviewAdminService.searchReviews(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @AuthenticationPrincipal MemberDetail principal
    ){
        reviewAdminService.deleteReviewAsAdmin(reviewId, principal.getMember().getId());
        return ResponseEntity.noContent().build();
    }
}
