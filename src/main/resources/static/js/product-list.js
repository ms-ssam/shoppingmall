// CSRF 토큰 가져오기
function getCsrfToken() {
    return document.querySelector("meta[name='_csrf']")?.getAttribute("content");
}

function getCsrfHeader() {
    return document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");
}

// 정렬 변경 함수
function changeSort(currentCategoryId) {
    const sortType = document.getElementById('sortSelect').value;
    let url = '/products?sortType=' + sortType;

    if (currentCategoryId) {
        url += '&categoryId=' + currentCategoryId;
    }
    location.href = url;
}

// 찜 토글 함수
function toggleWish(event, productId) {
    event.stopPropagation();

    const iconBox = event.currentTarget;
    const icon = iconBox.querySelector('i');
    const isActive = iconBox.classList.contains('active');

    const csrfToken = getCsrfToken();
    const csrfHeader = getCsrfHeader();

    const method = isActive ? 'DELETE' : 'POST';
    const url = isActive ? `/api/products/${productId}/wish` : `/api/products/${productId}/wishList`;

    const headers = { 'Content-Type': 'application/json' };
    if (csrfHeader && csrfToken) {
        headers[csrfHeader] = csrfToken;
    }

    fetch(url, { method: method, headers: headers })
        .then(response => {
            if (response.ok) {
                iconBox.classList.toggle('active');
                if (isActive) {
                    icon.className = 'bi bi-heart';
                } else {
                    icon.className = 'bi bi-heart-fill';
                }
            } else if (response.status === 401) {
                if(confirm('로그인이 필요한 서비스입니다. 로그인 하시겠습니까?')) {
                    location.href = '/login';
                }
            } else {
                alert('오류가 발생했습니다.');
            }
        })
        .catch(error => console.error('Error:', error));
}

// 이미지 에러 처리
function setupImageErrorHandling() {
    document.querySelectorAll('img').forEach(img => {
        if (!img.dataset.errorBound) {
            img.dataset.errorBound = 'true';
            img.addEventListener('error', handleImageError);
        }
    });
}

function handleImageError(event) {
    const img = event.target;
    if (!img.dataset.errorHandled) {
        img.dataset.errorHandled = 'true';
        img.src = '/images/default.jpg';
        img.alt = '이미지를 불러올 수 없습니다';
    }
}

// DOMContentLoaded 이벤트
document.addEventListener('DOMContentLoaded', setupImageErrorHandling);
