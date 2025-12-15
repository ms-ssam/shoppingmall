package com.example.elicesecondproject.mall.domain.payment.service;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.domain.order.mapper.OrderMapper;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.payment.dto.TossConfirmResponse;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import com.example.elicesecondproject.mall.domain.payment.repository.PaymentRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TossPaymentService {
    private final OrderMapper orderMapper;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;


    @Value("${toss.payments.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Transactional
    public UserOrderDetailResponse handleSuccess(String paymentKey, String orderId, Long amount, Long memberId) {

        // 1) 사용자가 시도했던 결제 정보와 동일한지 확인 + 결제 금액 검사
        Payment payment = paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if(payment.getAmount() != amount) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 2) 실제 주문 조회 + 본인 검증
        Order order = orderRepository.findWithItemsByOrderId(payment.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getOwnerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        // 3) 토스 결제 승인 API 호출 (DB 값 사용)
        TossConfirmResponse confirmResponse = confirm(paymentKey, payment.getOrderId(), (long) payment.getAmount());

        if(!confirmResponse.getStatus().equals("DONE")) {
            throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        // 4) confirm 성공했다면 그 이후부터 paymentkey 값 신뢰라고 저장
        payment.markAsCompleted(confirmResponse.getPaymentKey(), confirmResponse.getMethod(), confirmResponse.getApprovedAt());

        // 5) 주문도 결제 성공 처리
        order.markAsPaid();

        /*
         6) 장바구니 비우기
          현재 방식이 동작하려면 CartItem 하나가 Cart 내에서 OD 기준으로 유일해야 한다는 전제가 필요.
           즉, 같은 옵션을 장바구니에 두 번 담으면 수량만 증가하는 형태로 동작해야만 정상 동작
         */
        List<Long> optionDetailIds = order.getOrderItems().stream()
                .map(OrderItem::getOptionDetailId)
                .distinct()
                .toList();

        List<CartItem> cartItemsToDelete = cartItemRepository.findAllByCartMemberIdAndProductOptionDetailIdIn(memberId, optionDetailIds);

        if (!cartItemsToDelete.isEmpty()) {
            List<Long> cartItemIds = cartItemsToDelete.stream()
                    .map(CartItem::getId)
                    .toList();

            // FIXME: 같은 레벨의 객체를 참조하면 순환 참조 가능성 존재 (안티패턴) -> Spring Event로 refactoring 필요
            cartService.deleteSelectedCartItems(memberId, cartItemIds);
        }

        return orderMapper.toUserOrderDetailResponse(order);
    }

    public TossConfirmResponse confirm(String paymentKey, String orderId, Long amount) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TossConfirmResponse> response =
                restTemplate.postForEntity(url, entity, TossConfirmResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }

        return response.getBody();
    }

    public Long handleFail(String orderId, Long memberId) {
        // 실제로 넘겨받은 주문번호에 해당하고 준비 상태인 사용자의 결제 건이 존재하는지 확인
        Payment payment = paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 주문 조회
        Order order = orderRepository.findByOrderId(payment.getOrderId())
                .orElseThrow(() ->  new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 소유주 확인
        if(!order.getOwnerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        // 주문, 결제 실패 처리
        payment.markAsFailed();
        order.markAsFailed();

        // PK 반환해서 redirect에 사용
        return order.getId();
    }
}
