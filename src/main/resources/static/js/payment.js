(function () {
    const cfg = window.__PAYMENT_PAGE__;
    if (!cfg) {
        console.error("Payment config not found: window.__PAYMENT_PAGE__");
        return;
    }

    const {
        clientKey,
        customerKey,
        successPath,
        failPath,
        order
    } = cfg;

    // 현재 브라우저 도메인 기준으로 URL 생성
    const successUrl = window.location.origin + successPath;
    const failUrl = window.location.origin + failPath;

    const orderId = order.orderId;
    const orderName = order.orderName;
    const amount = order.amount;

    const tossPayments = TossPayments(clientKey);
    const widgets = tossPayments.widgets({ customerKey });

    async function renderWidget() {
        await widgets.setAmount({
            currency: "KRW",
            value: amount
        });

        await Promise.all([
            widgets.renderPaymentMethods({ selector: "#payment-method" }),
            widgets.renderAgreement({ selector: "#agreement" })
        ]);
    }

    renderWidget().catch((e) => {
        console.error("renderWidget error:", e);
        alert("결제 위젯을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
    });

    const payButton = document.getElementById("pay-button");
    payButton.addEventListener("click", async () => {
        try {
            await widgets.requestPayment({
                orderId,
                orderName,
                successUrl,
                failUrl,
                customerEmail: order.ordererEmail,
                customerName: order.ordererName
            });
        } catch (error) {
            console.error("requestPayment error:", error);

            if (error && error.code === "USER_CANCEL") {
                alert("결제를 취소했습니다.");
                return;
            }

            const code = (error && error.code) ? error.code : "UNKNOWN";
            alert("결제 처리 중 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요.\n(" + code + ")");
        }
    });
})();
