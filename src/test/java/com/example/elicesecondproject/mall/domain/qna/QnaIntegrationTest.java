package com.example.elicesecondproject.mall.domain.qna;

import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.domain.qna.dto.request.QnaCreateRequest;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import com.example.elicesecondproject.mall.domain.qna.repository.QuestionRepository;
import com.example.elicesecondproject.mall.domain.qna.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource; // [필수] 추가
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@SpringBootTest
@TestConstructor(autowireMode = ALL)
@RequiredArgsConstructor
@Transactional
// 👇 [핵심 해결책] data.sql 자동 실행 방지 (깨끗한 DB에서 시작)
@TestPropertySource(properties = "spring.sql.init.mode=never")
public class QnaIntegrationTest {

    private final QuestionService questionService;
    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private Long testMemberId;
    private Long testProductId;

    @BeforeEach
    void setUp(){
        // 1. 회원 저장
        Member testMember = Member.builder()
                .email("test@example.com")
                .name("test유저")
                .password("1234")
                .role(Role.USER)
                .phone("010-1111-2222")
                .nickname("test")
                .build();
        testMemberId = memberRepository.save(testMember).getId();

        // 2. 카테고리 저장 (H2 제약조건 준수: path, depth 포함)
        Category category = Category.builder()
                .name("QnA 테스트 카테고리")
                .slug("qna-test-slug-" + UUID.randomUUID())
                .displayOrder(1)
                .isVisible(true)
                .path("/")
                .depth(0)
                .build();
        Category savedCategory = categoryRepository.save(category);

        // 3. 상품 저장 (카테고리 주입)
        Product testProduct = new Product(
                "테스트 상품",
                10000,
                10,
                "테스트 상품 설명",
                savedCategory, // 생성된 카테고리 주입
                ProductStatus.SELLING
        );
        testProductId = productRepository.save(testProduct).getId();
    }

    @Test
    @DisplayName("[성공]: 정상적인 문의 생성")
    void CreateQuestionSuccessTest(){
        //given
        QnaCreateRequest request = new QnaCreateRequest();
        request.setTitle("상품 문의 드립니다");
        request.setContent("상품이 사진이 안보이게 생겼어요");
        request.setSecret(false);

        //when
        questionService.createQuestion(testProductId, testMemberId, request);

        //then
        Question saved = questionRepository
                .findTopByMemberIdAndProductIdOrderByIdDesc(testMemberId, testProductId)
                .orElseThrow();

        assertEquals(request.getTitle(), saved.getTitle());
        assertEquals(request.getContent(), saved.getContent());
        assertEquals(request.isSecret(), saved.isSecret());
        assertEquals(testProductId, saved.getProduct().getId());
        assertEquals(testMemberId, saved.getMember().getId());
    }
}