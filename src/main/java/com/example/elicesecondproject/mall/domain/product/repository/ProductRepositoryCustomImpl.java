package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.elicesecondproject.mall.domain.category.entity.QCategory.category;
import static com.example.elicesecondproject.mall.domain.product.entity.QProduct.product;
import static com.example.elicesecondproject.mall.domain.product.entity.QProductImage.productImage;
import static com.example.elicesecondproject.mall.domain.product.entity.QWishList.wishList;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private static final int DISCOUNT_DIVISOR = 100;
    private static final String SALE_PRICE_ALIAS = "salePrice";

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSummaryDto> findProductsByCategory(
            Long categoryId,
            Boolean includeSubCategories,
            ProductSortType sortType,
            Pageable pageable,
            Long memberId) {

        BooleanExpression whereClause = buildWhereClause(categoryId, includeSubCategories);
        List<ProductSummaryDto> content = fetchProducts(whereClause, sortType, pageable, memberId);
        Long total = countProducts(whereClause);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<ProductSummaryDto> searchProducts(String keyword, ProductSortType sortType, Pageable pageable) {
        BooleanExpression whereClause = productNotDeleted().and(keywordCondition(keyword));
        List<ProductSummaryDto> content = fetchProducts(whereClause, sortType, pageable, null);
        Long total = countProducts(whereClause);
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<ProductSummaryDto> findAllProductsSummary(Pageable pageable, Long memberId, ProductSortType sortType) { // [수정] sortType 파라미터 추가
        BooleanExpression whereClause = productNotDeleted();

        // [수정] 고정값 LATEST 대신 전달받은 sortType 사용
        List<ProductSummaryDto> content = fetchProducts(whereClause, sortType, pageable, memberId);
        Long total = countProducts(whereClause);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private List<ProductSummaryDto> fetchProducts(
            BooleanExpression whereClause,
            ProductSortType sortType,
            Pageable pageable,
            Long memberId) {

        return queryFactory
                .select(createProductSummaryProjection(memberId))
                .from(product)
                .where(whereClause)
                // 2차 정렬 조건 (ID 역순) 추가하여 정렬 순서 보장
                .orderBy(getOrderSpecifier(sortType), product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
        if (sortType == null) return product.createdAt.desc();

        return switch (sortType) {
            case PRICE_HIGH -> product.price.desc();
            case PRICE_LOW -> product.price.asc();
            case REVIEW_COUNT -> product.reviewCount.desc();
            case WISHLIST_COUNT -> product.wishListCount.desc();
            case RATING -> product.averageRating.desc();
            default -> product.createdAt.desc();
        };
    }

    private BooleanExpression keywordCondition(String keyword) {
        String[] terms = keyword.split("\\s+");
        BooleanExpression condition = null;
        for (String term : terms) {
            BooleanExpression termExpr = product.name.containsIgnoreCase(term)
                    .or(product.description.containsIgnoreCase(term))
                    .or(product.category.name.containsIgnoreCase(term));
            condition = (condition == null) ? termExpr : condition.and(termExpr);
        }
        return condition;
    }

    private Long countProducts(BooleanExpression whereClause) {
        return queryFactory.select(product.count()).from(product).where(whereClause).fetchOne();
    }

    private BooleanExpression buildWhereClause(Long categoryId, Boolean includeSubCategories) {
        return productNotDeleted().and(categoryCondition(categoryId, includeSubCategories));
    }

    private Expression<ProductSummaryDto> createProductSummaryProjection(Long memberId) {
        Expression<Boolean> isLikedExpr = (memberId == null) ? Expressions.asBoolean(false) :
                JPAExpressions.selectOne().from(wishList)
                        .where(wishList.member.id.eq(memberId), wishList.product.id.eq(product.id))
                        .exists();

        return Projections.constructor(ProductSummaryDto.class,
                product.id, product.name, product.price, calculateSalePrice(), product.discountRate,
                product.status, selectMainImage(), product.averageRating, product.reviewCount,
                product.wishListCount, product.totalStock, isLikedExpr);
    }

    private Expression<Integer> calculateSalePrice() {
        return product.price.subtract(product.price.multiply(product.discountRate).divide(DISCOUNT_DIVISOR)).intValue().as(SALE_PRICE_ALIAS);
    }

    private Expression<String> selectMainImage() {
        return JPAExpressions.select(productImage.imageUrl).from(productImage)
                .where(productImage.product.eq(product), productImage.imageType.eq(ImageType.MAIN), productImage.deletedAt.isNull())
                .orderBy(productImage.displayOrder.asc()).limit(1);
    }

    private BooleanExpression productNotDeleted() { return product.deletedAt.isNull(); }

    private BooleanExpression categoryCondition(Long categoryId, Boolean includeSubCategories) {
        if (categoryId == null) return null;
        if (Boolean.TRUE.equals(includeSubCategories)) return categoryIdEq(categoryId).or(parentCategoryIdEq(categoryId));
        return categoryIdEq(categoryId);
    }

    private BooleanExpression categoryIdEq(Long categoryId) { return product.category.id.eq(categoryId); }

    private BooleanExpression parentCategoryIdEq(Long parentCategoryId) {
        return JPAExpressions.selectOne().from(category)
                .where(category.id.eq(product.category.id), category.parent.id.eq(parentCategoryId))
                .exists();
    }
}