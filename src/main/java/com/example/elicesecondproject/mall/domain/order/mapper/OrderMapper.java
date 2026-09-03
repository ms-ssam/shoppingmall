package com.example.elicesecondproject.mall.domain.order.mapper;

import com.example.elicesecondproject.mall.domain.order.dto.response.AdminOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderItemDetailResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    AdminOrderInfoResponse toAdminOrderInfoResponse(Order order);

    UserOrderInfoResponse toUserOrderInfoResponse(Order order);

    UserOrderItemDetailResponse toUserOrderItemDetailResponse(OrderItem orderItem);
    @Mapping(target = "deliveryInfo",
            expression = "java(com.example.elicesecondproject.mall.domain.order.dto.response.DeliveryInfoResponse.of(order.getDeliveryInfo()))")
    UserOrderDetailResponse toUserOrderDetailResponse(Order order);

}
