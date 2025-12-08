package com.example.elicesecondproject.mall.domain.order.entity;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.entity.SoftDeletableBaseEntity;
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
public class Order extends SoftDeletableBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 코치님한테 물어보기 -> 주문자(회원) 식별자?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

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
}
