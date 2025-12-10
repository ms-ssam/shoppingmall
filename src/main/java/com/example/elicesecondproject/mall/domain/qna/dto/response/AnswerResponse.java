package com.example.elicesecondproject.mall.domain.qna.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberProfileResponse;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class AnswerResponse {

    private Long id;
    private QuestionResponse questionResponse;
    private MemberProfileResponse memberProfileResponse;
    private String content;



}
