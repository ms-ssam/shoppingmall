package com.example.elicesecondproject.mall.domain.category.controller;

import com.example.elicesecondproject.mall.domain.category.dto.CategoryResponse;
import com.example.elicesecondproject.mall.domain.category.dto.CreateCategoryRequest;
import com.example.elicesecondproject.mall.domain.category.dto.UpdateCategoryRequest;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.global.common.PermissionValidator;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final PermissionValidator permissionValidator;

    // [수정]뷰 컨트롤러에서 Thymeleaf로 전달
    /*
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategoriesForAdmin(
            @AuthenticationPrincipal MemberDetail memberDetail) {
        permissionValidator.validateAdminOnly(memberDetail.getMember());
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("카테고리 목록 조회 성공", categories));
    }
    */

    // [관리자] 카테고리 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @RequestBody @Valid CreateCategoryRequest request) {

        permissionValidator.validateAdminOnly(memberDetail.getMember());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리 생성 성공", response));
    }

    // [관리자] 카테고리 수정
    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long categoryId,
            @RequestBody @Valid UpdateCategoryRequest request) {

        permissionValidator.validateAdminOnly(memberDetail.getMember());
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success("카테고리 수정 성공", response));
    }

    // [관리자] 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long categoryId) {

        permissionValidator.validateAdminOnly(memberDetail.getMember());
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("카테고리 삭제 성공", null));
    }

    // [관리자] 카테고리 노출 여부 토글
    @PatchMapping("/{categoryId}/visibility")
    public ResponseEntity<ApiResponse<Void>> toggleCategoryVisibility(
            @AuthenticationPrincipal MemberDetail memberDetail,
            @PathVariable Long categoryId) {

        permissionValidator.validateAdminOnly(memberDetail.getMember());
        categoryService.toggleVisibility(categoryId);
        return ResponseEntity.ok(ApiResponse.success("카테고리 노출 상태 변경 성공", null));
    }

    /* ✅ 제거 (PermissionValidator로 대체)
    private void validateAdminPermission(MemberDetail memberDetail) {
        if (memberDetail == null || memberDetail.getMember() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (memberDetail.getMember().getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
    */
}
