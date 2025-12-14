package com.example.elicesecondproject.mall.domain.qna.mapper;

import com.example.elicesecondproject.mall.domain.qna.dto.response.AdminQuestionDetailResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.AdminQuestionListResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.ProductQuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionSummaryResponse;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface QuestionMapper {

    AdminQuestionDetailResponse toAdminQuestionDetailResponse(Question question);
    QuestionSummaryResponse toQuestionSummaryResponse(Question question);
    AdminQuestionListResponse toAdminQuestionListResponse(Question question);
    ProductQuestionResponse toProductQuestionResponse(Question question);
}
