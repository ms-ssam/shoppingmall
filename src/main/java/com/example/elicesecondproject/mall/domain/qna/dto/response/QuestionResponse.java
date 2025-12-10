package com.example.elicesecondproject.mall.domain.qna.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.dto.response.MemberQnaResponse;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.product.dto.ProductNameResponse;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Builder
@AllArgsConstructor
public class QuestionResponse {

    private Long id;
    private MemberQnaResponse member;
    private ProductNameResponse product;
    private String title;
    private String content;
    private boolean isSecret;
    private LocalDateTime createdAt;

    // 답변 여부 추가
    private boolean answered;

}


