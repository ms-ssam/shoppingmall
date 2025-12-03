package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.product.dto.CreateProductRequest;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.dto.UpdateProductRequest;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import com.example.elicesecondproject.mall.global.service.ProductImageFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
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

    /**
     * [이미지 업로드 1] 기존 상품용 이미지 업로드
     * POST /api/admin/products/{productId}/images
     * - 용도: 상품 수정 화면에서 이미지 추가 시 사용
     */
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long productId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("target") ProductImageFileService.UploadTarget target
    ) throws IOException {
        validateAdminPermission(memberDetail);

        // 물리적 파일 저장 후 URL 반환
        List<String> imageUrls = productImageFileService.saveImages(productId, files, target);

        return ResponseEntity.ok(ApiResponse.success("이미지 업로드 성공", imageUrls));
    }

    /**
     * [이미지 업로드 2] 신규 상품 등록용 이미지 업로드
     * POST /api/admin/products/images
     * - 용도: 상품 등록 화면(ID가 아직 없음)에서 사용
     * - 내부적으로 ID 0번 폴더에 임시 저장
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> uploadNewProductImages(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("target") ProductImageFileService.UploadTarget target
    ) throws IOException {
        // ID 0번으로 처리
        return uploadImages(memberDetail, 0L, files, target);
    }



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

    // 상품 상세 수정 (Update)
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


    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long productId
    ) {
        validateAdminPermission(memberDetail);

        productService.deleteProduct(productId); // 서비스에 메서드 추가 필요

        return ResponseEntity.ok(ApiResponse.success("상품 판매 중지 처리 성공", null));
    }





    private void validateAdminPermission(MemberDetail memberDetail) {
        if (memberDetail == null || memberDetail.getMember() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (memberDetail.getMember().getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
