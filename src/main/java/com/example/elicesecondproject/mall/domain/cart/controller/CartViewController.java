package com.example.elicesecondproject.mall.domain.cart.controller;

import com.example.elicesecondproject.mall.domain.cart.dto.response.CartInfoResponseDto;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/cart")
@Controller
public class CartViewController {
    private final CartService cartService;

    @GetMapping
    public String showCartPage(Model model, @AuthenticationPrincipal MemberDetail memberDetail) {
        Long memberId = memberDetail.getMember().getId();

        CartInfoResponseDto cartInfo = cartService.getCartInfo(memberId);

        model.addAttribute("cartInfo", cartInfo);

        model.addAttribute("cartCount", cartInfo.getTotalCount());

        return "cart/cart";  // src/main/resources/templates/cart/cart.html
    }
}
