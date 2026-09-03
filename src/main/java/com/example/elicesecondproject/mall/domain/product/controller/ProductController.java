package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
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