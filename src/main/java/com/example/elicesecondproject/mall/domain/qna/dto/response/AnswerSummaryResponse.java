package com.example.elicesecondproject.mall.domain.qna.dto.response;

import com.example.elicesecondproject.mall.domain.member.dto.response.MemberNicknameResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerSummaryResponse {

    private MemberNicknameResponse admin; // 답변 관리자

    private String content;
}
