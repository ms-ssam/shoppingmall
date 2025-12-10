package com.example.elicesecondproject.mall.domain.order.dto.request;

import com.example.elicesecondproject.mall.domain.order.entity.DeliveryInfo;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class OrderCreateRequest {
    @NotBlank(message = "수령인을 입력해주세요.")
    private String receiverName;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^010[0-9]{8}$",
            message = "전화번호는 010으로 시작하며 숫자만 입력한 11자리여야 합니다.")
    private String receiverPhone;

    @NotBlank(message = "우편번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{5}$",
            message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address1;

    @NotBlank(message = "상세 주소를 입력해주세요.")
    private String address2;

    @Size(max = 100, message = "배송 메모는 최대 100자까지 입력할 수 있습니다.")
    private String deliveryMemo;

    @AssertTrue(message = "약관 동의가 필요합니다.")
    private boolean agreeTerms;

    @NotEmpty(message = "주문할 상품이 비어있습니다.")
    private List<Long> cartItemIds;

    // DeliveryInfo로 변환하는 헬퍼
    public DeliveryInfo toDeliveryInfo() {
        return DeliveryInfo.of(
                trim(receiverName),
                receiverPhone,
                zipCode,
                address1,
                trim(address2),
                trim(deliveryMemo)
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

}
