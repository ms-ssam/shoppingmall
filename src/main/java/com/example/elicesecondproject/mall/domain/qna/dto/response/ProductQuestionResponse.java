package com.example.elicesecondproject.mall.domain.qna.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberNicknameResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProductQuestionResponse {
    private Long id;
    private String title;      // 없으면 제거
    private String content;

    private MemberNicknameResponse member;
    private boolean secret;
    private boolean answered;
    private LocalDateTime createdAt;

    private AnswerSummaryResponse answer;
}
