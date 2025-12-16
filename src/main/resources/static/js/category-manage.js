// ==================== 전역 변수 ====================
let categoryTreeData = []; // [변경] 평면 데이터가 아닌 트리 데이터를 그대로 저장
let selectedCategoryId = null;
let selectedParentIdForChild = null;
let expandedParents = new Set();

// 한글 → 영문 변환 매핑
const koreanToEnglish = {
    '상의': 'tops', '하의': 'bottoms', '아우터': 'outer', '기타': 'etc',
    '민소매': 'sleeveless', '반팔': 'short-sleeve', '긴팔': 'long-sleeve',
    '맨투맨': 'mtm', '후드티': 'hoodie', '셔츠': 'shirt', '니트': 'knit',
    '청바지': 'jeans', '면바지': 'cotton-pants', '반바지': 'shorts',
    '조거': 'jogger', '트레이닝': 'training', '자켓': 'jacket', '코트': 'coat',
    '패딩': 'padded', '후드집업': 'hood-zipup', '가디건': 'cardigan',
    '모자': 'hat', '벨트': 'belt', '아이웨어': 'eyewear', '양말': 'socks', '장갑': 'gloves'
};

// ==================== 초기화 ====================
function initializeCategoryManager(initialCategoryTree) {
    console.log('초기 카테고리 트리:', initialCategoryTree);
    // [변경] 평면화(flatten) 과정 삭제. 서버 데이터를 그대로 사용.
    categoryTreeData = initialCategoryTree || [];
    renderCategoryTree();
}

// ==================== Slug 자동 생성 (공통 로직 분리) ====================
function createSlug(name) {
    if (!name) return '';
    if (koreanToEnglish[name]) return koreanToEnglish[name];

    return name
        .replace(/[^\w\s-]/g, '')
        .replace(/\s+/g, '-')
        .toLowerCase()
        .replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, '') || 'category';
}

function generateSlug() {
    const name = document.getElementById('categoryName').value.trim();
    document.getElementById('categorySlug').value = createSlug(name);
}

function generateModalSlug(type) {
    const nameInputId = type === 'parent' ? 'newParentName' : 'newChildName';
    const slugInputId = type === 'parent' ? 'newParentSlug' : 'newChildSlug';
    const name = document.getElementById(nameInputId).value.trim();
    document.getElementById(slugInputId).value = createSlug(name);
}

// ==================== 카테고리 트리 렌더링 (핵심 로직 변경) ====================
function renderCategoryTree() {
    const container = document.getElementById('categoryTree');
    container.innerHTML = '';

    if (categoryTreeData.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="bi bi-inbox text-4xl mb-2"></i>
                <p>등록된 카테고리가 없습니다.</p>
            </div>
        `;
        return;
    }

    // [변경] 평면 리스트를 필터링하는 것이 아니라, 트리 구조를 직접 순회합니다.
    categoryTreeData.forEach(parent => {
        // 1. 상위 카테고리 그리기
        const parentDiv = createParentCategoryItem(parent);
        container.appendChild(parentDiv);

        // 2. 하위 카테고리 그리기 (children 배열 사용)
        if (parent.children && parent.children.length > 0) {
            const childContainer = document.createElement('div');
            childContainer.className = 'ml-6 mt-1 space-y-1';
            childContainer.id = `children-${parent.id}`;
            childContainer.style.display = expandedParents.has(parent.id) ? 'block' : 'none';

            parent.children.forEach(child => {
                const childDiv = createChildCategoryItem(child);
                childContainer.appendChild(childDiv);
            });

            container.appendChild(childContainer);
        }
    });
}

function createParentCategoryItem(category) {
    const div = document.createElement('div');
    // [변경] children 배열 존재 여부로 자식 확인
    const hasChildren = category.children && category.children.length > 0;
    const isExpanded = expandedParents.has(category.id);

    div.className = `category-item p-3 border border-gray-200 rounded transition bg-white font-medium ${
        selectedCategoryId === category.id ? 'ring-2 ring-teal-500 bg-teal-50 category-selected' : 'hover:bg-teal-50'
    }`;

    const statusBadge = category.isVisible
        ? '<span class="text-xs bg-green-100 text-green-700 px-2 py-1 rounded">노출</span>'
        : '<span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">숨김</span>';

    const childCount = category.children ? category.children.length : 0;
    const childBadge = childCount > 0
        ? `<span class="text-xs bg-purple-100 text-purple-700 px-2 py-1 rounded ml-1">${childCount}개</span>`
        : '';

    const toggleIcon = hasChildren
        ? `<i class="bi bi-chevron-${isExpanded ? 'down' : 'right'} toggle-icon text-gray-600 mr-2" style="cursor: pointer;" onclick="event.stopPropagation(); toggleChildren(${category.id});"></i>`
        : '<span class="w-5 inline-block mr-2"></span>';

    // JSON.stringify 사용 시 따옴표 충돌 방지 처리
    const categoryJson = JSON.stringify(category).replace(/'/g, "&#39;");

    div.innerHTML = `
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-2 flex-1 cursor-pointer" onclick='selectCategory(${categoryJson})'>
                ${toggleIcon}
                <i class="bi bi-folder-fill text-teal-600"></i>
                <span>${category.name}</span>
                <span class="text-xs text-gray-500">(${category.slug})</span>
                ${childBadge}
            </div>
            <div class="flex items-center gap-2">
                ${statusBadge}
                <span class="text-xs text-gray-400">#${category.displayOrder}</span>
            </div>
        </div>
    `;

    return div;
}

function createChildCategoryItem(category) {
    const div = document.createElement('div');
    div.className = `category-item p-3 border border-gray-200 rounded cursor-pointer transition bg-gray-50 ${
        selectedCategoryId === category.id ? 'ring-2 ring-teal-500 bg-teal-50 category-selected' : 'hover:bg-teal-50'
    }`;

    const categoryJson = JSON.stringify(category).replace(/'/g, "&#39;");
    div.onclick = () => selectCategory(category);

    const statusBadge = category.isVisible
        ? '<span class="text-xs bg-green-100 text-green-700 px-2 py-1 rounded">노출</span>'
        : '<span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">숨김</span>';

    div.innerHTML = `
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
                <i class="bi bi-folder text-purple-600"></i>
                <span>${category.name}</span>
                <span class="text-xs text-gray-500">(${category.slug})</span>
            </div>
            <div class="flex items-center gap-2">
                ${statusBadge}
                <span class="text-xs text-gray-400">#${category.displayOrder}</span>
            </div>
        </div>
    `;

    return div;
}

function toggleChildren(parentId) {
    const childContainer = document.getElementById(`children-${parentId}`);
    if (childContainer) {
        if (expandedParents.has(parentId)) {
            expandedParents.delete(parentId);
        } else {
            expandedParents.add(parentId);
        }
        renderCategoryTree();
    }
}

// ==================== 카테고리 선택 ====================
function selectCategory(category) {
    selectedCategoryId = category.id;

    document.getElementById('emptyState').classList.add('hidden');
    document.getElementById('categoryDetailForm').classList.remove('hidden');

    document.getElementById('categoryId').value = category.id;
    document.getElementById('categoryName').value = category.name;
    document.getElementById('categorySlug').value = category.slug;
    document.getElementById('displayOrder').value = category.displayOrder;
    // select value는 문자열
    document.getElementById('isVisible').value = category.isVisible.toString();

    // [변경] 부모/자식 판별 로직
    // categoryTreeData(최상위 목록)에 현재 ID가 있는지 확인
    const isRoot = categoryTreeData.some(p => p.id === category.id);

    document.getElementById('categoryLevel').textContent = isRoot ? '최상위 카테고리 (대분류)' : '하위 카테고리 (소분류)';

    if (!isRoot) {
        // [변경] 자식 카테고리라면, 트리 전체를 뒤져서 부모를 찾아야 함
        const parent = categoryTreeData.find(p => p.children && p.children.some(c => c.id === category.id));

        if (parent) {
            document.getElementById('categoryParentId').value = parent.id;
            document.getElementById('parentCategoryDisplay').textContent = parent.name;
            document.getElementById('parentCategorySection').classList.remove('hidden');
        }
    } else {
        document.getElementById('parentCategorySection').classList.add('hidden');
        document.getElementById('categoryParentId').value = '';
    }

    document.getElementById('addChildBtn').classList.toggle('hidden', !isRoot);
    renderCategoryTree();
}

function clearSelection() {
    selectedCategoryId = null;
    document.getElementById('emptyState').classList.remove('hidden');
    document.getElementById('categoryDetailForm').classList.add('hidden');
    renderCategoryTree();
}

// ==================== CRUD (페이지 새로고침) ====================
function saveCategory() {
    const id = document.getElementById('categoryId').value;
    const name = document.getElementById('categoryName').value.trim();
    const slug = document.getElementById('categorySlug').value.trim();
    const displayOrder = parseInt(document.getElementById('displayOrder').value) || 1;
    const isVisible = document.getElementById('isVisible').value === 'true';
    const parentId = document.getElementById('categoryParentId').value;

    if (!name || !slug) {
        alert('카테고리명과 Slug를 입력하세요.');
        return;
    }

    fetch(`/api/admin/categories/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            name,
            slug,
            displayOrder,
            isVisible,
            parentId: parentId ? parseInt(parentId) : null
        })
    })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                alert('카테고리가 수정되었습니다.');
                window.location.reload();
            } else {
                alert('수정 실패: ' + result.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert('서버 오류가 발생했습니다.');
        });
}

function deleteCategory() {
    const id = document.getElementById('categoryId').value;

    // [변경] 삭제 전 자식 확인 로직 (트리 데이터 사용)
    const parentNode = categoryTreeData.find(p => p.id == id);
    if (parentNode && parentNode.children && parentNode.children.length > 0) {
        alert(`하위 카테고리가 ${parentNode.children.length}개 있습니다.\n먼저 하위 카테고리를 삭제해주세요.`);
        return;
    }

    const name = document.getElementById('categoryName').value;
    if (!confirm(`"${name}" 카테고리를 삭제하시겠습니까?`)) return;

    fetch(`/api/admin/categories/${id}`, { method: 'DELETE' })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                alert('카테고리가 삭제되었습니다.');
                window.location.reload();
            } else {
                alert('삭제 실패: ' + result.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert('서버 오류가 발생했습니다.');
        });
}

// ==================== 최상위 카테고리 추가 모달 ====================
function openAddParentModal() {
    document.getElementById('newParentName').value = '';
    document.getElementById('newParentSlug').value = '';

    // [변경] 트리 데이터에서 max 순서 찾기
    const maxOrder = categoryTreeData.length > 0
        ? Math.max(...categoryTreeData.map(c => c.displayOrder))
        : 0;
    document.getElementById('newParentOrder').value = maxOrder + 1;

    const modal = document.getElementById('addParentModal');
    modal.classList.remove('hidden');
    modal.classList.add('flex', 'modal-enter');
}

function closeAddParentModal() {
    const modal = document.getElementById('addParentModal');
    modal.classList.add('hidden');
    modal.classList.remove('flex', 'modal-enter');
}

function createParentCategory() {
    const name = document.getElementById('newParentName').value.trim();
    const slug = document.getElementById('newParentSlug').value.trim();
    const displayOrder = parseInt(document.getElementById('newParentOrder').value) || 1;

    if (!name || !slug) {
        alert('카테고리명과 Slug를 입력하세요.');
        return;
    }

    fetch('/api/admin/categories', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, slug, displayOrder, isVisible: true, parentId: null })
    })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                alert('최상위 카테고리가 추가되었습니다.');
                window.location.reload();
            } else {
                alert('추가 실패: ' + result.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert('서버 오류가 발생했습니다.');
        });
}

// ==================== 하위 카테고리 추가 모달 ====================
function openAddChildModal() {
    const parentId = document.getElementById('categoryId').value;
    const parentName = document.getElementById('categoryName').value;

    if (!parentId) {
        alert('상위 카테고리를 선택하세요.');
        return;
    }

    selectedParentIdForChild = parentId;

    // [변경] 해당 부모 노드를 찾아 자식들의 max 순서 계산
    const parentNode = categoryTreeData.find(p => p.id == parentId);
    let nextOrder = 1;
    if (parentNode && parentNode.children && parentNode.children.length > 0) {
        nextOrder = Math.max(...parentNode.children.map(c => c.displayOrder)) + 1;
    }

    document.getElementById('selectedParentName').textContent = parentName;
    document.getElementById('newChildName').value = '';
    document.getElementById('newChildSlug').value = '';
    document.getElementById('newChildOrder').value = nextOrder;

    const modal = document.getElementById('addChildModal');
    modal.classList.remove('hidden');
    modal.classList.add('flex', 'modal-enter');
}

function closeAddChildModal() {
    const modal = document.getElementById('addChildModal');
    modal.classList.add('hidden');
    modal.classList.remove('flex', 'modal-enter');
}

function createChildCategory() {
    const parentId = selectedParentIdForChild;
    const name = document.getElementById('newChildName').value.trim();
    const slug = document.getElementById('newChildSlug').value.trim();
    const displayOrder = parseInt(document.getElementById('newChildOrder').value) || 1;

    if (!name || !slug) {
        alert('카테고리명과 Slug를 입력하세요.');
        return;
    }

    fetch('/api/admin/categories', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, slug, displayOrder, isVisible: true, parentId: parseInt(parentId) })
    })
        .then(res => res.json())
        .then(result => {
            if (result.success) {
                alert('하위 카테고리가 추가되었습니다.');
                window.location.reload();
            } else {
                alert('추가 실패: ' + result.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert('서버 오류가 발생했습니다.');
        });
}