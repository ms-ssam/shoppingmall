package com.example.elicesecondproject.mall.domain.order.entity;

import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String ordererName;

    @Column(nullable = false)
    private String ordererPhoneNumber;

    @Column(nullable = false)
    private String ordererEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;         // 주문 상태

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;       // 결제 상태

    private LocalDateTime orderDate;    // 주문 날짜

    @Embedded
    private DeliveryInfo  deliveryInfo;  // 배송 정보

    private int totalPrice;             // 상품 총액
    private int deliveryFee;            // 배송비
    private int totalPaymentFee;        // 총 결제 금액

    private String mainProductName; // 대표 상품 이름 예) 반팔 외 3개

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
