package com.example.elicesecondproject.mall.domain.payment.controller;

import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.payment.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payments/toss")
@RequiredArgsConstructor
public class TossPaymentController {
    private final TossPaymentService tossPaymentService;

    @GetMapping("/success")
    public String success(@RequestParam String paymentKey,
                          @RequestParam String orderId,  // 토스가 넘겨주는 orderId가 orderCode임
                          @RequestParam Long amount,
                          @AuthenticationPrincipal MemberDetail memberDetail,
                          Model model) {
        Long memberId = memberDetail.getMember().getId();

        UserOrderDetailResponse order = tossPaymentService.handleSuccess(paymentKey, orderId, amount, memberId);

        model.addAttribute("order", order);

        return "order/order-complete";
    }
}