package com.example.elicesecondproject.mall.domain.product.controller;

import com.example.elicesecondproject.mall.domain.product.dto.ProductDetailResponse;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.domain.review.dto.response.ReviewResponse;
import com.example.elicesecondproject.mall.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductViewController {
    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping("/{productId}")
    public String getProduct(@PathVariable Long productId,
                             @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
                             Pageable pageable,
                             Model model) {

        ProductDetailResponse res = productService.getProduct(productId);
        Page<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId, pageable);

        model.addAttribute("product", res);
        model.addAttribute("reviews", reviews);

        return "product/product-detail";   // 파일 이름
    }
}
