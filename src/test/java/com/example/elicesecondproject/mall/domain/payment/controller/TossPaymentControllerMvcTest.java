package com.example.elicesecondproject.mall.domain.payment.controller;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.entity.Role;
import com.example.elicesecondproject.mall.domain.order.dto.response.DeliveryInfoResponse;
import com.example.elicesecondproject.mall.domain.order.dto.response.UserOrderDetailResponse;
import com.example.elicesecondproject.mall.domain.payment.service.TossPaymentService;
import com.example.elicesecondproject.mall.global.config.JpaAuditingConfig;
import com.example.elicesecondproject.mall.global.config.WebMvcConfig;
import com.example.elicesecondproject.mall.global.security.entity.MemberDetail;
import com.example.elicesecondproject.mall.global.web.GlobalModelAttributeAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(
        controllers = TossPaymentController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalModelAttributeAdvice.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaAuditingConfig.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TossPaymentControllerMvcTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    TossPaymentService tossPaymentService;


    // === FAIL TEST ===
    @Nested
    @DisplayName("/payments/toss/fail")
    class Fail {

        @Test
        @DisplayName("/payments/toss/fail 주소가 호출되면 handleFail 메서드가 호출되고 주문서 작성 페이지로 리다이렉트 시켜준다.")
        void fail_redirectsToOrderSheet_withFlashMessage() throws Exception {
            // === given ===
            Long memberId = 1L;
            String orderId = "ORDER_CODE_123";
            Long orderPk = 10L;

            // fail 처리 후 준비한 orderPK를 반환하도록 설정
            when(tossPaymentService.handleFail(orderId, memberId)).thenReturn(orderPk);


            // === when + then ===
            // /payments/toss/fail 주소 호출 시 redirect URL, flash message가 제대로 나오는지 검증
            mockMvc.perform(get("/payments/toss/fail")
                            .param("code", "PAY_PROCESS_CANCELED")
                            .param("message", "cancel")
                            .param("orderId", orderId)
                            .with(loginMember(memberId))
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders/" + orderPk + "/sheet"))
                    .andExpect(flash().attributeExists("errorMessage"));

            // 위의 설정대로 /payments/toss/fail 주소 호출했을 시 기대한 파라미터로 handleFail 메서드가 호출되는지 검증
            verify(tossPaymentService).handleFail(orderId, memberId);
        }
    }

    // === SUCCESS TEST ===
    @Nested
    @DisplayName("/payments/toss/success")
    class Success {

        @Test
        @DisplayName("/payments/toss/success 주소가 호출되고 handleSuccess 메서드가 정상 응답을 내려준다면 주문 완료 페이지를 렌더링해준다.")
        void success_returnsOrderCompleteView_withModelOrder() throws Exception {
            // === given ===
            Long memberId = 1L;
            String orderId = "ORDER_CODE_123";

            // handleSuccess 메서드 호출 시 반환 응답 설정
            UserOrderDetailResponse response = mock(UserOrderDetailResponse.class);
            DeliveryInfoResponse deliveryInfo = mock(DeliveryInfoResponse.class);
            when(deliveryInfo.getReceiverName()).thenReturn("홍길동");
            when(response.getDeliveryInfo()).thenReturn(deliveryInfo);
            when(response.getOrderItems()).thenReturn(List.of());

            when(tossPaymentService.handleSuccess(eq("PAY_KEY"), eq(orderId), eq(10000L), eq(memberId)))
                    .thenReturn(response);



            // === when + then ===
            // /payments/toss/success 주소 호출 + 성공 시 order/order-complete 뷰를 내려주는지와 model에 order 담는지 검증
            mockMvc.perform(get("/payments/toss/success")
                            .param("paymentKey", "PAY_KEY")
                            .param("orderId", orderId)
                            .param("amount", "10000")
                            .with(loginMember(memberId))
                    )
                    .andExpect(status().isOk())
                    .andExpect(view().name("order/order-complete"))
                    .andExpect(model().attributeExists("order"));

            // handleSuccess가 제대로 호출되었는지 검증
            verify(tossPaymentService).handleSuccess("PAY_KEY", orderId, 10000L, memberId);
        }

        @Test
        @DisplayName("/payments/toss/success 주소가 호출되고 handleSuccess 메서드에서 예외가 발생한다면 " +
                "handleFail 메서드가 호출되고 에러 메시지와 함께 주문서 작성 페이지를 렌더링해준다.")
        void success_whenServiceThrows_redirectsToOrderSheet() throws Exception {
            // === given ===
            Long memberId = 1L;
            String orderId = "ORDER_CODE_123";
            Long orderPk = 10L;

            // handleSuccess가 호출되면 예외를 던지도록 설정
            when(tossPaymentService.handleSuccess(anyString(), anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("boom"));

            // 예외 처리 중 handleFail 메서드 호출 시 반환 응답 설정
            when(tossPaymentService.handleFail(orderId, memberId)).thenReturn(orderPk);



            // === when + then ===
            // /payments/toss/success 주소 호출 시 /orders/" + orderPk + "/sheet 주소로 redirect하고 flash message가 제대로 나오는지 검증
            mockMvc.perform(get("/payments/toss/success")
                            .param("paymentKey", "PAY_KEY")
                            .param("orderId", orderId)
                            .param("amount", "10000")
                            .with(loginMember(memberId))
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/orders/" + orderPk + "/sheet"))
                    .andExpect(flash().attributeExists("errorMessage"));

            // 예외 시 handleFail 메서드가 호출되는지 검증
            verify(tossPaymentService).handleFail(orderId, memberId);
        }
    }



    // === Helper Method ===
    private RequestPostProcessor loginMember(Long memberId) {
        return request -> {
            Member member = mock(Member.class);
            when(member.getId()).thenReturn(memberId);
            when(member.getRole()).thenReturn(Role.USER);
            when(member.getStatus()).thenReturn(MemberStatus.ACTIVE);

            MemberDetail principal = new MemberDetail(member);

            Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            return request;
        };
    }
}
