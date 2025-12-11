package com.example.elicesecondproject.mall.domain.order.controller;

import com.example.elicesecondproject.mall.domain.member.entity.MemberDetail;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderCreateRequest;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.service.OrderService;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderViewController {

    private final OrderService orderService;

    // 장바구니 -> 주문서 화면
    // 모든 에러는 errorMessage 하나로 통일해서 /cart로 리다이렉트
    @PostMapping("/sheet")
    public String showOrderSheet(@AuthenticationPrincipal MemberDetail memberDetail,
                                 @Valid @ModelAttribute OrderSheetFromCartRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        Long memberId = memberDetail.getMember().getId();

        // DTO 검증 실패
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.hasFieldErrors("cartItemIds")
                    ? bindingResult.getFieldError("cartItemIds").getDefaultMessage()
                    : "요청 값이 올바르지 않습니다.";
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/cart";
        }

        try{
            OrderSheetResponse orderSheet = orderService.createOrderSheet(memberId, request);

            // 주문 생성용 DTO (배송정보 입력)
            OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
            orderCreateRequest.setCartItemIds(request.getCartItemIds());

            model.addAttribute("orderSheet", orderSheet);
            model.addAttribute("orderCreateRequest", orderCreateRequest);

            return "order/order-sheet";

        } catch(BusinessException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    // 주문서 -> 주문 생성
    @PostMapping
    public String createOrder(@AuthenticationPrincipal MemberDetail memberDetail,
                              @Valid @ModelAttribute OrderCreateRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Long memberId = memberDetail.getMember().getId();
        // 배송정보 입력, 약관 동의 -> dto에서 검증 ->실패 시 다시 주문서 화면으로
        if (bindingResult.hasErrors()) {
            OrderSheetFromCartRequest sheetRequest = new OrderSheetFromCartRequest();
            sheetRequest.setCartItemIds(request.getCartItemIds());

            try {
                OrderSheetResponse orderSheet = orderService.createOrderSheet(memberId, sheetRequest);
                model.addAttribute("orderSheet", orderSheet);
                model.addAttribute("orderCreateRequest", request);
                return "order/order-sheet";
            } catch (BusinessException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/cart";
            }
        }

        try{
            Long orderId = orderService.createOrder(memberId, request);

//            // 일단 주문 완료 = 결제 완료. (TODO : 결제 pg 연동 후 수정)
//            return "redirect:/orders/" + orderId + "/complete";
            return "redirect:/orders/" + orderId + "/payment";  // 결제 페이지로 이동

        } catch(BusinessException e) {
            // 재고부족, 판매중지 상품 등 예외 -> 장바구니로
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

//    @GetMapping("/{orderId}/complete")
//    public String orderComplete(@PathVariable Long orderId,
//                                @AuthenticationPrincipal MemberDetail memberDetails,
//                                Model model) {
//
//        Long memberId = memberDetails.getMember().getId();
//
//        // TODO : 제품 상세나오면 수정하기
//        UserOrderInfoResponse order = orderService.getOrderForMember(orderId, memberId);  // 푸름님 여기 아예 다른 메서드로 갈아끼우셨음
//        model.addAttribute("order", order);
//
//        return "order/order-complete";
//    }
}
