package com.example.elicesecondproject.mall.domain.review.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.review.dto.request.CreateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.request.UpdateReviewRequest;
import com.example.elicesecondproject.mall.domain.review.dto.response.MyReviewResponse;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(@PathVariable Long productId,
                                                                               Pageable pageable
    ){
        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping("/products/{productId}/reviews") //FIXME: 경로 /mypage/orders/{orderItemId}/review 이걸로 수정
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@PathVariable Long productId,
                                                                    @Valid @RequestBody CreateReviewRequest request,
                                                                    @AuthenticationPrincipal MemberDetail principal
    ) {
        ReviewResponse response = reviewService.createReview(productId, request, principal.getMember().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /*@PutMapping("/products/{productId}/reviews/{reviewId}") //FIXME: 경로 /mypage/reviews/{reviewId} 이걸로 수정
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(@PathVariable Long reviewId,
                                                                    @Valid @RequestBody UpdateReviewRequest request,
                                                                    @AuthenticationPrincipal MemberDetail principal
    ) {
        ReviewResponse response = reviewService.updateReview(reviewId, request, principal.getMember().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }*/

    @DeleteMapping("/products/{productId}/reviews/{reviewId}") //FIXME: 경로 /mypage/reviews/{reviewId} 이걸로 수정
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @AuthenticationPrincipal MemberDetail principal) {

        reviewService.softDeleteReview(reviewId, principal.getMember().getId());
        return ResponseEntity.noContent().build();
    }

    // 마이 페이지

    /*@GetMapping("/mypage/reviews") //TODO : PAGE 설정 만들기
    public ResponseEntity<ApiResponse<Page<MyReviewResponse>>> getMyReviews(Pageable pageable,
                                                                        @AuthenticationPrincipal MemberDetail principal
    ){
        Page<MyReviewResponse> reviews = reviewService.getReviewsByMember(principal.getMember().getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }*/
}
