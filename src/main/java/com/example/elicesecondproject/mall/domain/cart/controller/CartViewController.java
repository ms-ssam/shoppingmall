package com.example.elicesecondproject.mall.domain.cart.controller;

import com.example.elicesecondproject.mall.domain.cart.dto.response.CartInfoResponseDto;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

    @DeleteMapping("/items/{cartItemId}")
    public String deleteCartItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal MemberDetail memberDetail,
            RedirectAttributes redirectAttributes) {
        Long memberId = memberDetail.getMember().getId();
        try{
            cartService.deleteCartItem(memberId, cartItemId);
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/cart";
    }

    @DeleteMapping("/items")
    public String deleteSelectedCartItems(
            @RequestParam(value = "cartItemIds", required = false) List<Long> cartItemIds,
            @AuthenticationPrincipal MemberDetail memberDetail,
            RedirectAttributes redirectAttributes) {
        if(cartItemIds == null || cartItemIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "선택된 상품이 없습니다.");
            return "redirect:/cart";
        }

        Long memberId = memberDetail.getMember().getId();
        cartService.deleteSelectedCartItems(memberId, cartItemIds);

        return "redirect:/cart";
    }
}
