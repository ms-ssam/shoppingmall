package com.example.elicesecondproject.mall.domain.review.dto.response;

import com.example.elicesecondproject.mall.domain.product.dto.ReviewProductInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class MyReviewResponse {
    private Long id;
    private Integer rating;
    private String content;

    private String imageUrl;

    private LocalDateTime createdAt;

    private ReviewProductInfoDto product;
}