package com.example.elicesecondproject.mall.domain.qna.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 30, message = "제목은 30자 이내로 입력해주세요.")
    private  String title;

    @NotBlank(message = "문의 내용은 필수입니다.")
    @Size(max = 255, message = "문의 내용은 255자 이내로 입력해주세요.")
    private  String content;

    private boolean secret;
}
