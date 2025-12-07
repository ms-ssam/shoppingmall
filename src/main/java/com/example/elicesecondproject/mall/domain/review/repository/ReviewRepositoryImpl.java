package com.example.elicesecondproject.mall.domain.review.repository;

import com.example.elicesecondproject.mall.domain.member.entity.QMember;
import com.example.elicesecondproject.mall.domain.product.entity.QProduct;
import com.example.elicesecondproject.mall.domain.review.dto.request.ReviewSearchCondition;
import com.example.elicesecondproject.mall.domain.review.entity.QReview;
import com.example.elicesecondproject.mall.domain.review.entity.Review;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

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

    @Override
    public Page<Review> searchReviews(ReviewSearchCondition condition, Pageable pageable) {
        QReview review = QReview.review;
        QProduct product = QProduct.product;
        QMember member = QMember.member;

        // content 조회
        List<Review> content = queryFactory
                .selectFrom(review)
                .join(review.product, product).fetchJoin()
                .join(review.member, member).fetchJoin()
                .where(
                        productIdEq(condition.getProductId()),
                        productNameContains(condition.getProductName()),
                        memberNicknameContains(condition.getMemberNickname()),
                        ratingGoe(condition.getMinRating()),
                        ratingLoe(condition.getMaxRating()),
                        deletedEq(condition.getDeleted()),
                        createdAtBetween(condition.getStartDate(), condition.getEndDate())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(review.id.desc())
                .fetch();

        // count 조회
        Long total = queryFactory
                .select(review.count())
                .from(review)
                .join(review.product, product)
                .join(review.member, member)
                .where(
                        productIdEq(condition.getProductId()),
                        productNameContains(condition.getProductName()),
                        memberNicknameContains(condition.getMemberNickname()),
                        ratingGoe(condition.getMinRating()),
                        ratingLoe(condition.getMaxRating()),
                        deletedEq(condition.getDeleted()),
                        createdAtBetween(condition.getStartDate(), condition.getEndDate())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<Review> findMyReviewsByPeriod(Long memberId,
                                              LocalDate startDate,
                                              LocalDate endDate,
                                              Pageable pageable) {

        QReview review = QReview.review;
        QProduct product = QProduct.product;

        // content 조회
        List<Review> content = queryFactory
                .selectFrom(review)
                .join(review.product, product).fetchJoin()
                .where(
                        review.member.id.eq(memberId),   // 내 리뷰만
                        review.deletedAt.isNull(),       // 삭제 안 된 것만
                        createdAtBetween(startDate, endDate) // 기간 필터
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(review.id.desc())
                .fetch();

        // count 조회
        Long total = queryFactory
                .select(review.count())
                .from(review)
                .join(review.product, product)
                .where(
                        review.member.id.eq(memberId),
                        review.deletedAt.isNull(),
                        createdAtBetween(startDate, endDate)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // ====== 조건 메서드들 ======

    private BooleanExpression productIdEq(Long productId) {
        return productId != null ? QReview.review.product.id.eq(productId) : null;
    }

    private BooleanExpression productNameContains(String productName) {
        return hasText(productName)
                ? QReview.review.product.name.containsIgnoreCase(productName)
                : null;
    }

    private BooleanExpression memberNicknameContains(String nickname) {
        return hasText(nickname)
                ? QReview.review.member.nickname.containsIgnoreCase(nickname)
                : null;
    }

    private BooleanExpression ratingGoe(Integer minRating) {
        return minRating != null
                ? QReview.review.rating.goe(minRating)
                : null;
    }

    private BooleanExpression ratingLoe(Integer maxRating) {
        return maxRating != null
                ? QReview.review.rating.loe(maxRating)
                : null;
    }

    /**
     * deleted == null  -> 삭제/미삭제 전체
     * deleted == true  -> deletedAt IS NOT NULL (소프트 삭제된 것만)
     * deleted == false -> deletedAt IS NULL (삭제 안 된 것만)
     */
    private BooleanExpression deletedEq(Boolean deleted) {
        if (deleted == null) {
            return null;
        }
        return deleted
                ? QReview.review.deletedAt.isNotNull()
                : QReview.review.deletedAt.isNull();
    }

    private BooleanExpression createdAtBetween(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return null;
        }

        if (start != null && end != null) {
            return QReview.review.createdAt.between(
                    start.atStartOfDay(),
                    end.plusDays(1).atStartOfDay()
            );
        } else if (start != null) {
            return QReview.review.createdAt.goe(start.atStartOfDay());
        } else { // end != null
            return QReview.review.createdAt.loe(end.plusDays(1).atStartOfDay());
        }
    }
}
