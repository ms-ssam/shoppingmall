package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<ProductSummaryDto> searchProducts(String keyword, ProductSortType sortType, Pageable pageable);

    // 카테고리별 상품 조회
    Page<ProductSummaryDto> findProductsByCategory(Long categoryId, Boolean includeSubCategories, ProductSortType sortType, Pageable pageable, Long memberId);

    // [수정] 전체 상품 조회 (sortType 추가)
    Page<ProductSummaryDto> findAllProductsSummary(Pageable pageable, Long memberId, ProductSortType sortType);


    // [추가] 관리자용 - 전체 상품 조회 (STOP 상태 포함)
    Page<ProductSummaryDto> findAllProductsForAdmin(Pageable pageable, ProductSortType sortType);

    // [추가] 관리자용 - 검색 (STOP 상태 포함)
    Page<ProductSummaryDto> searchProductsForAdmin(String keyword, ProductSortType sortType, Pageable pageable);
}


