// ==================== 전역 변수 ====================
let categoryData = [];
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
    categoryData = flattenCategoryTree(initialCategoryTree);
    console.log('평면화된 카테고리 데이터:', categoryData);
    renderCategoryTree();
}

// ==================== 트리 → 평면 리스트 변환 ====================
function flattenCategoryTree(tree) {
    const result = [];
    tree.forEach(parent => {
        result.push({
            id: parent.id,
            name: parent.name,
            slug: parent.slug,
            parentId: null,
            depth: parent.depth,
            displayOrder: parent.displayOrder,
            isVisible: parent.isVisible
        });

        if (parent.children && parent.children.length > 0) {
            parent.children.forEach(child => {
                result.push({
                    id: child.id,
                    name: child.name,
                    slug: child.slug,
                    parentId: parent.id,
                    depth: child.depth,
                    displayOrder: child.displayOrder,
                    isVisible: child.isVisible
                });
            });
        }
    });
    return result;
}

// ==================== Slug 자동 생성 ====================
function generateSlug() {
    const name = document.getElementById('categoryName').value.trim();
    const slugInput = document.getElementById('categorySlug');

    if (!name) {
        slugInput.value = '';
        return;
    }

    if (koreanToEnglish[name]) {
        slugInput.value = koreanToEnglish[name];
    } else {
        let slug = name
            .replace(/[^\w\s-]/g, '')
            .replace(/\s+/g, '-')
            .toLowerCase()
            .replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, '');
        slugInput.value = slug || 'category';
    }
}

function generateModalSlug(type) {
    const nameInputId = type === 'parent' ? 'newParentName' : 'newChildName';
    const slugInputId = type === 'parent' ? 'newParentSlug' : 'newChildSlug';

    const name = document.getElementById(nameInputId).value.trim();
    const slugInput = document.getElementById(slugInputId);

    if (!name) {
        slugInput.value = '';
        return;
    }

    if (koreanToEnglish[name]) {
        slugInput.value = koreanToEnglish[name];
    } else {
        let slug = name
            .replace(/[^\w\s-]/g, '')
            .replace(/\s+/g, '-')
            .toLowerCase()
            .replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, '');
        slugInput.value = slug || 'category';
    }
}

// ==================== 카테고리 트리 렌더링 ====================
function renderCategoryTree() {
    const container = document.getElementById('categoryTree');
    container.innerHTML = '';

    if (categoryData.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="bi bi-inbox text-4xl mb-2"></i>
                <p>등록된 카테고리가 없습니다.</p>
            </div>
        `;
        return;
    }

    const parents = categoryData.filter(c => c.depth === 0).sort((a, b) => a.displayOrder - b.displayOrder);
    const children = categoryData.filter(c => c.depth === 1);

    parents.forEach(parent => {
        const parentDiv = createParentCategoryItem(parent);
        container.appendChild(parentDiv);

        const parentChildren = children.filter(c => c.parentId === parent.id)
            .sort((a, b) => a.displayOrder - b.displayOrder);

        if (parentChildren.length > 0) {
            const childContainer = document.createElement('div');
            childContainer.className = 'ml-6 mt-1 space-y-1';
            childContainer.id = `children-${parent.id}`;
            childContainer.style.display = expandedParents.has(parent.id) ? 'block' : 'none';

            parentChildren.forEach(child => {
                const childDiv = createChildCategoryItem(child);
                childContainer.appendChild(childDiv);
            });

            container.appendChild(childContainer);
        }
    });
}

function createParentCategoryItem(category) {
    const div = document.createElement('div');
    const hasChildren = categoryData.filter(c => c.parentId === category.id).length > 0;
    const isExpanded = expandedParents.has(category.id);

    div.className = `category-item p-3 border border-gray-200 rounded transition bg-white font-medium ${
        selectedCategoryId === category.id ? 'ring-2 ring-teal-500 bg-teal-50 category-selected' : 'hover:bg-teal-50'
    }`;

    const statusBadge = category.isVisible
        ? '<span class="text-xs bg-green-100 text-green-700 px-2 py-1 rounded">노출</span>'
        : '<span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">숨김</span>';

    const childCount = categoryData.filter(c => c.parentId === category.id).length;
    const childBadge = childCount > 0
        ? `<span class="text-xs bg-purple-100 text-purple-700 px-2 py-1 rounded ml-1">${childCount}개</span>`
        : '';

    const toggleIcon = hasChildren
        ? `<i class="bi bi-chevron-${isExpanded ? 'down' : 'right'} toggle-icon text-gray-600 mr-2" style="cursor: pointer;" onclick="event.stopPropagation(); toggleChildren(${category.id});"></i>`
        : '<span class="w-5 inline-block mr-2"></span>';

    div.innerHTML = `
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-2 flex-1 cursor-pointer" onclick="selectCategory(${JSON.stringify(category).replace(/"/g, '&quot;')})">
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
    document.getElementById('isVisible').value = category.isVisible;

    const isParent = category.depth === 0;
    document.getElementById('categoryLevel').textContent = isParent ? '최상위 카테고리 (대분류)' : '하위 카테고리 (소분류)';

    if (!isParent && category.parentId) {
        document.getElementById('categoryParentId').value = category.parentId;
        document.getElementById('parentCategorySection').classList.remove('hidden');
        const parent = categoryData.find(p => p.id === category.parentId);
        document.getElementById('parentCategoryDisplay').textContent = parent ? parent.name : '-';
    } else {
        document.getElementById('parentCategorySection').classList.add('hidden');
        document.getElementById('categoryParentId').value = '';
    }

    document.getElementById('addChildBtn').classList.toggle('hidden', !isParent);
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
    const name = document.getElementById('categoryName').value;
    const childCount = categoryData.filter(c => c.parentId == id).length;

    if (childCount > 0) {
        alert(`하위 카테고리가 ${childCount}개 있습니다.\n먼저 하위 카테고리를 삭제해주세요.`);
        return;
    }

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
    const maxOrder = Math.max(...categoryData.filter(c => c.depth === 0).map(c => c.displayOrder), 0);
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
    const childCount = categoryData.filter(c => c.parentId == parentId).length;

    document.getElementById('selectedParentName').textContent = parentName;
    document.getElementById('newChildName').value = '';
    document.getElementById('newChildSlug').value = '';
    document.getElementById('newChildOrder').value = childCount + 1;

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
