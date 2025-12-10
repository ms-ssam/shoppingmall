package com.example.elicesecondproject.mall.global.web;

import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {
    private final CartService cartService;

    @ModelAttribute("cartCount")
    public int cartCount(@AuthenticationPrincipal MemberDetail memberDetail) {
        // 비로그인(anonymous)일 때
        if (memberDetail == null) {
            return 0;
        }

        Long memberId = memberDetail.getMember().getId();

        return cartService.getCartCount(memberId);
    }
}
