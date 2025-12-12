package com.example.elicesecondproject.mall.domain.payment.controller;

import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PaymentPageController {
    private final OrderService orderService;

    @Value("${toss.payments.widget-client-key}")
    private String widgetClientKey;

    @Value("${toss.payments.success-url}")
    private String successUrl;

    @Value("${toss.payments.fail-url}")
    private String failUrl;

    @GetMapping("/orders/{orderId}/payment")
    public String showPaymentPage(@AuthenticationPrincipal MemberDetail memberDetail,
                                  @PathVariable Long orderId,
                                  Model model) {
        Long memberId = memberDetail.getMember().getId();
        UserOrderInfoResponse order = orderService.getOrderForMember(orderId, memberId);

        model.addAttribute("order", order);
        model.addAttribute("clientKey", widgetClientKey);
        model.addAttribute("successUrl", successUrl);
        model.addAttribute("failUrl", failUrl);
        model.addAttribute("customerKey", "member-" +  memberId);  // TODO: 아무 String 가능

        return "payment/payment";
    }
}