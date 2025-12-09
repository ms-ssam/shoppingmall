package com.example.elicesecondproject.mall.domain.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeliveryInfo {

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String receiverAddress;

    @Column(nullable = false)
    private String receiverPhone;



    public static DeliveryInfo of(String receiverName,
                                  String receiverAddress,
                                  String receiverPhone
    ) {
        DeliveryInfo info = new DeliveryInfo();
        info.receiverName = receiverName;
        info.receiverAddress = receiverAddress;
        info.receiverPhone = receiverPhone;
        return info;
    }
}

/*
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeliveryInfo {

    private String receiverName;
    private String receiverPhone;
    private String zipCode;
    private String address1;
    private String address2;
    private String memo;

    public static DeliveryInfo of(String receiverName,
                                  String receiverPhone,
                                  String zipCode,
                                  String address1,
                                  String address2,
                                  String memo) {
        return new DeliveryInfo(receiverName, receiverPhone, zipCode, address1, address2, memo);
    }
}
 */