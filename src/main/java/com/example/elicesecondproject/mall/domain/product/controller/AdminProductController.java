package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.product.dto.CreateProductRequest;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.dto.UpdateProductRequest;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final PermissionValidator permissionValidator;

    // [관리자] 상품 등록 (파일 업로드 포함)
    // multipart/form-data 요청을 처리하기 위해 @RequestPart 사용
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // [변경] consumes 추가
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @AuthenticationPrincipal MemberDetail principal,
            @RequestPart("data") @Valid CreateProductRequest request, // [변경] @RequestBody -> @RequestPart
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage, // [추가]
            @RequestPart(value = "sliderImages", required = false) List<MultipartFile> sliderImages, // [추가]
            @RequestPart(value = "descImages", required = false) List<MultipartFile> descImages // [추가]
    ) {
        permissionValidator.validateAdminOnly(principal.getMember());

        // [변경] 호출 메서드 변경 (createProduct -> createProductWithFiles)
        ProductDetailResponse response = productService.createProductWithFiles(request, mainImage, sliderImages, descImages);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("상품 등록 성공", response));
    }

    // [관리자] 상품 수정 (파일 업로드 포함)
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @AuthenticationPrincipal MemberDetail principal,
            @PathVariable Long productId,
            @RequestPart("data") @Valid UpdateProductRequest request,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "sliderImages", required = false) List<MultipartFile> sliderImages,
            @RequestPart(value = "descImages", required = false) List<MultipartFile> descImages
    ) {
        permissionValidator.validateAdminOnly(principal.getMember());

        ProductDetailResponse response = productService.updateProductWithFiles(productId, request, mainImage, sliderImages, descImages);

        return ResponseEntity.ok(ApiResponse.success("상품 수정 성공", response));
    }

    // [관리자] 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal MemberDetail principal,
            @PathVariable Long productId
    ) {
        permissionValidator.validateAdminOnly(principal.getMember());
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("상품 판매 중지 처리 성공", null));
    }

    // [관리자] 상품의 모든 이미지 조회
    @GetMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getAllImages(
            @AuthenticationPrincipal MemberDetail principal,
            @PathVariable @Min(1) Long productId) {

        permissionValidator.validateAdminOnly(principal.getMember());
        List<ProductImageDto> images = productService.getAllImages(productId);
        return ResponseEntity.ok(ApiResponse.success("이미지 목록 조회 성공", images));
    }





}