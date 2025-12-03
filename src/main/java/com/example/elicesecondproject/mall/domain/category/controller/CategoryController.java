package com.example.elicesecondproject.mall.domain.category.controller;

import com.example.elicesecondproject.mall.domain.category.dto.CategoryTreeResponse;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // [사용자] 카테고리 트리 구조로 조회
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        List<CategoryTreeResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success("카테고리 트리 조회 성공", tree));
    }
}
