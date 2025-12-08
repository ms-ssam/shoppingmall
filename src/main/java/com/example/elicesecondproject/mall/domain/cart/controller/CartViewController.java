package com.example.elicesecondproject.mall.domain.cart.controller;

import com.example.elicesecondproject.mall.domain.cart.dto.request.CartItemOptionModifyRequest;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartItemEditPopupResponse;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor  // TODO: 주소 cart가 맞을지, carts가 맞을지 찾아보기 (cart 같은 경우엔 여러 개 조회하는 게 아니니까 cart가 맞나?)
@RequestMapping("/cart")  // TODO: 경로 매핑 팀원들한테 물어보기 (Security 걸리는 거 때문에 - API의 경우 /api로 시작해서 로그인 사용자만 볼 수 있도록...)
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
}
