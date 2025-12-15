package com.example.elicesecondproject.mall.domain.payment.controller;

import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.payment.service.TossPaymentService;
import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/fail")
    public String fail(@RequestParam(required = false) String code,
                       @RequestParam(required = false) String message,
                       @RequestParam String orderId,
                       @AuthenticationPrincipal MemberDetail memberDetail,
                       RedirectAttributes redirectAttributes) {

        Long memberId = memberDetail.getMember().getId();

        Long orderPk = tossPaymentService.handleFail(orderId, memberId);

        redirectAttributes.addFlashAttribute("errorMessage",
                "결제에 실패했습니다. (" + code + ") " + (message == null ? "" : message));

        // 현재는 결제 실패시 주문서 작성 페이지로 이동. 실패하면 주문서 작성으로 보낼지 아니면 결제 페이지로 되돌려서 다시 시도하게 할지 논의 필요
        return "redirect:/orders/" + orderPk + "/sheet";
    }
}