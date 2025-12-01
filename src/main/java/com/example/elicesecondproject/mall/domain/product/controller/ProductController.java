package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

//@RestController("/api")
//@RequiredArgsConstructor
//public class ProductController {
//
//    private final ProductService productService;
//
//    @GetMapping("/products")
//    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getAllProducts(
//            @PageableDefault(
//                    size = 20,
//                    sort = "id",
//                    direction = Sort.Direction.DESC
//            ) Pageable pageable) {
//        Page<ProductSummaryDto> products = productService.getProducts(pageable);
//        return ResponseEntity.ok(ApiResponse.success(products));
//    }
//}
