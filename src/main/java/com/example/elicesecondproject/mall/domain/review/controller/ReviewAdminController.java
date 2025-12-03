package com.example.elicesecondproject.mall.domain.review.controller;

import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewAdminResponse;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewAdminService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class ReviewAdminController {
    private final ReviewAdminService reviewAdminService;

    @Secured("ROLE_ADMIN")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewAdminResponse>>> getProductReviews(@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                                                                                    Pageable pageable
    ){
        Page<ReviewAdminResponse> reviews = reviewAdminService.getAllReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
}
