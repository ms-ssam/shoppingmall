package com.example.elicesecondproject.mall.domain.qna.repository;

import com.example.elicesecondproject.mall.domain.qna.dto.request.QuestionSearchCondition;
import com.example.elicesecondproject.mall.domain.qna.entity.Question;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.elicesecondproject.mall.domain.qna.entity.QQuestion.question;
import static com.example.elicesecondproject.mall.domain.qna.entity.QAnswer.answer;
import static com.example.elicesecondproject.mall.domain.member.entity.QMember.member;
import static com.example.elicesecondproject.mall.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Question> findAllWithDetails(QuestionSearchCondition condition, Pageable pageable) {

        List<Question> content = queryFactory
                .selectFrom(question)
                .leftJoin(question.member, member).fetchJoin()
                .leftJoin(question.product, product).fetchJoin()
                .leftJoin(question.answer, answer).fetchJoin()
                .where(
                        deletedEq(condition.getDeleted()),
                        productNameContains(condition.getProductName()),
                        answeredEq(condition.getAnswered())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable))
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(question.count())
                .from(question)
                .leftJoin(question.product, product)
                .leftJoin(question.answer, answer)
                .where(
                        deletedEq(condition.getDeleted()),
                        productNameContains(condition.getProductName()),
                        answeredEq(condition.getAnswered())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * deleted == null  -> 전체
     * deleted == false -> 삭제 안 됨
     * deleted == true  -> 삭제 됨
     */
    private BooleanExpression deletedEq(Boolean deleted) {
        if (deleted == null) return null; // 전체
        return deleted ? question.deletedAt.isNotNull()
                : question.deletedAt.isNull();
    }


    /**
     * 상품명 키워드 검색 (부분 일치)
     */
    private BooleanExpression productNameContains(String productName) {
        if (!StringUtils.hasText(productName)) return null;
        return product.name.containsIgnoreCase(productName.trim());
    }

    /**
     * answered == null  → 전체
     * answered == true  → 답변 있음
     * answered == false → 답변 없음
     */
    private BooleanExpression answeredEq(Boolean answered) {
        if (answered == null) return null;
        return answered ? answer.id.isNotNull() : answer.id.isNull();
    }

    /**
     * Pageable Sort → QueryDSL OrderSpecifier
     */
    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        if (!pageable.getSort().isEmpty()) {
            for (Sort.Order sort : pageable.getSort()) {
                Order direction = sort.getDirection().isAscending()
                        ? Order.ASC
                        : Order.DESC;

                switch (sort.getProperty()) {
                    case "createdAt":
                        return new OrderSpecifier<>(direction, question.createdAt);
                    case "title":
                        return new OrderSpecifier<>(direction, question.title);
                }
            }
        }
        // 기본 정렬
        return new OrderSpecifier<>(Order.DESC, question.createdAt);
    }
}
