package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
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


    // [추가] 관리자용 - 전체 상품 조회 (STOP 포함)
    @Override
    public Page<ProductSummaryDto> findAllProductsForAdmin(Pageable pageable, ProductSortType sortType) {
        BooleanExpression whereClause = productNotDeletedForAdmin(); // ✅ 관리자용 조건
        List<ProductSummaryDto> content = fetchProducts(whereClause, sortType, pageable, null);
        Long total = countProducts(whereClause);
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // [추가] 관리자용 - 검색 (STOP 포함)
    @Override
    public Page<ProductSummaryDto> searchProductsForAdmin(String keyword, ProductSortType sortType, Pageable pageable) {
        BooleanExpression whereClause = productNotDeletedForAdmin().and(keywordCondition(keyword));
        List<ProductSummaryDto> content = fetchProducts(whereClause, sortType, pageable, null);
        Long total = countProducts(whereClause);
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }


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
    public Page<ProductSummaryDto> findAllProductsSummary(Pageable pageable, Long memberId, ProductSortType sortType) {
        BooleanExpression whereClause = productNotDeleted();
        // [수정] sortType 적용
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
                .orderBy(getOrderSpecifier(sortType), product.id.desc()) // Stable Sort
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

             case STOCK_HIGH -> product.totalStock.desc();
             case STOCK_LOW -> product.totalStock.asc();
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
        return queryFactory
                .select(product.count())
                .from(product)
                .where(whereClause)
                .fetchOne();
    }

    private BooleanExpression buildWhereClause(Long categoryId, Boolean includeSubCategories) {
        return productNotDeleted()
                .and(categoryCondition(categoryId, includeSubCategories));
    }

    private Expression<ProductSummaryDto> createProductSummaryProjection(Long memberId) {
        Expression<Boolean> isLikedExpr = (memberId == null) ? Expressions.asBoolean(false) :
                JPAExpressions.selectOne().from(wishList)
                        .where(wishList.member.id.eq(memberId), wishList.product.id.eq(product.id))
                        .exists();

        return Projections.constructor(
                ProductSummaryDto.class,
                product.id,
                product.name,
                product.price,
                calculateSalePrice(),
                product.discountRate,
                product.status,
                product.thumbnailUrl,
                product.averageRating,
                product.reviewCount,
                product.wishListCount,
                product.totalStock,
                isLikedExpr,
                product.category.name,
                product.createdAt
        );
    }

    private Expression<Integer> calculateSalePrice() {
        return product.price
                .subtract(product.price.multiply(product.discountRate).divide(DISCOUNT_DIVISOR))
                .intValue()
                .as(SALE_PRICE_ALIAS);
    }



    private BooleanExpression productNotDeleted() {
        return product.deletedAt.isNull()
                .and(product.status.ne(ProductStatus.STOP));  // STOP 상태 제외
    }


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
    // ✅ 관리자용 (STOP 포함)
    private BooleanExpression productNotDeletedForAdmin() {
        return product.deletedAt.isNull();
    }



}/*
        // 2차 정렬로 id DESC 한 번 더 넣어주면 정렬 안정적
        return switch (sortType) {
            case LATEST -> new OrderSpecifier[]{
                    product.createdAt.desc(),
                    product.id.desc()
            };
            case PRICE_HIGH -> new OrderSpecifier[]{
                    product.price.desc(),
                    product.id.desc()
            };
            case PRICE_LOW -> new OrderSpecifier[]{
                    product.price.asc(),
                    product.id.desc()
            };
            case REVIEW_COUNT ->  new OrderSpecifier[]{
                    product.reviewCount.desc(),
                    product.id.desc()
            };
            case WISHLIST_COUNT -> new OrderSpecifier[]{
                    product.WishListCount.desc(),
                    product.id.desc()
            };
            case RATING ->  new OrderSpecifier[]{
                    product.averageRating.desc(),
                    product.id.desc()
            };
        };*/