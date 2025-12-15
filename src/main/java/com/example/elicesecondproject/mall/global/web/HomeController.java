package com.example.elicesecondproject.mall.global.web;

import com.example.elicesecondproject.mall.domain.product.dto.ProductSummaryDto;
import com.example.elicesecondproject.mall.domain.product.service.ProductService;
import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductService productService;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal MemberDetail memberDetail,
            Model model) {
        Long memberId = memberDetail != null ? memberDetail.getMember().getId() : null;

        Pageable pageable = PageRequest.of(0, 8);

        Page<ProductSummaryDto> products = productService.getAllProducts(pageable, memberId, null);

//        List<ProductSummaryDto> products = page.getContent();

        model.addAttribute("products", products);

        return "home";
    }
}
