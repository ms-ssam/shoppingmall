package com.example.elicesecondproject.mall.domain.payment.service;

import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.domain.order.mapper.OrderMapper;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.payment.dto.TossConfirmResponse;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import com.example.elicesecondproject.mall.domain.payment.event.TossPaymentSucceedEvent;
import com.example.elicesecondproject.mall.domain.payment.repository.PaymentRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TossPaymentServiceTest {
    @Mock private OrderMapper orderMapper;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private CartService cartService;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks private TossPaymentService tossPaymentService;

    @Test
    @DisplayName("handleSuccess 메서드가 성공하면 결제와 주문의 상태가 바뀌고 결제 항목에 따른 장바구니 항목이 비워진다.")
    void handleSuccess_success() {
        // === given ===
        Long memberId = 1L;
        String orderId = "ORDER_CODE_123"; // 토스로 보내는 orderId (= orderCode)
        String paymentKey = "PAY_KEY_123";
        Long amount = 10000L;

        // stub 객체 준비
        Payment payment = stubReadyPayment(orderId, memberId, 10000);
        Order order = stubOrder(memberId, orderId);

        // spy TossPaymentService의 confirm() 메서드 응답 설정
        TossConfirmResponse confirmResponse = mock(TossConfirmResponse.class);
        when(confirmResponse.isDoneStatus()).thenReturn(true);
        when(confirmResponse.getPaymentKey()).thenReturn(paymentKey);
        when(confirmResponse.getMethod()).thenReturn("CARD");
        when(confirmResponse.getApprovedAt()).thenReturn("2025-12-17T00:00:00+09:00");  // 아무 값

        // stub spy service 준비
        TossPaymentService spyService = spyConfirmDone(paymentKey, orderId, amount, confirmResponse);

        // 성공 후 반환 응답 설정 (mapper 세팅)
        UserOrderDetailResponse mapped = mock(UserOrderDetailResponse.class);
        when(orderMapper.toUserOrderDetailResponse(order)).thenReturn(mapped);



        // === when ===
        UserOrderDetailResponse result = spyService.handleSuccess(paymentKey, orderId, amount, memberId);



        // === then ===

        // Payment 결제 완료 처리 메서드 호출됐는지
        verify(payment).markAsCompleted(eq(paymentKey), eq("CARD"), anyString());

        // Order 주문 완료 메서드 호출됐는지
        verify(order).markAsPaid();

        verify(applicationEventPublisher).publishEvent(
                argThat((Object ev) -> ev instanceof TossPaymentSucceedEvent)
        );

        // 최종 응답이 mapper 결과인지
        assertSame(mapped, result);
    }

    @Test
    @DisplayName("브라우저에서 조작된 금액이 들어올 경우 예외를 던져 confirm이 호출되지 않고 결제 완료 상태로 바꾸지 않는다.")
    void handleSuccess_amountMismatch_throws_and_does_not_confirm_and_change_anything() {
        // === given ===
        Long memberId = 1L;
        String orderId = "ORDER_CODE_123";
        String paymentKey = "PAY_KEY_123";
        Long amountFromClient = 9999L;  // 사용자 요청 금액을 다르게 만들어 위/변조 상황 설정

        Payment payment = stubReadyPaymentAmountOnly(orderId, memberId);

        // payment.validAmount(amountFromClient)가 실행되면 예외 (조작된 금액이 들어온 상황, 해당 경우 내부 로직에서 무조건 잡는다는 것을 가정)
        doThrow(new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT))
                .when(payment).validAmount(eq(amountFromClient));

        // confirm() 메서드 호출 여부를 검증해야 하므로 tossPaymentService를 spy로 만들어 외부 통신 방지
        TossPaymentService spyService = Mockito.spy(tossPaymentService);



        // === when + then ===

        // 위/변조된 금액이 들어왔을 때 예외 던지는지 검증
        assertThrows(BusinessException.class,
                () -> spyService.handleSuccess(paymentKey, orderId, amountFromClient, memberId)
        );

        // confirm(외부 승인) 호출되면 안 됨
        verify(spyService, never()).confirm(anyString(), anyString(), anyLong());

        // 상태 변경 일어나면 안됨
        verify(payment, never()).markAsCompleted(anyString(), anyString(), anyString());

        // 금액 검증 단계에서 막히면 주문 조회 및 장바구니 삭제도 일어나면 안됨 (이벤트 발행 X)
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(applicationEventPublisher);
    }



    // === Helper method ===

    // 금액 비교까지만 필요한 테스트의 Payment stub 생성
    private Payment stubReadyPaymentAmountOnly(String orderId, Long memberId) {
        Payment payment = mock(Payment.class);
        when(paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY))
                .thenReturn(Optional.of(payment));

        return payment;
    }

    // 해당 주문의 결제인지까지 확인 필요한 테스트용 Payment stub 생성
    private Payment stubReadyPayment(String orderId, Long memberId, int savedAmount) {
        Payment payment = mock(Payment.class);

        when(paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY))
                .thenReturn(Optional.of(payment));
        when(payment.getAmount()).thenReturn(savedAmount);  // confirm에서 사용하므로 필요
        when(payment.getOrderId()).thenReturn(orderId);     // 주문 조회에 사용하므로 필요

        return payment;
    }

    private Order stubOrder(Long memberId, String orderId) {
        Order order = mock(Order.class);
        when(orderRepository.findWithItemsByOrderId(orderId)).thenReturn(Optional.of(order));

        return order;
    }

    // confirm() 외부 통신 메서드를 spy로 덮어쓰기
    private TossPaymentService spyConfirmDone(String paymentKey, String orderId, Long amount, TossConfirmResponse confirmDone) {
        TossPaymentService spyService = Mockito.spy(tossPaymentService);
        doReturn(confirmDone).when(spyService).confirm(eq(paymentKey), eq(orderId), eq(amount));
        return spyService;
    }

}
