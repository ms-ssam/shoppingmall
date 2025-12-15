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

        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchProductsForAdmin(keyword, sortType, pageable);
        } else {
            products = productService.getAllProductsForAdmin(pageable, sortType);
        }

        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "상품 관리");
        model.addAttribute("menu", "product");
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortType", sortType);

        return "admin/product/product-manage";
    }



    @GetMapping("/new")
    public String createProductForm(Model model) {
        // 카테고리 선택 모달을 위해 트리 정보 필요
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();

        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("pageTitle", "상품 등록");
        model.addAttribute("menu", "product");

        return "admin/product/product-form";
    }


    @GetMapping("/{id}/edit")
    public String updateProductForm(@PathVariable Long id, Model model) {
        // 기존 상품 정보 조회
        ProductDetailResponse product = productService.getProduct(id, null);
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();

        model.addAttribute("product", product);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("images", product.getImages());
        model.addAttribute("pageTitle", "상품 수정");
        model.addAttribute("menu", "product");

        return "admin/product/product-form";
    }
}