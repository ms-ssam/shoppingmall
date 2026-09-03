package com.example.elicesecondproject.mall.domain.qna.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberQnaResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductNameResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminQuestionListResponse {
    private Long id;
    private MemberQnaResponse member;
    private ProductNameResponse product;
    private String title;
    private boolean secret;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    // 답변 여부 추가
    private boolean answered;
}
