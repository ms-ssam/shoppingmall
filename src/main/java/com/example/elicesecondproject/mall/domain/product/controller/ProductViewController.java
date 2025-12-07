package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.cart.dto.request.AddCartItemRequest;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.category.dto.CategoryTreeResponse;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final CartService cartService;

    /**
     * 상품 목록 페이지 (전체 / 카테고리 / 검색 통합)
     */
    @GetMapping
    public String productList(
            @RequestParam(required = false) String keyword, // [추가] 검색어
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "LATEST") ProductSortType sortType,
            @PageableDefault(size = 12) Pageable pageable,
            @AuthenticationPrincipal MemberDetail memberDetail,
            Model model
    ) {
        Long memberId = (memberDetail != null) ? memberDetail.getMember().getId() : null;
        Page<ProductSummaryDto> products;

        // [로직 분기]
        if (keyword != null && !keyword.isBlank()) {
            // 1. 검색어가 있으면 검색 서비스 호출
            products = productService.searchProducts(keyword, sortType, pageable);
        } else if (categoryId != null) {
            // 2. 카테고리가 있으면 카테고리별 조회
            products = productService.getProductsByCategory(categoryId, true, sortType, pageable, memberId);
        } else {
            // 3. 둘 다 없으면 전체 조회
            products = productService.getAllProducts(pageable, memberId, sortType);
        }

        // 카테고리 네비게이션 데이터
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();
        List<CategoryTreeResponse> subCategories = null;

        // 카테고리 선택 시 서브 카테고리 찾기 로직 (기존 유지)
        if (categoryId != null) {
            for (CategoryTreeResponse root : categoryTree) {
                if (root.getId().equals(categoryId)) {
                    subCategories = root.getChildren();
                    break;
                }
                if (root.getChildren() != null) {
                    for (CategoryTreeResponse child : root.getChildren()) {
                        if (child.getId().equals(categoryId)) {
                            subCategories = root.getChildren();
                            break;
                        }
                    }
                }
                if (subCategories != null) break;
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortType", sortType);
        model.addAttribute("keyword", keyword); // [추가] 검색어 유지용

        return "product/list";
    }

    /**
     * 상품 상세 페이지
     */
    @GetMapping("/{productId}")
    public String productDetail(@PathVariable Long productId, Model model, @AuthenticationPrincipal MemberDetail memberDetail, Pageable pageable) {
        Long memberId = (memberDetail != null) ? memberDetail.getMember().getId() : null;
        ProductDetailResponse product = productService.getProduct(productId, memberId);
        model.addAttribute("product", product);
        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);
        model.addAttribute("reviews", reviews);
        return "product/detail";
    }

    // 장바구니에 상품 추가
    @PostMapping("/{productId}/cart")
    public String addCartItem(@PathVariable Long productId,
                              @AuthenticationPrincipal MemberDetail memberDetail,
                              @Valid @ModelAttribute AddCartItemRequest request,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("cartError", "장바구니 추가 정보가 올바르지 않습니다.");
            // 보고있던 상품 상세페이지로
            return "redirect:/products/" + productId;
        }

        cartService.addItemToCart(memberDetail.getMember().getId(), request);

        redirectAttributes.addFlashAttribute("cartSuccess", "장바구니에 상품이 추가되었습니다.");
        return "redirect:/products/" + productId;
    }
}