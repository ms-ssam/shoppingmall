package com.example.elicesecondproject.mall.domain.cart.eventListener;

import com.example.elicesecondproject.mall.domain.cart.service.CartService;
import com.example.elicesecondproject.mall.domain.payment.event.TossPaymentSucceedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CartCleanupEventListener {
    private final CartService cartService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTossPaymentSucceedEvent(final TossPaymentSucceedEvent event) {
        cartService.deleteCartItemOnPaymentSucceed(event.memberId(), event.order());
    }
}
