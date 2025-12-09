package com.example.elicesecondproject.mall.domain.order.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderCreateRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
