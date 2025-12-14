package com.example.elicesecondproject.mall.domain.qna.service;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.dto.QnaProductInfoResponse;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.qna.dto.request.QnaCreateRequest;
import com.example.elicesecondproject.mall.domain.qna.dto.response.ProductQuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.dto.response.QuestionSummaryResponse;
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
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductMapper productMapper;
    private final PermissionValidator permissionValidator;

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

    public Page<ProductQuestionResponse> getQuestionsByProduct(Long productId, Member member, Pageable pageable) {
        Page<Question> questions = questionRepository.findByProductId(productId, pageable);

        return questions.map(q -> {
            boolean canViewSecret = true;

            // 비밀글이면 권한 체크 (member == null이면 무조건 권한 없음 처리)
            if (q.isSecret()) {
                canViewSecret = canView(q, member); // validate를 이용한 판별
            }

            // 기본 DTO 매핑
            ProductQuestionResponse dto = questionMapper.toProductQuestionResponse(q);

            //  권한 없으면 비밀글 내용 마스킹
            if (q.isSecret() && !canViewSecret) {
                dto.setTitle("비밀글입니다.");
                dto.setContent("비밀글입니다.");

                // 답변도 같이 숨길지/말지 정책에 따라 선택 (보통 같이 숨김)
                dto.setAnswer(null);
            }

            return dto;
        });
    }

    @Transactional
    public void deleteMyQuestion(Long questionId, Member member) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));

        // 본인 문의인지 검증
        permissionValidator.validate(question, member);

        question.softDelete();
    }

    private boolean canView(Question question, Member member) {
        if (member == null) return false;

        try {
            permissionValidator.validate(question, member);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }
}
