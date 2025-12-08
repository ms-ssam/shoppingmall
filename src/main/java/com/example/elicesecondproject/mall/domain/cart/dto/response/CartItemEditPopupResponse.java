package com.example.elicesecondproject.mall.domain.cart.dto.response;

import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailIdNameResponse;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupIdNameResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemEditPopupResponse {

    // 선택 가능한 옵션 그룹 목록 (예: 색상)
    private List<ProductOptionGroupIdNameResponse> optionGroups;

    // 선택 가능한 옵션 상세 목록 (예: 사이즈)
    private List<OptionDetailIdNameResponse> optionDetails;

    // 현재 장바구니에 담긴 수량
    private int selectedQuantity;

    // 팝업에서 기본으로 선택되어 있어야 하는 항목들
    private Long selectedProductOptionGroupId;
    private Long selectedOptionDetailId;
}
