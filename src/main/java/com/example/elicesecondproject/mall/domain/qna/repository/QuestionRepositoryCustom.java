package com.example.elicesecondproject.mall.domain.qna.repository;

import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionRepositoryCustom {

    Page<Question> findAllWithDetails(Pageable pageable);
}