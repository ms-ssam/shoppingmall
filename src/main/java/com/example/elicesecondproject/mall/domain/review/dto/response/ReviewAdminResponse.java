package com.example.elicesecondproject.mall.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReviewAdminResponse {
    private Long id;
    private String productName;
    private String memberNickname;

    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String imageUrl;
}
