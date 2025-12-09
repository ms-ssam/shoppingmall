package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.entity.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminOrderDetailResponse {
    private final Long orderId;
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
    // TODO: 결제 정보 추후 추가

    public static AdminOrderDetailResponse of(Order order) {
        List<AdminOrderItemDetailResponse> items = order.getOrderItems().stream()
                .map(AdminOrderItemDetailResponse::of)
                .toList();

        return new AdminOrderDetailResponse(
                order.getId(),
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
                order.getPaymentStatus());
    }
}

