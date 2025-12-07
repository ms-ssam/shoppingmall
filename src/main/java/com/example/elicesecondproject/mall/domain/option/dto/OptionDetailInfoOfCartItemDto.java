package com.example.elicesecondproject.mall.domain.option.dto;

import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import lombok.Builder;
import lombok.Getter;

// 장바구니 화면의 장바구니 항목 부분에 보여줄 정보 용도입니다.
@Getter
public class OptionDetailInfoOfCartItemDto {
    private long id;
    private String name;
    private boolean soldOut;
    private int optionAppliedUnitPrice;
    private int saleUnitPrice;
    private int subtotalPrice;

    @Builder
    private OptionDetailInfoOfCartItemDto(long id, String name, boolean soldOut, int  optionAppliedUnitPrice, int saleUnitPrice, int subtotalPrice) {
        this.id = id;
        this.name = name;
        this.soldOut = soldOut;
        this.optionAppliedUnitPrice = optionAppliedUnitPrice;
        this.saleUnitPrice = saleUnitPrice;
        this.subtotalPrice = subtotalPrice;
    }

    public static OptionDetailInfoOfCartItemDto of(OptionDetail optionDetail) {
        return OptionDetailInfoOfCartItemDto.builder()
                .id(optionDetail.getId())
                .name(optionDetail.getName())
                .soldOut(optionDetail.isSoldOut())
                .optionAppliedUnitPrice(optionDetail.getOptionAppliedUnitPrice())
                .saleUnitPrice(optionDetail.getSaleUnitPrice())
                .subtotalPrice(optionDetail.getSubtotalPrice())
                .build();
    }
}
