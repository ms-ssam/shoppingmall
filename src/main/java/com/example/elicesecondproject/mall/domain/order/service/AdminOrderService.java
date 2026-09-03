package com.example.elicesecondproject.mall.domain.order.service;

import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.repository.OptionDetailRepository;
import com.example.elicesecondproject.mall.domain.order.dto.request.AdminOrderSearchCondition;
import com.example.elicesecondproject.mall.domain.order.dto.response.AdminOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.AdminOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import com.example.elicesecondproject.mall.domain.order.entity.OrderStatus;
import com.example.elicesecondproject.mall.domain.order.mapper.OrderMapper;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.payment.entity.Payment;
import com.example.elicesecondproject.mall.domain.payment.repository.PaymentRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final OptionDetailRepository optionDetailRepository;
    private final OrderMapper orderMapper;
    private final PaymentRepository paymentRepository;

    public Page<AdminOrderInfoResponse> searchOrders(AdminOrderSearchCondition condition, Pageable pageable) {
        Page<Order> orders = orderRepository.searchOrders(condition, pageable);
        return orders.map(orderMapper::toAdminOrderInfoResponse);
    }

    public AdminOrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findByOrderIdAndMemberId(order.getOrderId(), order.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return AdminOrderDetailResponse.of(order, payment);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        changeOrderStatusWithStock(order, newStatus);
    }

    @Transactional
    public void updateOrdersStatus(List<Long> orderIds, OrderStatus newStatus) {
        List<Order> orders = orderRepository.findAllById(orderIds);

        if (orders.size() != orderIds.size()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        for (Order order : orders) {
            changeOrderStatusWithStock(order, newStatus);
        }
    }

    private void changeOrderStatusWithStock(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getOrderStatus();

        // 1. 상태 전이 가능 여부 확인
        if (!currentStatus.canChangeTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_CHANGE);
        }

        // 2. 재고 차감 조건: "결제완료 → 상품 준비중"으로 넘어갈 때
        if (currentStatus == OrderStatus.PAID && newStatus == OrderStatus.PREPARING) {

            // (1) 주문 아이템에서 optionDetailId만 뽑기
            List<Long> optionDetailIds = order.getOrderItems().stream()
                    .map(OrderItem::getOptionDetailId)
                    .distinct()
                    .toList();

            if (optionDetailIds.isEmpty()) {
                throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
            }

            List<OptionDetail> optionDetails = optionDetailRepository.findAllById(optionDetailIds);

            if (optionDetails.size() != optionDetailIds.size()) {
                throw new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND);
            }

            Map<Long, OptionDetail> optionDetailMap = optionDetails.stream()
                    .collect(Collectors.toMap(OptionDetail::getId, Function.identity()));

            for (OrderItem item : order.getOrderItems()) {
                Long optionDetailId = item.getOptionDetailId();
                OptionDetail optionDetail = optionDetailMap.get(optionDetailId);

                if (optionDetail == null) {
                    throw new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND);
                }

                // 실제 재고 차감 (부족하면 removeStock 안에서 예외 던지게 해도 됨)
                optionDetail.removeStock(item.getQuantity());
            }

        }

        // 3. 실제 상태 변경
        order.updateOrderStatus(newStatus);
    }

}
