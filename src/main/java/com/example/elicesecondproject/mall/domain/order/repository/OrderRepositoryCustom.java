package com.example.elicesecondproject.mall.domain.order.repository;

import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
    Page<Order> searchOrders(AdminOrderSearchCondition condition, Pageable pageable);
}
