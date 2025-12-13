package com.example.elicesecondproject.mall.domain.qna.mapper;

import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionSummaryResponse;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionResponse toQuestionResponse(Question question);
    QuestionSummaryResponse toQuestionSummaryResponse(Question question);
}
