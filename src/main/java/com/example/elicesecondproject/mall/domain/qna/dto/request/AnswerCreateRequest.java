package com.example.elicesecondproject.mall.domain.qna.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerCreateRequest {

    @NotBlank(message = "답변 내용을 입력해주세요.")
    @Size(max = 255, message = "답변은 최대 255자까지 입력할 수 있습니다.")
    private String content;
}