//package com.example.elicesecondproject.mall.domain.payment.service;
//
//import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
//import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
//import com.example.elicesecondproject.mall.domain.cart.service.CartService;
//import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
//import com.example.elicesecondproject.mall.domain.order.entity.Order;
//import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
//import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
//import com.example.elicesecondproject.mall.domain.order.mapper.OrderMapper;
//import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
//import com.example.elicesecondproject.mall.domain.payment.dto.TossConfirmResponse;
//import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
//import com.example.elicesecondproject.mall.domain.payment.repository.PaymentRepository;
//import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TossPaymentServiceTest {
//    @Mock private OrderMapper orderMapper;
//    @Mock private CartItemRepository cartItemRepository;
//    @Mock private OrderRepository orderRepository;
//    @Mock private PaymentRepository paymentRepository;
//    @Mock private CartService cartService;
//
//    @InjectMocks private TossPaymentService tossPaymentService;
//
//    @Test
//    @DisplayName("handleSuccess 메서드가 성공하면 결제와 주문의 상태가 바뀌고 결제 항목에 따른 장바구니 항목이 비워진다.")
//    void handleSuccess_success() {
//        // given
//        Long memberId = 1L;
//        String orderId = "ORDER_CODE_123"; // 토스로 보내는 orderId (= orderCode)
//        String paymentKey = "PAY_KEY_123";
//        Long amount = 10000L;
//
//        Payment payment = mock(Payment.class);
//        when(paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY))
//                .thenReturn(Optional.of(payment));
//        when(payment.getAmount()).thenReturn(10000);
//        when(payment.getOrderId()).thenReturn(orderId);
//
//        Order order = mock(Order.class);
//        when(orderRepository.findWithItemsByOrderId(orderId)).thenReturn(Optional.of(order));
//        when(order.getOwnerId()).thenReturn(memberId);
//
//        // 주문 아이템(옵션) -> 장바구니 삭제 대상으로 사용됨
//        OrderItem item1 = mock(OrderItem.class);
//        OrderItem item2 = mock(OrderItem.class);
//        when(item1.getOptionDetailId()).thenReturn(10L);
//        when(item2.getOptionDetailId()).thenReturn(20L);
//        when(order.getOrderItems()).thenReturn(List.of(item1, item2));
//
//        // cartItemRepository가 찾아온 삭제 대상 cartItems
//        CartItem cartItem1 = mock(CartItem.class);
//        CartItem cartItem2 = mock(CartItem.class);
//        when(cartItem1.getId()).thenReturn(111L);
//        when(cartItem2.getId()).thenReturn(222L);
//        when(cartItemRepository.findAllByCartMemberIdAndProductOptionDetailIdIn(eq(memberId), anyList()))
//                .thenReturn(List.of(cartItem1, cartItem2));
//
//        // confirm() 외부통신은 spy로 막고 DONE 응답 고정
//        TossPaymentService spyService = Mockito.spy(tossPaymentService);
//        TossConfirmResponse confirm = mock(TossConfirmResponse.class);
//        when(confirm.getStatus()).thenReturn("DONE");
//        when(confirm.getPaymentKey()).thenReturn(paymentKey);
//        when(confirm.getMethod()).thenReturn("CARD");
//        when(confirm.getApprovedAt()).thenReturn(OffsetDateTime.now().toString());
//
//        doReturn(confirm).when(spyService).confirm(eq(paymentKey), eq(orderId), eq(amount));
//
//        UserOrderDetailResponse mapped = mock(UserOrderDetailResponse.class);
//        when(orderMapper.toUserOrderDetailResponse(order)).thenReturn(mapped);
//
//        // when
//        UserOrderDetailResponse result = spyService.handleSuccess(paymentKey, orderId, amount, memberId);
//
//        // then
//        verify(payment).markAsCompleted(eq(paymentKey), eq("CARD"), anyString());
//        verify(order).markAsPaid();
//
//        verify(cartService).deleteSelectedCartItems(eq(memberId), argThat(ids ->
//                ids.size() == 2 && ids.containsAll(List.of(111L, 222L))
//        ));
//
//        // 최종 응답이 mapper 결과인지
//        assertSame(mapped, result);
//    }
//
//    @Test
//    @DisplayName("브라우저에서 조작된 금액이 들어올 경우 예외를 던져 confirm이 호출되지 않고 결제 완료 상태로 바꾸지 않는다.")
//    void handleSuccess_amountMismatch_throws_and_does_not_confirm_and_change_anything() {
//        // given
//        Long memberId = 1L;
//        String orderId = "ORDER_CODE_123";
//        String paymentKey = "PAY_KEY_123";
//        Long amountFromClient = 9999L;
//
//        Payment payment = mock(Payment.class);
//        when(paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY))
//                .thenReturn(Optional.of(payment));
//        when(payment.getAmount()).thenReturn(10000); // 서버에 저장된 금액
//
//        TossPaymentService spyService = Mockito.spy(tossPaymentService);
//
//        // when / then
//        org.junit.jupiter.api.Assertions.assertThrows(BusinessException.class,
//                () -> spyService.handleSuccess(paymentKey, orderId, amountFromClient, memberId)
//        );
//
//        // confirm 호출되면 안 됨
//        verify(spyService, never()).confirm(anyString(), anyString(), anyLong());
//
//        // 상태 변경도 안 됨
//        verify(payment, never()).markAsCompleted(anyString(), anyString(), anyString());
//        verifyNoInteractions(orderRepository); // 금액 검증에서 바로 막히면 주문 조회도 안 됨(네 코드 기준)
//        verifyNoInteractions(cartService);
//    }
//}