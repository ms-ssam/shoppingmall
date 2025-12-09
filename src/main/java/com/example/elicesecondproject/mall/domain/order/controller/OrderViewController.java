package com.example.elicesecondproject.mall.domain.order.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderCreateRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.service.OrderService;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderViewController {

    private final OrderService orderService;

    /**
     * 장바구니 → 주문서 화면 진입
     * - 장바구니에서 선택된 cartItemIds를 받아서
     * - 주문서에 뿌려줄 OrderSheetResponse 내려줌
     */
    @PostMapping("/sheet")
    public String showOrderSheet(@AuthenticationPrincipal MemberDetail memberDetail,
                                 @Valid @ModelAttribute OrderSheetFromCartRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        Long memberId = memberDetail.getMember().getId();

        if (bindingResult.hasErrors()) {
            // cartItemIds가 비어있음 -> 다시 장바구니로
            redirectAttributes.addFlashAttribute("errorMessage", "주문할 상품을 선택해주세요.");
            return "redirect:/carts";
        }

        try{
            OrderSheetResponse orderSheet = orderService.createOrderSheet(memberId, request);

            // 주문 생성용 DTO (배송정보 입력용)
            OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
            orderCreateRequest.setCartItemIds(request.getCartItemIds());

            model.addAttribute("orderSheet", orderSheet);
            model.addAttribute("orderCreateRequest", orderCreateRequest);

            return "order/order-sheet";

        } catch(BusinessException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/carts";
        }
    }

    @PostMapping
    public String createOrder(@AuthenticationPrincipal MemberDetail memberDetails,
                              @Valid @ModelAttribute OrderCreateRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Long memberId = memberDetails.getMember().getId();
        // 배송정보 입력, 약관 동의 -> dto에서 검증 ->실패 시 다시 주문서 화면으로
        if (bindingResult.hasErrors()) {
            OrderSheetFromCartRequest sheetRequest = new OrderSheetFromCartRequest();
            sheetRequest.setCartItemIds(request.getCartItemIds());

            try {
                OrderSheetResponse orderSheet = orderService.createOrderSheet(memberId, sheetRequest);
                model.addAttribute("orderSheet", orderSheet);
                // request는 이미 모델에 있음
                return "order/order-sheet";
            } catch (BusinessException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/carts";
            }
        }

        try{
            Long orderId = orderService.createOrder(memberId, request);

            // FIXME : 일단 주문 완료 페이지로 리다이렉트(결제 pg 연동 후 수정)
            return "redirect:/orders/" + orderId + "/complete";

        } catch(BusinessException e) {
            // TODO : 재고부족, 판매중지 상품 등 있으면 주문서를 다시 띄울지 장바구니로 보낼지 의논 필요
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/carts";
        }
    }

    @GetMapping("/{orderId}/complete")
    public String orderComplete(@PathVariable Long orderId,
                                @AuthenticationPrincipal MemberDetail memberDetails,
                                Model model) {

        Long memberId = memberDetails.getMember().getId();
        Order order = orderService.getOrderForMember(orderId, memberId);

        model.addAttribute("order", order);

        return "order/order-complete";
    }
}
