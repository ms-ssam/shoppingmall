package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import com.example.elicesecondproject.mall.domain.payment.dto.PaymentInfoResponse;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminOrderDetailResponse {
    private final Long id;
    private final String orderId;
    private final List<AdminOrderItemDetailResponse> items;
    private final LocalDateTime orderDate;
    private final OrderStatus orderStatus;

    private final String ordererName;
    private final String ordererEmail;
    private final String ordererPhoneNumber;

    private final int orderTotalPrice;
    private final int deliveryFee;
    private final int totalPaymentFee;

    private final DeliveryInfoResponse deliveryInfo;

    private final PaymentStatus paymentStatus;

    private final PaymentInfoResponse paymentInfo;

    public static AdminOrderDetailResponse of(Order order, Payment payment) {
        List<AdminOrderItemDetailResponse> items = order.getOrderItems().stream()
                .map(AdminOrderItemDetailResponse::of)
                .toList();

        return new AdminOrderDetailResponse(
                order.getId(),
                order.getOrderId(),
                items,
                order.getOrderDate(),
                order.getOrderStatus(),
                order.getOrdererName(),
                order.getOrdererEmail(),
                order.getOrdererPhoneNumber(),
                order.getTotalPrice(),
                order.getDeliveryFee(),
                order.getTotalPaymentFee(),
                DeliveryInfoResponse.of(order.getDeliveryInfo()),
                order.getPaymentStatus(),
                PaymentInfoResponse.of(payment)
        );
    }
}

