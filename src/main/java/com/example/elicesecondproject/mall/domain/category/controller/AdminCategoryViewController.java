package com.example.elicesecondproject.mall.domain.category.controller;

import com.example.elicesecondproject.mall.domain.category.dto.CategoryTreeResponse;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryViewController {

    private final CategoryService categoryService;

    // ✅관리자 카테고리 목록 조회(트리 구조로)
    @GetMapping
    public String categoryManage(Model model) {
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTreeForAdmin();

        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("pageTitle", "카테고리 관리");
        model.addAttribute("menu", "category");

        return "admin/category-manage";
    }
}