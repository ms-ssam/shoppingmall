package com.example.elicesecondproject.mall.domain.cart.controller;

import com.example.elicesecondproject.mall.domain.cart.dto.request.CartItemOptionModifyRequest;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartInfoResponseDto;
import com.example.elicesecondproject.mall.domain.cart.dto.response.CartItemEditPopupResponse;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/cart")
@Controller
public class CartViewController {
    private final CartService cartService;

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
                                 @AuthenticationPrincipal MemberDetail principal,
                                 Model model) {

        Long memberId = principal.getMember().getId(); // 네 프로젝트 구조에 맞게
        CartInfoResponseDto cartInfo = cartService.getCartInfo(memberId); // 이미 있을 거라고 가정
        model.addAttribute("cartInfo", cartInfo);

        // 팝업에 쓸 기본 데이터
        CartItemEditPopupResponse popupData = cartService.getCartItemEditPopup(cartItemId);
        model.addAttribute("cartItem", popupData);
        model.addAttribute("cartItemId", cartItemId);
        model.addAttribute("showCartItemEditPopup", true); // cart.html에서 이걸 보고 팝업 띄움

        // 1) 검증 오류 → cart 페이지 + 팝업 유지
        if (bindingResult.hasErrors()) {
            return "cart/cart";   // ★ 팝업 템플릿이 아니라 cart 페이지
        }

        try {
            // 2) 비즈니스 로직 수행
            cartService.updateCartItemOption(cartItemId, request);
        } catch (BusinessException e) {
            // 비즈니스 오류 → cart 페이지 + 팝업 + 에러 메시지
            model.addAttribute("errorMessage", e.getMessage());
            return "cart/cart";
        }

        // 3) 성공 시 장바구니로 리다이렉트
        return "redirect:/cart";
    }

    @GetMapping
    public String showCartPage(Model model, @AuthenticationPrincipal MemberDetail memberDetail) {
        Long memberId = memberDetail.getMember().getId();

        CartInfoResponseDto cartInfo = cartService.getCartInfo(memberId);

        model.addAttribute("cartInfo", cartInfo);

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
