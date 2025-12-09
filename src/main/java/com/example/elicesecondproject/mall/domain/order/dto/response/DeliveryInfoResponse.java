package com.example.elicesecondproject.mall.domain.order.dto.response;

import com.example.elicesecondproject.mall.domain.order.entity.DeliveryInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeliveryInfoResponse {
    private final String receiverName;
    private final String receiverAddress;
    private final String receiverPhone;

    public static DeliveryInfoResponse of(DeliveryInfo deliveryInfo){
        return new DeliveryInfoResponse(
                deliveryInfo.getReceiverName(),
                deliveryInfo.getReceiverAddress(),
                deliveryInfo.getReceiverPhone()
        );
    }
}
