package com.example.elicesecondproject.mall.domain.inventory.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustInventoryRequest {

    @NotNull(message = "옵션 상세 ID는 필수입니다.")
    private Long optionDetailId;

    @NotNull(message = "변경 수량은 필수입니다.")
    private Integer quantity;

    @NotBlank(message = "변경 사유는 필수입니다.")
    @Size(max = 255, message = "변경 사유는 최대 255자까지 입력 가능합니다.")
    private String reason;
}

