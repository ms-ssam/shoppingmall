package com.example.elicesecondproject.mall.domain.qna.repository;

import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question,Long>, QuestionRepositoryCustom {

}
