package com.example.elicesecondproject.mall.domain.review.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberNicknameResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductNameResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReviewAdminResponse {
    private Long id;
    private ProductNameResponse product;
    private MemberNicknameResponse member;

    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private String imageUrl;
}
