package com.example.elicesecondproject.mall.domain.order.service;

import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.response.AdminOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.OrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.mapper.OrderMapper;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public Page<OrderInfoResponse> searchOrders(AdminOrderSearchCondition condition, Pageable pageable) {
        Page<Order> responses = orderRepository.searchOrders(condition, pageable);
        return responses.map(orderMapper::toOrderInfoResponse);
    }

    public AdminOrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return AdminOrderDetailResponse.of(order);
    }
}
