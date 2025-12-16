package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.cart.dto.request.AddCartItemRequest;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.category.dto.CategoryTreeResponse;
import com.example.elicesecondproject.mall.domain.category.service.CategoryService;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSortType;
import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.domain.qna.dto.response.ProductQuestionResponse;
import com.example.elicesecondproject.mall.domain.qna.service.QuestionService;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewService;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final QuestionService questionService;


    @GetMapping
    public String productList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "LATEST") ProductSortType sortType,
            @PageableDefault(size = 12) Pageable pageable,
            @AuthenticationPrincipal MemberDetail memberDetail,
            Model model
    ) {
        // 1. 회원 ID 추출
        Long memberId = (memberDetail != null) ? memberDetail.getMember().getId() : null;

        // 2. 상품 목록 조회
        Page<ProductSummaryDto> products = productService.getProductList(
                keyword, categoryId, sortType, pageable, memberId
        );

        // 3. 카테고리 네비게이션 데이터 조회
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();
        List<CategoryTreeResponse> subCategories = categoryService.getSubCategories(categoryId);

        model.addAttribute("products", products);
        model.addAttribute("categoryTree", categoryTree);
        model.addAttribute("subCategories", subCategories);

        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortType", sortType);
        model.addAttribute("keyword", keyword);

        return "product/list";
    }

    @GetMapping("/{productId}")
    public String productDetail(@PathVariable Long productId, Model model, @AuthenticationPrincipal MemberDetail memberDetail, Pageable pageable) {
        Member member = (memberDetail != null) ? memberDetail.getMember() : null;
        Long memberId = (memberDetail != null) ? memberDetail.getMember().getId() : null;
        ProductDetailResponse product = productService.getProduct(productId, memberId);
        model.addAttribute("product", product);
        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);
        model.addAttribute("reviews", reviews);
        Page<ProductQuestionResponse> questions = questionService.getQuestionsByProduct(productId, member, pageable);
        model.addAttribute("questions", questions);
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

        try{
            cartService.addItemToCart(memberDetail.getMember().getId(), request);
        } catch (BusinessException e){
            redirectAttributes.addFlashAttribute("cartError", e.getErrorCode().getMessage());
            return "redirect:/products/" + productId;
        }
        redirectAttributes.addFlashAttribute("cartSuccess", "장바구니에 상품이 추가되었습니다.");
        return "redirect:/products/" + productId + "?addedToCart=true";
    }

    // 구매하기 버튼으로 장바구니에 상품 추가 후 장바구니로 이동
    @PostMapping("/{productId}/purchase")
    public String buyProduct(@PathVariable Long productId,
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

        return "redirect:/cart";
    }
}