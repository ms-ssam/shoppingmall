document.addEventListener('DOMContentLoaded', function () {
    const checkboxes = document.querySelectorAll('.cart-item-checkbox');
    const selectedCountEl = document.getElementById('selected-count');
    const subtotalEl = document.getElementById('summary-subtotal');
    const totalEl = document.getElementById('summary-total');

    const orderSheetForm = document.getElementById('orderSheetForm');
    const orderHiddenFields = document.getElementById('order-hidden-fields');
    const goOrderSheetBtn = document.getElementById('goOrderSheetBtn');

    function formatWon(value) {
        return '₩' + new Intl.NumberFormat('ko-KR').format(value);
    }

    function recalcSummary() {
        let count = 0;
        let sum = 0;

        checkboxes.forEach(cb => {
            if (cb.checked && !cb.disabled) {
                const parentCard = cb.closest('.cart-item-card');
                const subtotal = parseInt(parentCard.dataset.subtotal, 10) || 0;
                count++;
                sum += subtotal;
            }
        });

        selectedCountEl.textContent = count;
        subtotalEl.textContent = formatWon(sum);
        totalEl.textContent = formatWon(sum);

        if (goOrderSheetBtn) {
            goOrderSheetBtn.disabled = (count === 0);
        }
    }

    checkboxes.forEach(cb => cb.addEventListener('change', recalcSummary));

    if (goOrderSheetBtn && orderSheetForm && orderHiddenFields) {
        goOrderSheetBtn.addEventListener('click', function () {
            orderHiddenFields.innerHTML = '';

            checkboxes.forEach(cb => {
                if (cb.checked && !cb.disabled) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'cartItemIds';
                    input.value = cb.value;
                    orderHiddenFields.appendChild(input);
                }
            });

            if (!orderHiddenFields.hasChildNodes()) {
                alert('주문할 상품을 선택해주세요.');
                return;
            }

            orderSheetForm.submit();
        });
    }

    recalcSummary();
});
