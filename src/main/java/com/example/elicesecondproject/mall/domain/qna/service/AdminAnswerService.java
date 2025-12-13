package com.example.elicesecondproject.mall.domain.qna.service;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.qna.dto.request.AnswerCreateRequest;
import com.example.elicesecondproject.mall.domain.qna.entity.Answer;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import com.example.elicesecondproject.mall.domain.qna.repository.AnswerRepository;
import com.example.elicesecondproject.mall.domain.qna.repository.QuestionRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAnswerService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public void createAnswer(Long questionId, Member admin, AnswerCreateRequest request) {

        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Question question = questionRepository.findForAnswerById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        // 이미 답변이 있으면 등록 불가
        if (question.getAnswer() != null || answerRepository.existsByQuestionId(questionId)) {
            throw new BusinessException(ErrorCode.ANSWER_ALREADY_EXISTS);
        }

        Answer answer = Answer.of(question, admin, request.getContent().trim());
        answerRepository.save(answer);
    }
}
