package com.example.elicesecondproject.mall.domain.qna.service;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.qna.dto.request.QuestionSearchCondition;
import com.example.elicesecondproject.mall.domain.qna.dto.response.AdminQuestionDetailResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.AdminQuestionListResponse;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import com.example.elicesecondproject.mall.domain.qna.mapper.QuestionMapper;
import com.example.elicesecondproject.mall.domain.qna.repository.QuestionRepository;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final PermissionValidator permissionValidator;

    // 관리자 문의(Q) 목록 조회
    public Page<AdminQuestionListResponse> getAllQuestions(QuestionSearchCondition condition, Pageable pageable) {
        // 1. Repository에서 Fetch Join된 데이터 가져오기
        Page<Question> questions = questionRepository.findAllWithDetails(condition, pageable);

        return questions.map(questionMapper::toAdminQuestionListResponse);
    }

    //관리자 문의 상세 조회
    public AdminQuestionDetailResponse getQuestionDetail(Long questionId) {
        Question question = questionRepository.findDetailById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        return questionMapper.toAdminQuestionDetailResponse(question);
    }

    @Transactional
    public void deleteQuestionByAdmin(Long questionId, Member admin) {
        permissionValidator.validateAdminOnly(admin);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        // 이미 삭제된 경우는 그냥 무시하거나 예외 처리
        if (question.getDeletedAt() != null) {
            return;
        }

        question.softDelete();
    }

    @Transactional
    public void deleteAnswerByAdmin(Long questionId, Member admin) {
        permissionValidator.validateAdminOnly(admin);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        if (!question.isAnswered() || question.getAnswer() == null) {
            return;
        }

        question.removeAnswer();

    }
}
