package com.example.elicesecondproject.mall.domain.review.repository;

import com.example.elicesecondproject.mall.domain.review.entity.QReview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Double calculateAverageRating(Long productId) {
        QReview review = QReview.review;

        Double avg = queryFactory
                .select(review.rating.avg())
                .from(review)
                .where(
                        review.product.id.eq(productId),
                        review.deletedAt.isNull()  // 소프트삭제 제외
                )
                .fetchOne();

        return avg != null ? avg : 0.0;
    }
}
