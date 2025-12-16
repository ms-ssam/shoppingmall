document.addEventListener('DOMContentLoaded', function () {
    const popupContainer = document.getElementById('cart-edit-popup-container');

    document.addEventListener('click', function (e) {
        const target = e.target.closest('.cart-option-edit-btn');
        if (!target) return;

        const cartItemId = target.dataset.cartItemId;
        if (!cartItemId) return;

        const url = `/cart/${cartItemId}/option`;

        fetch(url, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(res => {
                if (!res.ok) throw new Error('팝업 로딩 실패');
                return res.text();
            })
            .then(html => {
                popupContainer.innerHTML = '';
                popupContainer.insertAdjacentHTML('beforeend', html);

                // 오버레이 바깥 클릭시 닫기
                const overlay = popupContainer.querySelector('.cart-edit-overlay');
                if (overlay) {
                    overlay.addEventListener('click', function (e) {
                        if (e.target === overlay) overlay.remove();
                    });
                }

                // 팝업 DOM 생성 후 옵션 이벤트 세팅
                window.initCartOptionPopup();
            })
            .catch(err => {
                console.error(err);
                alert('옵션 변경 팝업을 여는 중 오류가 발생했습니다.');
            });
    });
});

// ===========================
// 팝업 내부 기능 (전역 함수)
// ===========================

// 수량 버튼
window.changeCartQty = function (delta) {
    const input = document.getElementById('cartEditQtyInput');
    let value = parseInt(input.value || '1', 10);
    value += delta;
    if (value < 1) value = 1;
    input.value = value;
};

// 팝업 옵션 초기화
window.initCartOptionPopup = function () {
    const groupSelect  = document.getElementById('cartOptionGroupSelect');
    const detailSelect = document.getElementById('cartOptionDetailSelect');

    if (!groupSelect || !detailSelect) return;

    function resetDetail() {
        detailSelect.innerHTML = '';
        const opt = document.createElement('option');
        opt.value = '';
        opt.textContent = '사이즈를 선택하세요';
        detailSelect.appendChild(opt);
    }

    function renderDetail(details) {
        resetDetail();
        details.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = d.name;
            detailSelect.appendChild(opt);
        });
    }

    groupSelect.addEventListener('change', function () {
        const groupId = this.value;
        resetDetail();
        if (!groupId) return;

        fetch('/api/cart/groups/' + groupId + '/details', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        })
            .then(res => {
                if (!res.ok) throw new Error('옵션 조회 실패');
                return res.json();
            })
            .then(data => renderDetail(data))
            .catch(err => {
                console.error(err);
                alert('옵션 정보를 불러오지 못했습니다.');
            });
    });
};

// 팝업 닫기
window.closeCartEditPopup = function () {
    const overlay = document.querySelector('.cart-edit-overlay');
    if (overlay) overlay.remove();
};

// 제출 검증
window.validateCartEditForm = function () {
    const groupSelect  = document.getElementById('cartOptionGroupSelect');
    const detailSelect = document.getElementById('cartOptionDetailSelect');
    const qtyInput     = document.getElementById('cartEditQtyInput');

    if (!groupSelect.value) {
        alert('색상을 선택해주세요.');
        groupSelect.focus();
        return false;
    }

    if (!detailSelect.value) {
        alert('사이즈를 선택해주세요.');
        detailSelect.focus();
        return false;
    }

    const qty = parseInt(qtyInput.value, 10);
    if (!qty || qty < 1) {
        alert('수량은 1 이상이어야 합니다.');
        qtyInput.focus();
        return false;
    }
    return true;
};
