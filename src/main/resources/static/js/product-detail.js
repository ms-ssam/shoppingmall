const optionGroups = window.optionGroups;
const basePrice = window.basePrice;

// --- Swiper 연동 (Thumbs Gallery) ---
var swiperThumbs = new Swiper(".mySwiper", {
    spaceBetween: 10,
    slidesPerView: 5,
    freeMode: true,
    watchSlidesProgress: true,
});

var swiperMain = new Swiper(".mySwiper2", {
    spaceBetween: 10,
    navigation: {
        nextEl: ".swiper-button-next",
        prevEl: ".swiper-button-prev",
    },
    thumbs: {
        swiper: swiperThumbs,
    },
});

// --- 옵션 선택 및 가격 계산 ---
let selectedItems = [];

function handleColorChange() {
    const colorSelect = document.getElementById('colorSelect');
    const sizeSelect = document.getElementById('sizeSelect');
    const groupId = colorSelect.value;

    sizeSelect.innerHTML = '<option value="">사이즈 선택</option>';
    sizeSelect.disabled = true;

    if (!groupId) {
        sizeSelect.innerHTML = '<option value="">먼저 색상을 선택하세요</option>';
        return;
    }

    const group = optionGroups.find(g => g.id == groupId);
    if (group && group.details) {
        sizeSelect.disabled = false;
        group.details.forEach(detail => {
            const opt = document.createElement('option');
            opt.value = detail.id;

            if (detail.stockQuantity <= 0) {
                opt.text = `${detail.name} (품절)`;
                opt.disabled = true;
                opt.style.color = '#999';
            } else if (detail.stockQuantity <= 5) {
                const priceText = detail.addPrice > 0 ? ` (+${detail.addPrice.toLocaleString()}원)` : '';
                opt.text = `${detail.name}${priceText} [재고 ${detail.stockQuantity}개]`;
            } else {
                const priceText = detail.addPrice > 0 ? ` (+${detail.addPrice.toLocaleString()}원)` : '';
                opt.text = `${detail.name}${priceText}`;
            }

            opt.dataset.price = basePrice + detail.addPrice;
            opt.dataset.stock = detail.stockQuantity;
            opt.dataset.name = `${group.name} / ${detail.name}`;
            sizeSelect.add(opt);
        });
    }
}

function addOption() {
    const sizeSelect = document.getElementById('sizeSelect');
    const detailId = sizeSelect.value;
    if (!detailId) return;

    if (selectedItems.find(item => item.id === detailId)) {
        alert("이미 선택된 옵션입니다.");
        return;
    }

    const optionEl = sizeSelect.options[sizeSelect.selectedIndex];
    const name = optionEl.dataset.name;
    const price = parseInt(optionEl.dataset.price);
    const stock = parseInt(optionEl.dataset.stock);

    if (stock <= 0) {
        alert("품절된 상품입니다.");
        return;
    }

    selectedItems.push({
        id: detailId,
        name: name,
        price: price,
        qty: 1,
        stock: stock
    });
    renderSelectedOptions();
    sizeSelect.value = "";
}

function changeQty(index, delta) {
    const item = selectedItems[index];
    const newQty = item.qty + delta;

    if (newQty < 1) return;

    if (newQty > item.stock) {
        alert(`재고가 부족합니다. (남은 재고: ${item.stock}개)`);
        return;
    }

    item.qty = newQty;
    renderSelectedOptions();
}

function renderSelectedOptions() {
    const container = document.getElementById('selected-options');
    container.innerHTML = "";

    selectedItems.forEach((item, index) => {
        const html = `
            <div class="sel-item">
                <div class="sel-item-top">
                    <span>${item.name}</span>
                    <i class="bi bi-x-lg sel-close" onclick="removeOption(${index})"></i>
                </div>
                <div class="sel-item-bottom">
                    <div class="qty-box">
                        <button class="qty-btn" onclick="changeQty(${index}, -1)">-</button>
                        <input type="text" class="qty-input" value="${item.qty}" readonly>
                        <button class="qty-btn" onclick="changeQty(${index}, 1)">+</button>
                    </div>
                    <span class="sel-price">${(item.price * item.qty).toLocaleString()}원</span>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', html);
    });
    syncHiddenFields();
    updateTotal();
}

function syncHiddenFields() {
    const cartHiddenContainer = document.getElementById('cart-hidden-fields');
    const buyHiddenContainer = document.getElementById('buy-hidden-fields');

    cartHiddenContainer.innerHTML = "";
    buyHiddenContainer.innerHTML = "";

    if (selectedItems.length === 0) {
        return;
    }

    selectedItems.forEach(item => {
        // 장바구니용 hidden 필드
        const cartHiddenId = document.createElement('input');
        cartHiddenId.type = 'hidden';
        cartHiddenId.name = 'optionDetailIds';
        cartHiddenId.value = item.id;

        const cartHiddenQty = document.createElement('input');
        cartHiddenQty.type = 'hidden';
        cartHiddenQty.name = 'quantities';
        cartHiddenQty.value = item.qty;

        cartHiddenContainer.appendChild(cartHiddenId);
        cartHiddenContainer.appendChild(cartHiddenQty);

        // 구매하기용 hidden 필드
        const buyHiddenId = document.createElement('input');
        buyHiddenId.type = 'hidden';
        buyHiddenId.name = 'optionDetailIds';
        buyHiddenId.value = item.id;

        const buyHiddenQty = document.createElement('input');
        buyHiddenQty.type = 'hidden';
        buyHiddenQty.name = 'quantities';
        buyHiddenQty.value = item.qty;

        buyHiddenContainer.appendChild(buyHiddenId);
        buyHiddenContainer.appendChild(buyHiddenQty);
    });
}

function removeOption(index) {
    selectedItems.splice(index, 1);
    renderSelectedOptions();
}

function updateTotal() {
    const total = selectedItems.reduce((sum, item) => sum + (item.price * item.qty), 0);
    document.getElementById('totalPrice').innerText = total.toLocaleString() + "원";
}

// --- 상세 정보 더보기 버튼 ---
function toggleDesc() {
    const wrap = document.getElementById('descListWrap');
    const btn = document.querySelector('.btn-more');

    if (wrap.classList.contains('expanded')) {
        wrap.classList.remove('expanded');
        btn.innerHTML = '상세 정보 더보기 <i class="bi bi-chevron-down"></i>';
    } else {
        wrap.classList.add('expanded');
        btn.innerHTML = '상세 정보 접기 <i class="bi bi-chevron-up"></i>';
    }
}

// --- 찜 토글 ---
function toggleWish(pId) {
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    const btn = document.querySelector('.wish-btn');
    const isLiked = btn.classList.contains('active');
    const method = isLiked ? 'DELETE' : 'POST';
    const url = isLiked ? `/api/products/${pId}/wish` : `/api/products/${pId}/wishList`;

    const headers = { 'Content-Type': 'application/json' };
    if (csrfHeader) headers[csrfHeader] = csrfToken;

    fetch(url, { method: method, headers: headers })
        .then(res => {
            if (res.ok) {
                btn.classList.toggle('active');
                btn.classList.toggle('bi-heart-fill');
                btn.classList.toggle('bi-heart');
            } else if (res.status === 401) {
                if (confirm('로그인이 필요합니다. 이동하시겠습니까?')) location.href = '/login';
            }
        });
}

// --- 상품 품절 체크 ---
function checkProductAvailability() {
    const productData = window.productData;
    if (!productData) return;

    const soldoutBtn = document.getElementById('soldoutBtn');
    const cartForm = document.getElementById('cartForm');
    const buyForm = document.getElementById('buyNowForm');

    const isSoldOut = productData.status === 'SOLD_OUT' || productData.totalStock <= 0;

    if (isSoldOut) {
        // 품절 버튼 표시
        if (soldoutBtn) {
            soldoutBtn.style.display = 'block';
        }

        // 장바구니, 구매하기 폼 숨기기
        if (cartForm) {
            cartForm.style.display = 'none';
        }
        if (buyForm) {
            buyForm.style.display = 'none';
        }
    } else {
        // 정상 상태: 품절 버튼 숨기고 폼 표시
        if (soldoutBtn) {
            soldoutBtn.style.display = 'none';
        }
        if (cartForm) {
            cartForm.style.display = 'block';
        }
        if (buyForm) {
            buyForm.style.display = 'block';
        }
    }
}

// --- DOMContentLoaded: 페이지 로드 시 초기화 ---
document.addEventListener('DOMContentLoaded', function () {
    // 상품 품절 체크
    checkProductAvailability();

    // 탭 메뉴 설정
    const tabItems = document.querySelectorAll('.tab-item');
    const tabContents = {
        detail: document.getElementById('tab-detail'),
        review: document.getElementById('tab-review'),
        qna: document.getElementById('tab-qna')
    };

    tabItems.forEach(tab => {
        tab.addEventListener('click', function () {
            const target = this.getAttribute('data-target');

            // 탭 active 변경
            tabItems.forEach(t => t.classList.remove('active'));
            this.classList.add('active');

            // 컨텐츠 show/hide
            Object.keys(tabContents).forEach(key => {
                if (key === target) {
                    tabContents[key].classList.add('active');
                } else {
                    tabContents[key].classList.remove('active');
                }
            });
        });
    });

    // 장바구니 폼 제출 시 검증
    const cartForm = document.getElementById('cartForm');
    if (cartForm) {
        cartForm.addEventListener('submit', function (e) {
            if (selectedItems.length === 0) {
                alert("옵션을 최소 1개 이상 선택해 주세요.");
                e.preventDefault();
                return;
            }
        });
    }

    // 구매하기 폼 제출 시 검증
    const buyNowForm = document.getElementById('buyNowForm');
    if (buyNowForm) {
        buyNowForm.addEventListener('submit', function (e) {
            if (selectedItems.length === 0) {
                alert("옵션을 최소 1개 이상 선택해 주세요.");
                e.preventDefault();
                return;
            }
        });
    }

    // 장바구니 담기 성공 시 모달 띄우기
    const params = new URLSearchParams(window.location.search);
    const added = params.get("addedToCart");

    if (added === "true") {
        const modal = document.getElementById("cart-added-modal");
        if (modal) {
            modal.style.display = "flex";

            const btnContinue = document.getElementById("btn-continue-shopping");
            const btnGoCart = document.getElementById("btn-go-cart");

            if (btnContinue) {
                btnContinue.addEventListener("click", function () {
                    modal.style.display = "none";

                    // URL에서 addedToCart 파라미터 제거
                    if (window.history.replaceState) {
                        const url = new URL(window.location);
                        url.searchParams.delete("addedToCart");
                        window.history.replaceState({}, document.title, url);
                    }
                });
            }

            if (btnGoCart) {
                btnGoCart.addEventListener("click", function () {
                    window.location.href = "/cart";
                });
            }
        }
    }

});

document.addEventListener('DOMContentLoaded', function () {
    const buyNowForm = document.getElementById('buyNowForm');
    const cartHidden = document.getElementById('cart-hidden-fields');
    const buyHidden = document.getElementById('buy-hidden-fields');

    if (!buyNowForm || !cartHidden || !buyHidden) {
        return;
    }

    // 구매하기 폼 제출 시, 장바구니 폼의 hidden 필드를 그대로 복사
    buyNowForm.addEventListener('submit', function () {
        buyHidden.innerHTML = cartHidden.innerHTML;
    });
});
