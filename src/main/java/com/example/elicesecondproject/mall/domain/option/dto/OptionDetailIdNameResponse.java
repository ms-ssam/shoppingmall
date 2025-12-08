package com.example.elicesecondproject.mall.domain.option.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionDetailIdNameResponse {
    private Long id;
    private String name;
}
