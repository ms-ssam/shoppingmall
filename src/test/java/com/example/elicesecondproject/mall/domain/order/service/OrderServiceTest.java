package com.example.elicesecondproject.mall.domain.order.service;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.order.dto.request.OrderSheetFromCartRequest;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderSheetResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import com.example.elicesecondproject.mall.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderService orderService;

    // 결제 페이지 -> 주문서 페이지 돌아가는 상황 단위 테스트
    @Test
    @DisplayName("cancelPendingOrderAndCreateOrderSheet 메서드 호출 시 주문과 결제를 실패 처리하고 선택했던 장바구니 아이템들을 기반으로 주문서를 재작성한다.")
    void cancelPendingOrderAndCreateOrderSheet_success_marksFailed_andRecreatesSheetFromCart() {
        // === given ===
        Long memberId = 1L;
        Long orderPk = 10L;
        String orderId = "ORDER_NUMBER_123";
        List<Long> optionDetailIds = List.of(10L, 20L);

        Order order = stubPendingOrder(memberId, orderPk, orderId, optionDetailIds);
        Payment payment = stubReadyPayment(orderId, memberId);

        stubCartHasAllOptionDetails(memberId, optionDetailIds);
        stubCartItems(memberId, optionDetailIds, List.of(111L, 222L));

        OrderSheetResponse fakeSheet = mock(OrderSheetResponse.class);
        OrderService spyService = spyCreateOrderSheetReturn(fakeSheet);



        // === when ===
        OrderSheetResponse result = spyService.cancelPendingOrderAndCreateOrderSheet(memberId, orderPk);



        // === then ===

        // 결제와 주문을 실패 처리 했는지 검증
        verify(payment).markAsFailed();
        verify(order).markAsFailed();

        // createOrderSheet가 cartItemIds를 담아서 호출되었는지 검증
        verify(spyService).createOrderSheet(eq(memberId), argThat(req ->
                req.getCartItemIds() != null &&
                        req.getCartItemIds().size() == 2 &&
                        req.getCartItemIds().containsAll(List.of(111L, 222L))
        ));

        assertSame(fakeSheet, result);
    }



    // === Helper Method ===

    // 주어진 옵션의 주문 항목을 가지고 있는 주문 stub 생성
    private Order stubPendingOrder(Long memberId, Long orderPk, String orderCode, List<Long> optionDetailIds) {
        Order order = mock(Order.class);
        when(orderRepository.findWithItemsById(orderPk)).thenReturn(Optional.of(order));
        when(order.getOwnerId()).thenReturn(memberId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        when(order.getOrderId()).thenReturn(orderCode);

        List<OrderItem> items = optionDetailIds.stream().map(odId -> {
            OrderItem oi = mock(OrderItem.class);
            when(oi.getOptionDetailId()).thenReturn(odId);
            return oi;
        }).toList();
        when(order.getOrderItems()).thenReturn(items);

        return order;
    }

    // stub payment 객체 생성
    private Payment stubReadyPayment(String orderId, Long memberId) {
        Payment payment = mock(Payment.class);
        when(paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY))
                .thenReturn(Optional.of(payment));
        return payment;
    }

    // 해당 회원의 장바구니에 주어진 옵션에 해당하는 장바구니 항목 다 가지고 있도록 설정
    private void stubCartHasAllOptionDetails(Long memberId, List<Long> optionDetailIds) {
        when(cartItemRepository.findExistingOptionDetailIdsInCart(memberId, optionDetailIds))
                .thenReturn(Set.copyOf(optionDetailIds));
    }

    // 주어진 항목 id에 포함되는 장바구니 항목 반환할 때 설정한 값 반환하도록 repo를 stub 객체로 설정
    private void stubCartItems(Long memberId, List<Long> optionDetailIds, List<Long> cartItemIds) {
        List<CartItem> cartItems = cartItemIds.stream().map(id -> {
            CartItem ci = mock(CartItem.class);
            when(ci.getId()).thenReturn(id);
            return ci;
        }).toList();

        when(cartItemRepository.findAllByCartMemberIdAndProductOptionDetailIdIn(memberId, optionDetailIds))
                .thenReturn(cartItems);
    }

    // 주어진 OrderResponse 반환하도록 설정한 spy 처리 OrderService 반환
    private OrderService spyCreateOrderSheetReturn(OrderSheetResponse sheet) {
        OrderService spyService = Mockito.spy(orderService);
        doReturn(sheet).when(spyService).createOrderSheet(anyLong(), any(OrderSheetFromCartRequest.class));
        return spyService;
    }


}
