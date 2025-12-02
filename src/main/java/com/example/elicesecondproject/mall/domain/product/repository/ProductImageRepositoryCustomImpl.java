package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.elicesecondproject.mall.domain.product.entity.QProductImage.productImage;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryCustomImpl implements ProductImageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 슬라이더 이미지 조회 (MAIN + SLIDER)
     *
     * 정렬 순서:
     * 1. MAIN 먼저 (imageType = 'MAIN')
     * 2. SLIDER 다음 (displayOrder ASC)
     */
    @Override
    public List<ProductImage> findSliderImagesByProductId(Long productId) {
        return queryFactory
                .selectFrom(productImage)
                .where(
                        productImage.product.id.eq(productId),
                        productImage.imageType.in(ImageType.MAIN, ImageType.SLIDER),
                        productImage.deletedAt.isNull()
                )
                .orderBy(
                        // MAIN이면 0, SLIDER면 1 → MAIN이 먼저 옴
                        new CaseBuilder()
                                .when(productImage.imageType.eq(ImageType.MAIN)).then(0)
                                .otherwise(1)
                                .asc(),
                        // displayOrder 오름차순
                        productImage.displayOrder.asc()
                )
                .fetch();
    }



    /**
     * 상품의 모든 이미지 Soft Delete
     *
     * @param productId 상품 ID
     * @return 삭제된 이미지 개수
     */
    @Override
    public long softDeleteByProductId(Long productId) {
        return queryFactory
                .update(productImage)
                .set(productImage.deletedAt, LocalDateTime.now())
                .where(
                        productImage.product.id.eq(productId),
                        productImage.deletedAt.isNull()
                )
                .execute();
    }
}
