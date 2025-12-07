const optionGroups = window.optionGroups;
const basePrice = window.basePrice;

// --- Swiper 연동 (Thumbs Gallery) ---
var swiperThumbs = new Swiper(".mySwiper", {
    spaceBetween: 10,
    slidesPerView: 5, // 5개 보이게 설정 (이미지가 3개면 3개만 보임)
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
            } else {
                opt.text = detail.name + (detail.addPrice > 0 ? ` (+${detail.addPrice}원)` : '');
            }

            opt.dataset.price = basePrice + detail.addPrice;
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
        sizeSelect.value = "";
        return;
    }

    const optionEl = sizeSelect.options[sizeSelect.selectedIndex];
    const name = optionEl.dataset.name;
    const price = parseInt(optionEl.dataset.price);

    selectedItems.push({ id: detailId, name: name, price: price, qty: 1 });
    renderSelectedOptions();
    sizeSelect.value = "";
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
    updateTotal();
}

function changeQty(index, delta) {
    const item = selectedItems[index];
    if (item.qty + delta < 1) return;
    item.qty += delta;
    renderSelectedOptions();
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

// --- 탭 전환 ---
document.addEventListener('DOMContentLoaded', function () {
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
});