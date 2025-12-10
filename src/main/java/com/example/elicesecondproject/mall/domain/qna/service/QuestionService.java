package com.example.elicesecondproject.mall.domain.qna.service;

import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import com.example.elicesecondproject.mall.domain.qna.mapper.QuestionMapper;
import com.example.elicesecondproject.mall.domain.qna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;


    // 관리자 문의(Q) 목록 조회
    public Page<QuestionResponse> getAllQuestions(Pageable pageable) {
        // 1. Repository에서 Fetch Join된 데이터 가져오기
        Page<Question> questions = questionRepository.findAllWithDetails(pageable);

        return questions.map(questionMapper::toQuestionResponse);
    }


}
