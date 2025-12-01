package com.example.elicesecondproject.mall.domain.option.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionGroupDto {

    private Long id;

    @NotBlank(message = "옵션 그룹명(색상)은 필수입니다.")
    private String name;

    @NotNull(message = "정렬 순서는 필수입니다.")
    private Integer displayOrder;

    @Valid
    @NotEmpty(message = "최소 1개 이상의 옵션 상세가 필요합니다.")
    private List<OptionDetailDto> details;
}

