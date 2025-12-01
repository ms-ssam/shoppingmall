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
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom{
    private final JPAQueryFactory queryFactory;



    @Override
    public Page<ProductSummaryDto> findProductsByCategory(
            Long categoryId,
            ProductSortType sortType,
            Pageable pageable) {

        log.debug("### categoryId: {}, sortType: {}", categoryId, sortType);
        log.debug("### pageable: {}", pageable);

        List<ProductSummaryDto> content = queryFactory
                .select(createProductSummaryProjection())
                .from(product)
                .leftJoin(product.category, category)
                .where(
                        productNotDeleted(),
                        categoryEq(categoryId)
                )
                .orderBy(getOrderSpecifier(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        productNotDeleted(),
                        categoryEq(categoryId)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private Expression<ProductSummaryDto> createProductSummaryProjection() {
        return Projections.constructor(ProductSummaryDto.class,
                product.id,
                product.name,
                product.price,
                // 할인가 계산
                product.price.subtract(
                        product.price.multiply(product.discountRate).divide(100)
                ).intValue().as("salePrice"),
                product.discountRate,
                product.status,
                // 메인 이미지 SubQuery
                JPAExpressions
                        .select(productImage.imageUrl)
                        .from(productImage)
                        .where(
                                productImage.product.eq(product),
                                productImage.imageType.eq(ImageType.MAIN),
                                productImage.deletedAt.isNull()
                        )
                        .orderBy(productImage.displayOrder.asc())
                        .limit(1),
                product.averageRating,
                product.reviewCount,
                product.WishListCount
        );
    }

    private BooleanExpression productNotDeleted() {
        return product.deletedAt.isNull();
    }
    private BooleanExpression categoryEq(Long categoryId) {
        return categoryId != null ? product.category.id.eq(categoryId) : null;
    }
    private OrderSpecifier<?> getOrderSpecifier(ProductSortType sortType) {
        if (sortType == null) {
            return product.createdAt.desc();
        }

        return switch (sortType) {
            case LATEST -> product.createdAt.desc();
            case PRICE_HIGH -> product.price.desc();
            case PRICE_LOW -> product.price.asc();
            case REVIEW_COUNT -> product.reviewCount.desc();
            case WISHLIST_COUNT -> product.WishListCount.desc();
            case RATING -> product.averageRating.desc();
        };
    }

}
