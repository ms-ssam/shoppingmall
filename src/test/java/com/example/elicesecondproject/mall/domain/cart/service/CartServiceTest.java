package com.example.elicesecondproject.mall.domain.cart.service;

import com.example.elicesecondproject.mall.domain.cart.dto.request.AddCartItemRequest;
import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.option.repository.OptionDetailRepository;
import com.example.elicesecondproject.mall.domain.option.repository.ProductOptionGroupRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CartServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CartService cartService;

    @Autowired
    OptionDetailRepository optionDetailRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOptionGroupRepository productOptionGroupRepository;

    @Autowired
    CategoryRepository categoryRepository;

    Member member;
    Cart cart;
    OptionDetail optionDetail;
    Long memberId;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 생성 (실제 빌더 사용)
        member = Member.builder()
                .email("test@test.com")
                .password("encoded-password")
                .name("테스트회원")
                .nickname("tester")
                .phone("01012345678")
                .role(Role.USER)
                .build();

        // 장바구니 생성 및 연관관계 연결
        Cart newCart = Cart.create();
        member.assignCart(newCart);

        // 회원 저장 → Cart도 같이 저장
        memberRepository.save(member);

        memberId = member.getId();
        cart = member.getCart();

        // 테스트용 옵션 생성 (실제 팩토리/생성자에 맞게 수정)
        optionDetail = createOptionDetailWithStock(10);
        optionDetailRepository.save(optionDetail);
    }

    private OptionDetail createOptionDetailWithStock(int stock) {
        // 1) 테스트용 카테고리
        Category category = Category.builder()
                .name("테스트 카테고리")
                .slug("test-category")
                .isVisible(true)
                .displayOrder(1)
                .build();
        categoryRepository.save(category);

        // 2) 테스트용 상품
        Product product = new Product(
                "테스트 상품",
                10000,           // price
                0,               // discountRate
                "테스트용 상품입니다.",
                category,
                ProductStatus.SELLING
        );
        productRepository.save(product);

        // 3) 옵션 그룹 (예: 색상)
        ProductOptionGroup group = ProductOptionGroup.builder()
                .name("컬러")
                .displayOrder(1)
                .build();
        group.initProduct(product);
        productOptionGroupRepository.save(group);

        // 4) 옵션 디테일 (예: L 사이즈)
        OptionDetail detail = OptionDetail.builder()
                .name("L")
                .sku("TEST-SKU-001")
                .addPrice(0)
                .stockQuantity(stock)
                .displayOrder(1)
                .build();
        detail.initProductOptionGroup(group);

        return optionDetailRepository.save(detail);
    }

    @Test
    @DisplayName("장바구니에 없는 옵션을 담으면 CartItem이 생성된다")
    void addItemToCart_newOption() {
        // given
        AddCartItemRequest request = new AddCartItemRequest();
        request.setOptionDetailIds(List.of(optionDetail.getId()));
        request.setQuantities(List.of(3));

        // when
        cartService.addItemToCart(memberId, request);

        // then
        Optional<CartItem> cartItem =
                cartItemRepository.findByCartIdAndProductOptionDetailId(
                        cart.getId(), optionDetail.getId()
                );

        assertThat(cartItem).isPresent();
        assertThat(cartItem.get().getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("이미 담긴 옵션이면 수량만 증가한다")
    void addItemToCart_existingOption() {
        // given
        CartItem existing = CartItem.of(optionDetail, 2);
        cart.addItem(existing);
        cartItemRepository.save(existing);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setOptionDetailIds(List.of(optionDetail.getId()));
        request.setQuantities(List.of(3));

        // when
        cartService.addItemToCart(memberId, request);

        // then
        CartItem updated =
                cartItemRepository.findByCartIdAndProductOptionDetailId(
                        cart.getId(), optionDetail.getId()
                ).orElseThrow();

        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("재고를 초과하면 NOT_ENOUGH_STOCK 예외가 발생한다")
    void addItemToCart_notEnoughStock() {
        // given
        CartItem existing = CartItem.of(optionDetail, 8);
        cart.addItem(existing);
        cartItemRepository.save(existing);

        AddCartItemRequest request = new AddCartItemRequest();
        request.setOptionDetailIds(List.of(optionDetail.getId()));
        request.setQuantities(List.of(5)); // 총 13 > 재고 10

        // when & then
        assertThatThrownBy(() -> cartService.addItemToCart(memberId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode", ErrorCode.NOT_ENOUGH_STOCK
                );

        // 수량 롤백 확인
        CartItem unchanged =
                cartItemRepository.findByCartIdAndProductOptionDetailId(
                        cart.getId(), optionDetail.getId()
                ).orElseThrow();

        assertThat(unchanged.getQuantity()).isEqualTo(8);
    }
}
