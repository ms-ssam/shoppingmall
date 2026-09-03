package com.example.elicesecondproject.mall.domain.qna.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionSearchCondition {
    private Boolean answered;
    private String productName;
    private Boolean deleted;
}
