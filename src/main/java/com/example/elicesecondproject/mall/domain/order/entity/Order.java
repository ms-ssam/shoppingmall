package com.example.elicesecondproject.mall.domain.order.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.common.Ownable;
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
public class Order extends BaseEntity implements Ownable {
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
    private DeliveryInfo deliveryInfo;  // 배송 정보

    private int totalPrice;             // 상품 총액
    private int deliveryFee;            // 배송비
    private int totalPaymentFee;        // 총 결제 금액

    private String mainProductName; // 대표 상품 이름 예) "반팔 외 3개"

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
    // === 정적 팩토리 메서드 ===
    public static Order create(Member member,
                               DeliveryInfo deliveryInfo,
                               List<OrderItem> orderItems) {

        Order order = new Order();

        order.ordererName = member.getName();
        order.ordererPhoneNumber = member.getPhone();
        order.ordererEmail = member.getEmail();

        order.deliveryInfo = deliveryInfo;

        order.orderStatus = OrderStatus.PENDING;
        order.paymentStatus = PaymentStatus.READY;

        if (orderItems != null) {
            for (OrderItem orderItem : orderItems) {
                order.addItem(orderItem);  // 연관관계 편의 메셔드 만든 후 연관관계 편의 메서드 호출하는 걸로 수정
            }
        }

        order.recalcAmount();

        return order;
    }

    // ============================
    private void addItem(OrderItem item) {  // 장바구니에서 장바구니 항목 추가
        item.setOrder(this);
        orderItems.add(item);
    }
    // ============================

    public void changePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void changeOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void markAsPaid() {
        this.orderStatus = OrderStatus.PAID;
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.orderDate = LocalDateTime.now();
    }


    private void recalcAmount() {
        this.totalPrice = this.orderItems.stream()
                .mapToInt(OrderItem::getSubtotalPrice)
                .sum();

        this.deliveryFee = calculateShippingFee(this.totalPrice);
        this.totalPaymentFee = this.totalPrice + this.deliveryFee;
        updateMainProductName();
    }

    public static int calculateShippingFee(int totalPrice) {
        // 예시: 5만원 이상 무료배송, 아니면 3,000원
        if (totalPrice >= 50_000) {
            return 0;
        }
        return 3_000;
    }

    private void updateMainProductName() {
        if (orderItems.isEmpty()) {
            this.mainProductName = null;
            return;
        }

        String firstName = orderItems.get(0).getProductName();
        int extraCount = orderItems.size() - 1;

        if (extraCount <= 0) {
            this.mainProductName = firstName;
        } else {
            this.mainProductName = firstName + " 외 " + extraCount + "개";
        }
    }

    @Override
    public Long getOwnerId() {
        return this.memberId;
    }
}