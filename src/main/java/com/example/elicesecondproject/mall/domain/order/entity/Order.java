package com.example.elicesecondproject.mall.domain.order.entity;

import com.example.elicesecondproject.mall.domain.Member.entity.Member;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  //!❗️연관관계 편의 메서드 설정 필요 (Member 쪽에서? 아님 여기서?)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)  //❗️ N+1 조회 주의
    private List<OrderItem> orderItems = new ArrayList<>();  //!❗️ 연관관계 편의 메서드 설정 필요 (여기서?)

    // === 연관관계 편의 메서드 ===
    private void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

}