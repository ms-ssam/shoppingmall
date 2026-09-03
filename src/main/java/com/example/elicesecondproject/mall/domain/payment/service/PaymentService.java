package com.example.elicesecondproject.mall.domain.payment.service;

import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import com.example.elicesecondproject.mall.domain.payment.repository.PaymentRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Payment getOrCreateReadyPayment(String orderId, Long memberId, int amount) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_PAYMENT_CONFLICT);
        }

        return paymentRepository.findByOrderIdAndMemberIdAndPaymentStatus(orderId, memberId, PaymentStatus.READY)
                .orElseGet(() -> paymentRepository.save(Payment.createReadyPayment(orderId, memberId, amount)));
    }
}
