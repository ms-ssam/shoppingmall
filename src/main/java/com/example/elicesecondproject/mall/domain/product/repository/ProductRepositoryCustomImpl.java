package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSummaryDto> searchProducts(String keyword, ProductSortType sortType, Pageable pageable) {
        return null;
    }

    @Override
    public Page<ProductSummaryDto> findProductsByCategoty(Long categoryId, ProductSortType sortType, Pageable pageable) {
        return null;
    }


}
