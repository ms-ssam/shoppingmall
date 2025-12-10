package com.example.elicesecondproject.mall.domain.qna.mapper;

import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionResponse toQuestionResponse(Question question);
}
