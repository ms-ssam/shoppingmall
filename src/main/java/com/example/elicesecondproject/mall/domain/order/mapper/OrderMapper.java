package com.example.elicesecondproject.mall.domain.order.mapper;

import com.example.elicesecondproject.mall.domain.order.dto.response.OrderInfoResponse;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    OrderInfoResponse  toOrderInfoResponse(Order order);
}
