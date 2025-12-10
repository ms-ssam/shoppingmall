package com.example.elicesecondproject.mall.domain.qna.repository;

import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.example.elicesecondproject.mall.domain.qna.entity.QQuestion.question;
import static com.example.elicesecondproject.mall.domain.qna.entity.QAnswer.answer;
import static com.example.elicesecondproject.mall.domain.member.entity.QMember.member;
import static com.example.elicesecondproject.mall.domain.product.entity.QProduct.product;

@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Question> findAllWithDetails(Pageable pageable) {

        // 1. 데이터 조회 쿼리 (Fetch Join 적용)
        List<Question> content = queryFactory
                .selectFrom(question)
                .leftJoin(question.member, member).fetchJoin()   // 작성자 정보 즉시 로딩
                .leftJoin(question.product, product).fetchJoin() // 상품 정보 즉시 로딩
                .leftJoin(question.answer, answer).fetchJoin()
                .where(question.deletedAt.isNull()) // Soft Delete 된 데이터 제외
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable)) // 동적 정렬 적용
                .fetch();


        JPAQuery<Long> countQuery = queryFactory
                .select(question.count())
                .from(question)
                .where(question.deletedAt.isNull());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // Pageable의 Sort를 QueryDSL 정렬로 변환하는 헬퍼 메서드
    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        if (!pageable.getSort().isEmpty()) {
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
                // 정렬 기준이 되는 필드명에 따라 분기
                switch (order.getProperty()) {
                    case "createdAt": return new OrderSpecifier<>(direction, question.createdAt);
                    case "title": return new OrderSpecifier<>(direction, question.title);
                }
            }
        }
        return new OrderSpecifier<>(Order.DESC, question.createdAt); // 기본값
    }
}
