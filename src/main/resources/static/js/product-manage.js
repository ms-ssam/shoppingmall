// ========================================
// 상품 관리 JavaScript
// ========================================

// 현재 URL에서 파라미터 가져오기
function getCurrentParams() {
    const urlParams = new URLSearchParams(window.location.search);
    return {
        keyword: urlParams.get('keyword') || '',
        sortType: urlParams.get('sortType') || 'LATEST'
    };
}

// ========================================
// 1. 검색 기능
// ========================================
function searchProducts() {
    const input = document.getElementById('searchInput');
    const keyword = input.value.trim();
    const errorMsg = document.getElementById('searchError');
    const sortType = document.getElementById('sortSelect').value;

    // 검색어가 비어있으면 전체 검색
    if (!keyword) {
        errorMsg.classList.add('hidden');
        window.location.href = `/admin/products?sortType=${sortType}`;
        return;
    }

    // 검색어가 2글자 미만이면 경고 표시
    if (keyword.length < 2) {
        errorMsg.innerHTML = `
            <i class="bi bi-exclamation-circle mr-1"></i>
            검색어는 최소 <strong>2글자 이상</strong> 입력해주세요.
        `;
        errorMsg.classList.remove('hidden');
        input.focus();
        input.classList.add('border-red-500', 'focus:ring-red-500');
        input.classList.remove('border-gray-300');
        return;
    }

    // 검증 통과 시 에러 메시지 숨기고 검색 실행
    errorMsg.classList.add('hidden');
    input.classList.remove('border-red-500', 'focus:ring-red-500');
    input.classList.add('border-gray-300');

    window.location.href = `/admin/products?keyword=${encodeURIComponent(keyword)}&sortType=${sortType}`;
}

// 검색 버튼 클릭
document.getElementById('searchBtn')?.addEventListener('click', searchProducts);

// Enter 키 검색
document.getElementById('searchInput')?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        searchProducts();
    }
});

// 입력 중에 에러 메시지 자동 숨김
document.getElementById('searchInput')?.addEventListener('input', function() {
    const errorMsg = document.getElementById('searchError');
    if (!errorMsg.classList.contains('hidden')) {
        if (this.value.trim().length >= 2) {
            errorMsg.classList.add('hidden');
            this.classList.remove('border-red-500', 'focus:ring-red-500');
            this.classList.add('border-gray-300');
        }
    }
});

// ========================================
// 2. 정렬 변경 (검색어 유지)
// ========================================
document.getElementById('sortSelect')?.addEventListener('change', function() {
    const params = getCurrentParams();
    const sortType = this.value;

    let url = `/admin/products?sortType=${sortType}`;
    if (params.keyword) {
        url += `&keyword=${encodeURIComponent(params.keyword)}`;
    }

    window.location.href = url;
});

// ========================================
// 3. 체크박스 기능
// ========================================
document.getElementById('selectAll')?.addEventListener('change', function() {
    const checked = this.checked;
    document.querySelectorAll('.product-checkbox').forEach(cb => {
        cb.checked = checked;
    });
    updateSelectedCount();
});

document.querySelectorAll('.product-checkbox').forEach(checkbox => {
    checkbox.addEventListener('change', updateSelectedCount);
});

function updateSelectedCount() {
    const checkedCount = document.querySelectorAll('.product-checkbox:checked').length;
    const totalCount = document.querySelectorAll('.product-checkbox').length;

    document.getElementById('selectedCount').textContent = checkedCount;

    const selectAll = document.getElementById('selectAll');
    if (selectAll) {
        selectAll.checked = (checkedCount === totalCount && totalCount > 0);
    }
}

function getSelectedIds() {
    return Array.from(document.querySelectorAll('.product-checkbox:checked'))
        .map(cb => parseInt(cb.value));
}

// ========================================
// 4. 단건 삭제
// ========================================
document.querySelectorAll('.delete-btn').forEach(button => {
    button.addEventListener('click', async function() {
        const productId = this.dataset.id;

        if (!confirm('정말 삭제하시겠습니까?')) return;

        try {
            const response = await fetch(`/api/admin/products/${productId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('삭제 실패');
            }

            const result = await response.json();
            alert(result.message || '삭제되었습니다.');
            location.reload();
        } catch (error) {
            console.error('삭제 오류:', error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    });
});

// ========================================
// 5. 일괄 삭제
// ========================================
document.getElementById('bulkDeleteBtn')?.addEventListener('click', async function() {
    const ids = getSelectedIds();

    if (ids.length === 0) {
        alert('삭제할 상품을 선택해주세요.');
        return;
    }

    if (!confirm(`${ids.length}개 상품을 삭제하시겠습니까?`)) return;

    try {
        const response = await fetch('/api/admin/products/bulk', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ productIds: ids })
        });

        if (!response.ok) {
            throw new Error('일괄 삭제 실패');
        }

        const result = await response.json();
        alert(result.message || '삭제되었습니다.');
        location.reload();
    } catch (error) {
        console.error('일괄 삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다.');
    }
});

// ========================================
// 6. 일괄 상태 변경
// ========================================
document.getElementById('bulkStatusBtn')?.addEventListener('click', async function() {
    const ids = getSelectedIds();
    const status = document.getElementById('bulkStatusSelect').value;

    if (ids.length === 0) {
        alert('상품을 선택해주세요.');
        return;
    }

    if (!status) {
        alert('변경할 상태를 선택해주세요.');
        return;
    }

    const statusText = {
        'SELLING': '판매중',
        'SOLD_OUT': '품절',
        'STOP': '판매중지'
    }[status];

    if (!confirm(`${ids.length}개 상품을 '${statusText}' 상태로 변경하시겠습니까?`)) return;

    try {
        const response = await fetch('/api/admin/products/bulk/status', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productIds: ids,
                status: status
            })
        });

        if (!response.ok) {
            throw new Error('상태 변경 실패');
        }

        const result = await response.json();
        alert(result.message || '상태가 변경되었습니다.');
        location.reload();
    } catch (error) {
        console.error('상태 변경 오류:', error);
        alert('상태 변경 중 오류가 발생했습니다.');
    }
});

// ========================================
// 페이지 로드 시 초기화
// ========================================
document.addEventListener('DOMContentLoaded', function() {
    updateSelectedCount();
});
