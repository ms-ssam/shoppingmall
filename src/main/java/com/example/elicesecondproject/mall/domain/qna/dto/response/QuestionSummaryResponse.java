package com.example.elicesecondproject.mall.domain.qna.dto.response;

import com.example.elicesecondproject.mall.domain.product.dto.QnaProductInfoResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuestionSummaryResponse {
    private QnaProductInfoResponse product;

    private String title;
    private String content;
    private boolean answered;
    private LocalDateTime createdAt;

    private AnswerSummaryResponse answer;
}
