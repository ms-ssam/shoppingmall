package com.example.elicesecondproject.mall.domain.order.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderCreateRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
                                 @ModelAttribute OrderSheetFromCartRequest request,
                                 Model model) {

        Long memberId = memberDetail.getMember().getId();

        OrderSheetResponse orderSheet = orderService.createOrderSheet(memberId, request);

        // 주문 생성용 DTO (배송정보 입력용)
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        orderCreateRequest.setCartItemIds(request.getCartItemIds());

        model.addAttribute("orderSheet", orderSheet);
        model.addAttribute("orderCreateRequest", orderCreateRequest);

        return "order/order-sheet"; // 타임리프 템플릿 이름
    }

    @PostMapping
    public String createOrder(@AuthenticationPrincipal MemberDetail memberDetails,
                              @Valid @ModelAttribute OrderCreateRequest request,
                              BindingResult bindingResult,
                              @RequestParam(value = "agreeTerms", required = false) String agreeTerms,
                              Model model) {

        // 1. 약관 동의 체크
        if (agreeTerms == null) {
            bindingResult.reject("agreeTerms", "약관 동의가 필요합니다.");
        }

        // 2. 검증 실패 시 다시 주문서 화면으로
        if (bindingResult.hasErrors()) {
            // 주문서 다시 그리려면 orderSheet 다시 조회해서 모델에 담아야 함
            // ex) model.addAttribute("orderSheet", orderSheetService.buildOrderSheet(...));
            return "order/order-sheet";
        }

        Long memberId = memberDetails.getMember().getId();

        Long orderId = orderService.createOrder(memberId, request);

        // TODO : 일단 주문 완료 페이지로 리다이렉트(결제 pg 연동 후 수정)
        return "redirect:/orders/" + orderId + "/complete";
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
