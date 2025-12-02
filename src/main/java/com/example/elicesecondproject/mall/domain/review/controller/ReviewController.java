package com.example.elicesecondproject.mall.domain.review.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.review.dto.request.CreateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.request.UpdateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(@PathVariable Long productId,
                                                                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                                                                  Pageable pageable
    ){
        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@PathVariable Long productId,
                                                                    @Valid @RequestBody CreateReviewRequest request,
                                                                    @AuthenticationPrincipal MemberDetail principal
    ) {
        ReviewResponse response = reviewService.createReview(productId, request, principal.getMember().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(@PathVariable Long reviewId,
                                                                    @Valid @RequestBody UpdateReviewRequest request,
                                                                    @AuthenticationPrincipal MemberDetail principal
    ) {
        ReviewResponse response = reviewService.updateReview(reviewId, request, principal.getMember().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @AuthenticationPrincipal MemberDetail principal) {

        reviewService.softDeleteReview(reviewId, principal.getMember().getId());
        return ResponseEntity.noContent().build();
    }
}
