package com.example.elicesecondproject.mall.domain.cart.controller;

import com.example.elicesecondproject.mall.domain.cart.dto.request.CartItemOptionModifyRequest;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartItemEditPopupResponse;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartInfoResponseDto;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/cart")
@Controller
public class CartViewController {
    private final CartService cartService;

//    public String getCartPage(Model model, @AuthenticationPrincipal MemberDetail memberDetail) {
//        Long memberId = memberDetail.getMember().getId();
//
//
//    }

    @GetMapping("/{cartItemId}/option")
    public String cartItemEditPopup(@PathVariable Long cartItemId, Model model) {
        CartItemEditPopupResponse item = cartService.getCartItemEditPopup(cartItemId);
        model.addAttribute("cartItem", item);
        model.addAttribute("cartItemId", cartItemId);
        return "cart/cart-item-edit-popup";
    }

    @PutMapping("/{cartItemId}/option")
    public String updateCartItem(@PathVariable Long cartItemId,
                                 @ModelAttribute @Valid CartItemOptionModifyRequest request,
                                 BindingResult bindingResult,
                                 Model model) {

        // 검증 오류 → 팝업 유지
        if (bindingResult.hasErrors()) {
            CartItemEditPopupResponse popupData = cartService.getCartItemEditPopup(cartItemId);
            model.addAttribute("cartItem", popupData);
            model.addAttribute("cartItemId", cartItemId);
            return "cart/cart-item-edit-popup";
        }

        try {
            cartService.updateCartItemOption(cartItemId, request);
        } catch (BusinessException e) {
            // 비즈니스 오류 → 팝업 유지
            CartItemEditPopupResponse popupData = cartService.getCartItemEditPopup(cartItemId);
            model.addAttribute("cartItem", popupData);
            model.addAttribute("cartItemId", cartItemId);
            model.addAttribute("errorMessage", e.getMessage());
            return "cart/cart-item-edit-popup";   // 팝업 유지
        }

        return "redirect:/cart";
    }
    @GetMapping
    public String showCartPage(Model model, @AuthenticationPrincipal MemberDetail memberDetail) {
        Long memberId = memberDetail.getMember().getId();

        CartInfoResponseDto cartInfo = cartService.getCartInfo(memberId);

        model.addAttribute("cartInfo", cartInfo);

        model.addAttribute("cartCount", cartInfo.getTotalCount());

        return "cart/cart";  // src/main/resources/templates/cart/cart.html
    }
}
