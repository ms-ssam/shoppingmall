package com.example.elicesecondproject.mall.domain.qna.service;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.dto.QnaProductInfoResponse;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.qna.dto.request.QnaCreateRequest;
import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionSummaryResponse;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import com.example.elicesecondproject.mall.domain.qna.mapper.QuestionMapper;
import com.example.elicesecondproject.mall.domain.qna.repository.QuestionRepository;
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
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductMapper productMapper;


    // 관리자 문의(Q) 목록 조회
    public Page<QuestionResponse> getAllQuestions(Pageable pageable) {
        // 1. Repository에서 Fetch Join된 데이터 가져오기
        Page<Question> questions = questionRepository.findAllWithDetails(pageable);

        return questions.map(questionMapper::toQuestionResponse);
    }

    // 사용자 문의 제품 가져오기
    public QnaProductInfoResponse getProductInfo(Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return productMapper.toQnaProductInfoResponse(product);
    }

    // 사용자 문의 생성
    @Transactional
    public void createQuestion(Long productId, Long memberId, QnaCreateRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        Member member = getActiveMember(memberId);

        Question question = Question.of(member, product, request.getTitle(), request.getContent(), request.isSecret());

        questionRepository.save(question);
    }

    public Page<QuestionSummaryResponse> getQuestionListByMember(Long memberId, Pageable pageable) {
        if (!memberRepository.existsByIdAndStatus(memberId, MemberStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Page<Question> questions =
                questionRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable);

        return questions.map(questionMapper::toQuestionSummaryResponse);
    }

    private Member getActiveMember(Long memberId) {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
