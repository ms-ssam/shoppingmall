package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
            Pageable pageable) {

        log.debug("카테고리별 상품 조회 - categoryId: {}, includeSubCategories: {}, sortType: {}",
                categoryId, includeSubCategories, sortType);

        BooleanExpression whereClause = buildWhereClause(categoryId, includeSubCategories);

        List<ProductSummaryDto> content = fetchProducts(whereClause, sortType, pageable);
        Long total = countProducts(whereClause);

        log.debug("조회 완료 - total: {}, fetched: {}", total, content.size());

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private List<ProductSummaryDto> fetchProducts(
            BooleanExpression whereClause,
            ProductSortType sortType,
            Pageable pageable) {

        return queryFactory
                .select(createProductSummaryProjection())
                .from(product)
                .where(whereClause)
                .orderBy(getOrderSpecifier(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
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

    private Expression<ProductSummaryDto> createProductSummaryProjection() {
        return Projections.constructor(
                ProductSummaryDto.class,
                product.id,
                product.name,
                product.price,
                calculateSalePrice(),
                product.discountRate,
                product.status,
                selectMainImage(),
                product.averageRating,
                product.reviewCount,
                product.wishListCount
        );
    }

    private Expression<Integer> calculateSalePrice() {
        return product.price
                .subtract(product.price.multiply(product.discountRate).divide(DISCOUNT_DIVISOR))
                .intValue()
                .as(SALE_PRICE_ALIAS);
    }

    private Expression<String> selectMainImage() {
        return JPAExpressions
                .select(productImage.imageUrl)
                .from(productImage)
                .where(
                        productImage.product.eq(product),
                        productImage.imageType.eq(ImageType.MAIN),
                        productImage.deletedAt.isNull()
                )
                .orderBy(productImage.displayOrder.asc())
                .limit(1);
    }

    private BooleanExpression productNotDeleted() {
        return product.deletedAt.isNull();
    }

    private BooleanExpression categoryCondition(Long categoryId, Boolean includeSubCategories) {
        if (categoryId == null) {
            return null;
        }

        if (Boolean.TRUE.equals(includeSubCategories)) {
            return categoryIdEq(categoryId)
                    .or(parentCategoryIdEq(categoryId));
        }

        return categoryIdEq(categoryId);
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return product.category.id.eq(categoryId);
    }

    private BooleanExpression parentCategoryIdEq(Long parentCategoryId) {
        return JPAExpressions
                .selectOne()
                .from(category)
                .where(
                        category.id.eq(product.category.id),
                        category.parent.id.eq(parentCategoryId)
                )
                .exists();
    }

    private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
        if (sortType == null) {
            sortType = ProductSortType.LATEST;
        }

        return switch (sortType) {
            case LATEST -> product.createdAt.desc();
            case PRICE_HIGH -> product.price.desc();
            case PRICE_LOW -> product.price.asc();
            case REVIEW_COUNT -> product.reviewCount.desc();
            case WISHLIST_COUNT -> product.wishListCount.desc();
            case RATING -> product.averageRating.desc();
        };
    }
}
