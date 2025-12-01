package com.example.elicesecondproject.mall.domain.cart.entity;

import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cart_items")
@Entity
public class CartItem extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)  //!❗️ N+1 터질 가능성 높다고 판단됨. 조회할 때 fetch join 하거나 EntityGraph 고려 (장바구니 항목 조회하는 경우 보통 제품 옵션 및 정보 필요)
    @JoinColumn(name = "product_option_detail_id", nullable = false)
    private OptionDetail productOptionDetail;

    @Column(nullable = false)
    private int quantity;

    // === 조회, 계산용 메서드 ===
    public String getName() {  // 장바구니 항목은 실제 해당 제품과 동기화되어야 함 -> 자체 필드 대신 실제 제품 객체에서 가져오기
        return getProduct().getName();
    }

    public int getUnitPrice() {
        Product product = getProduct();
        int basePrice = product.getSalePrice();  // 제품 자체 할인률을 고려한 제품 가격
        int optionAdditionalPrice = productOptionDetail.getAddPrice();

        return basePrice + optionAdditionalPrice;
    }

    public int getTotalPrice() {
        return getUnitPrice() * quantity;
    }

    // === 정적 팩토리 메서드 ===
    public static CartItem of(OptionDetail optionDetail, int quantity) {
        if(optionDetail == null) {
            throw new IllegalArgumentException("옵션 정보는 필수입니다.");
        }

        if(quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        CartItem cartItem = new CartItem();
        cartItem.productOptionDetail = optionDetail;
        cartItem.quantity = quantity;

        return cartItem;
    }

    // === 연관관계 편의 메서드 ===
    protected void setCart(Cart cart) {
        this.cart = cart;
    }

    // === 헬퍼 메서드 ===
    private Product getProduct() {  //!❗️ 그냥 사용하면 N+1 발생하는 메서드 -> repo에서 CartItem 조회할 때 fetch join 혹은 EntityGraph 사용
        return productOptionDetail.getProductOptionGroup().getProduct();
    }
}
