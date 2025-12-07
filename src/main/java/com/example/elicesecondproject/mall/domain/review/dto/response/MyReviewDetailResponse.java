package com.example.elicesecondproject.mall.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class MyReviewDetailResponse {
    private Long id;              // 리뷰 ID
    private Integer rating;       // 별점
    private String content;       // 내용
    private String imageUrl;      // 리뷰 이미지
    private LocalDateTime createdAt;

    private ReviewProductInfoDto product;
}