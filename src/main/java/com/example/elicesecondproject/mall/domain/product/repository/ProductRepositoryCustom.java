package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<ProductSummaryDto> searchProducts(String keyword,
                                           ProductSortType sortType,
                                           Pageable pageable);

// * PROD-F-02: 카테고리별 상품 조회
    Page<ProductSummaryDto> findProductsByCategory(Long categoryId,
                                                   Boolean includeSubCategories,
                                                   ProductSortType sortType,
                                                   Pageable pageable);
}
