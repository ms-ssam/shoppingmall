package com.example.elicesecondproject.mall.domain.order.repository;

import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.request.UserOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderRepositoryCustom {
    Page<Order> searchOrders(AdminOrderSearchCondition condition, Pageable pageable);
    Page<Order> searchMyOrders(UserOrderSearchCondition condition,
                               Long memberId,
                               Pageable pageable);
    Optional<Order> findWithItemsById(Long orderId);

    // 주문의 주문번호(!= PK)로 주문과 관련된 주문 항목들 함께 조회
    Optional<Order> findWithItemsByOrderId(String orderId);
}
