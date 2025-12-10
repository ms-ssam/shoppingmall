package com.example.elicesecondproject.mall.domain.qna.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.product.dto.ProductNameResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
public class QuestionResponse {

    private Long id;
    private MemberProfileResponse memberProfileResponse;
    private ProductNameResponse productNameResponse;
    private String title;
    private String content;
    private boolean isSecret;
    private LocalDateTime createdAt;
}


