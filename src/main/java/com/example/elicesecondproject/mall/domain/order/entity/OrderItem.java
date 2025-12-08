package com.example.elicesecondproject.mall.domain.order.entity;

import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private Long productId;
    private String productName;
    private String productThumbnailUrl;

    private Long optionDetailId;
    private String optionDetail;

    private String productOptionGroupName;
    private int unitPrice;
    private int quantity;
    private int subtotalPrice;


    public static OrderItem fromCartItem(CartItem cartItem, Order order) {
        OrderItem item = new OrderItem();

        item.order = order;

        // CartItem -> OptionDetail -> ProductOptionGroup -> Product
        OptionDetail detail = cartItem.getProductOptionDetail();
        ProductOptionGroup group = detail.getProductOptionGroup();
        Product product = group.getProduct();

        item.productId = product.getId();
        item.productName = product.getName();
        item.productThumbnailUrl = product.getThumbnailUrl();      // 실제 필드/메서드 이름에 맞게 수정

        item.optionDetailId = detail.getId();
        item.optionDetail = detail.getName();                        // 예: "블랙 / M" 같은 표시용 이름

        item.productOptionGroupName = group.getName();               // 예: "색상", "사이즈"

        // 단가/수량/소계
        item.unitPrice = product.getPrice();                          // 옵션가 or 옵션 포함 실제 판매가
        item.quantity = cartItem.getQuantity();
        item.subtotalPrice = item.unitPrice * item.quantity;

        return item;
    }
}
