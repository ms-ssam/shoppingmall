package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.product.dto.*;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import com.example.elicesecondproject.mall.global.service.ProductImageFileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductImageFileService productImageFileService;

    // [관리자] 기존 상품용 이미지 업로드
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long productId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("target") ProductImageFileService.UploadTarget target
    ) throws IOException {

        validateAdminPermission(memberDetail);

        List<String> imageUrls = productImageFileService.saveImages(productId, files, target);

        return ResponseEntity.ok(ApiResponse.success("이미지 업로드 성공", imageUrls));
    }

    // [관리자] 신규 상품 등록용 이미지 업로드
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> uploadNewProductImages(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("target") ProductImageFileService.UploadTarget target
    ) throws IOException {

        validateAdminPermission(memberDetail);

        // ID 0번으로 처리
        List<String> imageUrls = productImageFileService.saveImages(0L, files, target);

        return ResponseEntity.ok(ApiResponse.success("이미지 업로드 성공", imageUrls));
    }

    // [관리자] 상품의 모든 이미지 조회
    @GetMapping("/{productId}/images")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getAllImages(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable @Min(1) Long productId) {

        validateAdminPermission(memberDetail);

        List<ProductImageDto> images = productService.getAllImages(productId);
        return ResponseEntity.ok(ApiResponse.success("이미지 목록 조회 성공", images));
    }

    // [관리자] 상품 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestBody @Valid CreateProductRequest request
    ) {
        validateAdminPermission(memberDetail);

        ProductDetailResponse response = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("상품 등록 성공", response));
    }

    // [관리자] 상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        validateAdminPermission(memberDetail);

        ProductDetailResponse response = productService.updateProduct(productId, request);

        return ResponseEntity.ok(ApiResponse.success("상품 수정 성공", response));
    }

    // [관리자] 상품 삭제(판매 중지)
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long productId
    ) {
        validateAdminPermission(memberDetail);

        productService.deleteProduct(productId);

        return ResponseEntity.ok(ApiResponse.success("상품 판매 중지 처리 성공", null));
    }

    // 관리자 권한 검증
    private void validateAdminPermission(MemberDetail memberDetail) {
        if (memberDetail == null || memberDetail.getMember() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (memberDetail.getMember().getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
