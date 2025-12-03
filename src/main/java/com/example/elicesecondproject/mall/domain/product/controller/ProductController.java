package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.product.dto.*;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // [사용자] 카테고리별 상품 조회
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

    // [사용자] 전체 상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> getAllProducts(
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<ProductSummaryDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("상품 목록 조회 성공", products));
    }

    // [사용자] 상품 상세 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable Long productId) {

        ProductDetailResponse res = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품 상세 조회 성공", res));
    }

    // [사용자] 키워드 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "LATEST") ProductSortType sortType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductSummaryDto> products = productService.searchProducts(keyword, sortType, pageable);
        return ResponseEntity.ok(ApiResponse.success("상품 검색 성공", products));
    }

    // [사용자] 찜 상태 조회
    @GetMapping("/{productId}/wishlist")
    public ResponseEntity<ApiResponse<Boolean>> getIsInWishList(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long productId) {

        boolean isWished = false;

        if (memberDetail != null) {
            Long memberId = memberDetail.getMember().getId();
            isWished = productService.isInWishList(memberId, productId);
        }

        return ResponseEntity.ok(ApiResponse.success("찜 상태 조회 성공", isWished));
    }

    @PostMapping("/{productId}/wishList")
    public ResponseEntity<ApiResponse<WishListToggleResponseDto>> addWishList(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long productId) {
        Long memberId = memberDetail.getMember().getId();
        WishListToggleResponseDto result = productService.addWish(memberId, productId);

        return ResponseEntity.ok(ApiResponse.success("찜 추가 완료", result));
    }

    @DeleteMapping("/{productId}/wish")
    public ResponseEntity<ApiResponse<WishListToggleResponseDto>> removeWish(
            @PathVariable Long productId,
            @AuthenticationPrincipal MemberDetail memberDetails
    ) {
        Long memberId = memberDetails.getMember().getId();
        WishListToggleResponseDto result = productService.removeWish(memberId, productId);
        return ResponseEntity.ok(ApiResponse.success("찜 제거 완료", result));
    }
}
