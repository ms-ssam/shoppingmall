package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

// * PROD-F-02: 카테고리별 상품 조회
    Page<ProductSummaryDto> findProductsByCategory(Long categoryId,
                                                   Boolean includeSubCategories,
                                                   ProductSortType sortType,
                                                   Pageable pageable);*/
}
