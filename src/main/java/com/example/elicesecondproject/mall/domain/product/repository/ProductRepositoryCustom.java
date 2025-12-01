package com.example.elicesecondproject.mall.domain.product.repository;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {


    Page<ProductSummaryDto> findProductsByCategory(Long categoryId,
                                                   ProductSortType sortType,
                                                   Pageable pageable);
}
