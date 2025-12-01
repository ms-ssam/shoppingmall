package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getProductsByCategory(
            @PathVariable @Min(1) Long categoryId,
            @RequestParam(defaultValue = "false") Boolean includeSubCategories,
            @RequestParam(defaultValue = "LATEST") ProductSortType sortType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductSummaryDto> products = productService.getProductsByCategory(
                categoryId,
                includeSubCategories,
                sortType,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success("카테고리별 상품 조회 성공", products)
        );
    }



}
