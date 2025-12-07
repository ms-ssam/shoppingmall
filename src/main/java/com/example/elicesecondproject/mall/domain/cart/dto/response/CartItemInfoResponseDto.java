package com.example.elicesecondproject.mall.domain.cart.dto.response;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailInfoOfCartItemDto;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupInfoOfCartItemDto;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.product.dto.ProductInfoOfCartItemDto;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CartItemInfoResponseDto {
    private long cartItemId;
    private int quantity;  // 장바구니에 담은 수량
    private int subtotalPrice;
    private ProductInfoOfCartItemDto productInfo;
    private ProductOptionGroupInfoOfCartItemDto productOptionGroupInfo;
    private OptionDetailInfoOfCartItemDto optionDetailInfo;

    @Builder
    private CartItemInfoResponseDto(
            long cartItemId,
            int quantity,
            int subtotalPrice,
            ProductInfoOfCartItemDto productInfo,
            ProductOptionGroupInfoOfCartItemDto productOptionGroupInfo,
            OptionDetailInfoOfCartItemDto optionDetailInfo) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
        this.subtotalPrice = subtotalPrice;
        this.productInfo = productInfo;
        this.productOptionGroupInfo = productOptionGroupInfo;
        this.optionDetailInfo = optionDetailInfo;
    }

    public static CartItemInfoResponseDto of(CartItem cartItem) {
        int quantity = cartItem.getQuantity();
        OptionDetail optionDetail = cartItem.getProductOptionDetail();
        ProductOptionGroup productOptionGroup = optionDetail.getProductOptionGroup();
        Product product = productOptionGroup.getProduct();

        return CartItemInfoResponseDto.builder()
                .cartItemId(cartItem.getId())
                .quantity(quantity)
                .subtotalPrice(optionDetail.getSaleUnitPrice() * quantity)
                .productInfo(ProductInfoOfCartItemDto.of(product))
                .productOptionGroupInfo(ProductOptionGroupInfoOfCartItemDto.of(productOptionGroup))
                .optionDetailInfo(OptionDetailInfoOfCartItemDto.of(optionDetail))
                .build();
    }
}

/*
[CartItem에서 뽑을 수 있는 DTO 필드]

cartItemId
quantity
subtotalPrice  // 금액 소계
 */

// ==========================================================================================

/*
[OptionDetail에서 뽑을 수 있는 DTO 필드] ✅ -> OptionDetailInfoOfCartItemDto

optionDetailId
size
soldOut (옵션 품절) (단, OptionDetail.isSoldOut() 등 메서드로 처리해서 받아야 함)
optionAppliedUnitPrice  // Product 원가 + OptionDetail 옵션 추가금
saleUnitPrice  // Product 원가에 할인률 적용 금액 + OptionDetail 옵션 추가금
 */

// ==========================================================================================

/*
[ProductOptionGroup에서 뽑을 수 있는 DTO 필드] ✅ -> ProductOptionGroupInfoOfCartItemDto

color
 */

// ==========================================================================================

/*
[Product에서 뽑을 수 있는 DTO 필드] ✅ -> ProductInfoOfCartItemDto

productId
productName
image
originalUnitPrice
discountRate
 */