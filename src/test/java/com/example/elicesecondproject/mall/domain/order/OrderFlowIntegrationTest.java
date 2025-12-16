package com.example.elicesecondproject.mall.domain.order;

import com.example.elicesecondproject.mall.domain.cart.entity.Cart;
import com.example.elicesecondproject.mall.domain.cart.entity.CartItem;
import com.example.elicesecondproject.mall.domain.cart.repository.CartItemRepository;
import com.example.elicesecondproject.mall.domain.cart.repository.CartRepository;
import com.example.elicesecondproject.mall.domain.category.entity.Category;
import com.example.elicesecondproject.mall.domain.category.repository.CategoryRepository;
import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.option.repository.OptionDetailRepository;
import com.example.elicesecondproject.mall.domain.option.repository.ProductOptionGroupRepository;
import com.example.elicesecondproject.mall.domain.order.entity.Order;
import com.example.elicesecondproject.mall.domain.order.repository.OrderRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductStatus;
import com.example.elicesecondproject.mall.domain.product.repository.ProductRepository;
import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트가 끝나고 DB 롤백을 위함
@ActiveProfiles("test")
public class OrderFlowIntegrationTest {

    // 실제 서버를 띄우는 것처럼 HTTP 요청을 보냄, 컨트롤러를 호출
    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    ProductOptionGroupRepository optionGroupRepository;
    @Autowired
    OptionDetailRepository optionDetailRepository;
    @Autowired
    CategoryRepository categoryRepository;

    // 공통 준비 로직을 헬퍼 메서드로 빼서 테스트의 가독성, 유지보수성을 높임
    // =========================
    // 헬퍼: 로그인 principal 만들기
    // =========================
    private MemberDetail loginPrincipal(Member member) {
        return new MemberDetail(member);
    }

    // =========================
    // 헬퍼: 테스트 픽스처 구성
    // =========================
    private Member saveMember(String email) {
        Member member = Member.builder()
                .email(email)
                .password("encoded-pw")
                .name("테스터")
                .nickname("test")
                .phone("01000000000")
                .build();
        Cart cart = Cart.create();
        member.assignCart(cart);
        return memberRepository.save(member);
    }

    private Category saveRootCategory(String name, String slug) {
        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .displayOrder(1)
                .isVisible(true)
                .build();

        Category saved = categoryRepository.save(category); // id 생성
        ReflectionTestUtils.setField(saved, "path", "/" + saved.getId() + "/");                              // "/{id}/" 형태로 완성
        return categoryRepository.save(saved);              // path 업데이트 반영
    }

    private OptionDetail saveSellingProductWithOption(Category category) {
        Product product = productRepository.save(new Product(
                "테스트상품",
                100,
                0,
                "상품설명",
                category,
                ProductStatus.SELLING
        ));

        // 1) 그룹은 product에 먼저 연결 후 저장 (저장은 1번만)
        ProductOptionGroup optionGroup = ProductOptionGroup.builder()
                .name("색상")
                .build();
        product.addOptionGroup(optionGroup);
        optionGroupRepository.save(optionGroup);

        // 2) 디테일도 그룹에 먼저 연결 후 저장
        OptionDetail optionDetail = OptionDetail.builder()
                .name("사이즈")
                .stockQuantity(10)
                .sku("sku")
                .addPrice(0)
                .build();
        optionGroup.addDetail(optionDetail);
        optionDetailRepository.save(optionDetail);

        return optionDetail;
    }

    private List<Long> saveCartItemsForMember(Member member) {

        Cart cart = member.getCart();

        Category category = saveRootCategory("카테고리", "슬러그");
        OptionDetail optionDetail = saveSellingProductWithOption(category);

        CartItem cartItem = CartItem.of(optionDetail, 2);
        cart.addItem(cartItem);

        CartItem savedCartItem = cartItemRepository.save(cartItem);

        return List.of(savedCartItem.getId());
    }

    // 다른 회원의 cartItem을 하나 만들어서 id만 반환
    private Long saveCartItemForOtherMember(Member other) {
        return saveCartItemsForMember(other).get(0);
    }

    // redirect:/orders/{id}/payment 에서 id 추출
    private Long extractOrderIdFromRedirectUrl(String redirectUrl) {
        Pattern pattern = Pattern.compile("^/orders/(\\d+)/payment$");
        Matcher matcher = pattern.matcher(redirectUrl);
        assertThat(matcher.find()).isTrue();
        return Long.parseLong(matcher.group(1));
    }

    @Nested
    @DisplayName("장바구니 → 주문서 → 주문 플로우")
    class Flow {

        @Test
        @DisplayName("성공: 주문서 생성 후 주문 생성하면 결제 페이지로 리다이렉트되고 Order가 저장된다")
        void success_cart_to_sheet_to_order() throws Exception {
            // given
            Member member = saveMember("userA@test.com");
            List<Long> cartItemIds = saveCartItemsForMember(member);

            // 1) 장바구니 -> 주문서
            mockMvc.perform(
                            post("/orders/sheet")
                                    .with(user(loginPrincipal(member)))
                                    .with(csrf())
                                    // form param으로 List<Long> 전달: cartItemIds=1&cartItemIds=2 형태
                                    .param("cartItemIds", cartItemIds.stream().map(String::valueOf).toArray(String[]::new))
                    )
                    .andExpect(status().isOk())
                    .andExpect(view().name("order/order-sheet"))
                    .andExpect(model().attributeExists("orderSheet"))
                    .andExpect(model().attributeExists("orderCreateRequest"));

            // 2) 주문서 -> 주문 생성
            String redirectUrl = mockMvc.perform(
                            post("/orders")
                                    .with(user(loginPrincipal(member)))
                                    .with(csrf())
                                    .param("receiverName", "홍길동")
                                    .param("receiverPhone", "01012345678")
                                    .param("zipCode", "12345")
                                    .param("address1", "서울시 강남구")
                                    .param("address2", "101동 1001호")
                                    .param("deliveryMemo", "문 앞에 놔주세요")
                                    .param("agreeTerms", "true")
                                    .param("cartItemIds", cartItemIds.stream().map(String::valueOf).toArray(String[]::new))
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/orders/*/payment"))
                    .andReturn()
                    .getResponse()
                    .getRedirectedUrl();

            Long orderId = extractOrderIdFromRedirectUrl(redirectUrl);

            // then: Order 저장 검증
            Order order = orderRepository.findById(orderId).orElse(null);
            assertThat(order).isNotNull();
            assertThat(order.getOwnerId()).isEqualTo(member.getId());
            assertThat(order.getOrderItems()).isNotEmpty(); // orderItem 스냅샷 생성 확인
        }

        @Test
        @DisplayName("실패1: 주문서 요청 시 cartItemIds 누락되면 /cart로 리다이렉트 + errorMessage")
        void fail_sheet_when_cartItemIds_missing() throws Exception {
            // given
            Member member = saveMember("userA@test.com");

            // when & then
            mockMvc.perform(
                            post("/orders/sheet")
                                    .with(csrf())
                                    .with(user(loginPrincipal(member)))
                            // cartItemIds 파라미터를 아예 안 보냄
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cart"))
                    .andExpect(flash().attributeExists("errorMessage"));
        }

        @Test
        @DisplayName("실패2: 다른 사람 cartItemIds로 주문서 요청하면 /cart로 리다이렉트 + errorMessage")
        void fail_sheet_when_cartItem_not_owned() throws Exception {
            // given
            Member memberA = saveMember("userA@test.com");
            Member memberB = saveMember("userB@test.com");

            Long otherCartItemId = saveCartItemForOtherMember(memberB);

            // when & then
            mockMvc.perform(
                            post("/orders/sheet")
                                    .with(csrf())
                                    .with(user(loginPrincipal(memberA)))
                                    .param("cartItemIds", String.valueOf(otherCartItemId))
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/cart"))
                    .andExpect(flash().attributeExists("errorMessage"));
        }

        @Test
        @DisplayName("실패3: 약관 미동의 상태로 주문 생성 시 주문서로 돌아가고 에러 메시지가 있다")
        void fail_order_when_terms_not_agreed() throws Exception {
            // given
            Member member = saveMember("userA@test.com");
            List<Long> cartItemIds = saveCartItemsForMember(member);

            // when & then
            mockMvc.perform(
                            post("/orders")
                                    .with(user(loginPrincipal(member)))
                                    .with(csrf())
                                    .param("receiverName", "홍길동")
                                    .param("receiverPhone", "01012345678")
                                    .param("zipCode", "12345")
                                    .param("address1", "서울시 강남구")
                                    .param("address2", "101동 1001호")
                                    .param("deliveryMemo", "문 앞에 놔주세요")
                                    //  agreeTerms 누락 또는 false
                                    .param("agreeTerms", "false")
                                    .param("cartItemIds", cartItemIds.stream()
                                            .map(String::valueOf)
                                            .toArray(String[]::new))
                    )
                    // 주문 생성 실패
                    .andExpect(status().isOk())
                    .andExpect(view().name("order/order-sheet"))
                    .andExpect(model().attributeExists("orderSheet"))
                    .andExpect(model().attributeExists("orderCreateRequest"))
                    .andExpect(model().attributeHasFieldErrors("orderCreateRequest", "agreeTerms"));
        }
    }
}
