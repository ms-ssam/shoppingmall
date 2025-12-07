package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.category.dto.CategoryTreeResponse;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductViewController {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * [관리자] 상품 목록 페이지
     * URL: /admin/products?page=0&keyword=...
     */
    @GetMapping
    public String productManage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") ProductSortType sortType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductSummaryDto> products;

        // 검색어가 있으면 검색, 없으면 전체 조회
        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchProducts(keyword, sortType, pageable);
        } else {
            // 관리자용 전체 조회 (일단 기존 메서드 재사용, 추후 관리자 전용 메서드로 분리 가능)
            // 관리자는 본인 ID와 상관없이 모든 상품을 관리하므로 memberId는 null 처리
            products = productService.getAllProducts(pageable, null, sortType);
        }

        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "상품 관리");
        model.addAttribute("menu", "product"); // 사이드바 활성화용

        // 검색 상태 유지를 위해 모델에 담음
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortType", sortType);

        return "admin/product-manage";
    }

    /**
     * [관리자] 상품 등록 페이지
     * URL: /admin/products/new
     */
    @GetMapping("/new")
    public String createProductForm(Model model) {
        // 카테고리 선택 모달을 위해 트리 정보 필요
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();

        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("pageTitle", "상품 등록");
        model.addAttribute("menu", "product");

        return "admin/product-form";
    }

    /**
     * [관리자] 상품 수정 페이지
     * URL: /admin/products/{id}/edit
     */
    @GetMapping("/{id}/edit")
    public String updateProductForm(@PathVariable Long id, Model model) {
        // 기존 상품 정보 조회
        ProductDetailResponse product = productService.getProduct(id, null);
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();

        model.addAttribute("product", product);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("pageTitle", "상품 수정");
        model.addAttribute("menu", "product");

        return "admin/product-form";
    }
}