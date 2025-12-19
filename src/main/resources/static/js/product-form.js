let mainImage = null;
let sliderImages = [];
let descImages = [];
let colorVariantIndex = 0;
let sizeVariantIndexMap = {};
let isEditMode = false;
let productId = null;
let deletedExistingImages = []; // (더 이상 사용 안 하지만 호환성을 위해 유지)
let selectedCategoryId = null;
let selectedCategoryPath = '';

const IMAGE_LIMITS = {
    MAIN: 1,
    SLIDER: 4,
    DESC: 10
};

function initializeProductForm() {
    isEditMode = productData !== null;
    productId = isEditMode ? productData.id : null;

    if (isEditMode) {
        loadExistingProductData();
    } else {
        addColorVariant();
    }

    calculateSalePrice();
    initializeEventListeners();
    initializeSortable();
}

function initializeEventListeners() {
    setupImageUploadListeners();
    setupPriceInputListeners();

    const nameInput = document.getElementById('productName');
    if (nameInput) {
        nameInput.addEventListener('input', updateNameLength);
        updateNameLength();
    }
}

function setupImageUploadListeners() {
    const imageTypes = [
        { btnId: 'addMainImageBtn', inputId: 'mainImageInput', handler: handleMainImageUpload },
        { btnId: 'addSliderImageBtn', inputId: 'sliderImageInput', handler: handleSliderImageUpload },
        { btnId: 'addDescImageBtn', inputId: 'descImageInput', handler: handleDescImageUpload }
    ];

    imageTypes.forEach(({ btnId, inputId, handler }) => {
        document.getElementById(btnId)?.addEventListener('click', () => handler('click'));
        document.getElementById(inputId)?.addEventListener('change', (e) => handler('change', e));
    });
}

function handleMainImageUpload(action, event) {
    if (action === 'click') {
        if (mainImage || document.querySelector('#mainImagePreview .image-preview-item')) {
            alert('대표 이미지는 1장만 등록 가능합니다.');
            return;
        }
        document.getElementById('mainImageInput').click();
    } else if (action === 'change' && event.target.files.length > 0) {
        mainImage = event.target.files[0];
        renderImagePreview('mainImagePreview', mainImage);
    }
}

function handleSliderImageUpload(action, event) {
    if (action === 'click') {
        if (!checkImageLimit('sliderImageList', sliderImages, IMAGE_LIMITS.SLIDER, '슬라이더')) return;
        document.getElementById('sliderImageInput').click();
    } else if (action === 'change') {
        addMultipleImages(event, sliderImages, 'sliderImageList', IMAGE_LIMITS.SLIDER, '슬라이더', renderSliderImages);
    }
}

function handleDescImageUpload(action, event) {
    if (action === 'click') {
        if (!checkImageLimit('descImageList', descImages, IMAGE_LIMITS.DESC, '상세 설명')) return;
        document.getElementById('descImageInput').click();
    } else if (action === 'change') {
        addMultipleImages(event, descImages, 'descImageList', IMAGE_LIMITS.DESC, '상세 설명', renderDescImages);
    }
}

function checkImageLimit(listId, imageArray, limit, typeName) {
    const existingCount = document.querySelectorAll(`#${listId} .image-preview-item`).length;
    const currentCount = existingCount + imageArray.length;

    if (currentCount >= limit) {
        alert(`${typeName} 이미지는 최대 ${limit}장까지 등록 가능합니다.`);
        return false;
    }
    return true;
}

function addMultipleImages(event, imageArray, listId, limit, typeName, renderFunc) {
    const files = Array.from(event.target.files);
    if (files.length === 0) return;

    const existingCount = document.querySelectorAll(`#${listId} .image-preview-item`).length;
    const currentCount = existingCount + imageArray.length;

    if (currentCount >= limit) {
        alert(`${typeName} 이미지는 최대 ${limit}장까지 등록 가능합니다.`);
        event.target.value = '';
        return;
    }

    const allowedCount = limit - currentCount;
    files.slice(0, allowedCount).forEach(file => {
        const isDuplicate = imageArray.some(img => img.name === file.name && img.size === file.size);
        if (!isDuplicate) {
            imageArray.push(file);
        }
    });

    renderFunc();
    event.target.value = '';
}

function initializeSortable() {
    if (typeof Sortable === 'undefined') return;
    new Sortable(document.getElementById('sliderImageList'), {
        animation: 150,
        ghostClass: 'sortable-ghost',
        dragClass: 'sortable-drag',
        onEnd: updateSliderImageOrder
    });
}

// [수정됨] 기존 데이터 로드 시 ID 보존 로직 추가
function loadExistingProductData() {
    if (!productData) return;

    if (existingImages?.length > 0) {
        // displayOrder 기준으로 정렬해서 로드
        const sortedImages = [...existingImages].sort((a, b) => a.displayOrder - b.displayOrder);
        sortedImages.forEach(img => {
            // [중요] img.id를 넘겨줘야 함
            if (img.imageType === 'MAIN') loadExistingMainImage(img.imageUrl, img.id);
            else if (img.imageType === 'SLIDER') loadExistingSliderImage(img.imageUrl, img.id);
            else if (img.imageType === 'DESCRIPTION') loadExistingDescImage(img.imageUrl, img.id);
        });
    }

    productData.optionGroups?.forEach(group => {
        const colorIdx = colorVariantIndex;
        addColorVariant();
        const colorInput = document.getElementById(`colorName${colorIdx}`);
        colorInput.value = group.name || '';
        colorInput.dataset.groupId = group.id || '';

        group.details?.forEach((detail, idx) => {
            if (idx > 0) addSizeVariant(colorIdx);
            const sizeDiv = document.getElementById(`size${colorIdx}_${idx}`);
            sizeDiv.querySelector('.size-name').value = detail.name || '';
            sizeDiv.querySelector('.size-stock').value = detail.stockQuantity || 0;
            sizeDiv.querySelector('.sku-display').value = detail.sku || '';
            sizeDiv.dataset.detailId = detail.id || '';
        });
    });
}

function renderImagePreview(containerId, file) {
    const reader = new FileReader();
    reader.onload = (e) => {
        const container = document.getElementById(containerId);
        container.innerHTML = `
            <div class="relative image-preview-item border border-gray-200 rounded p-2">
                <img src="${e.target.result}" class="w-full h-32 object-cover rounded image-thumbnail">
                <button type="button" onclick="removeMainImage()"
                        class="absolute top-3 right-3 bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600">
                    삭제
                </button>
            </div>
        `;
        setupImageErrorHandling();
    };
    reader.readAsDataURL(file);
}

// [수정됨] ID를 data 속성에 저장
function loadExistingMainImage(url, id) {
    const preview = document.getElementById('mainImagePreview');
    preview.innerHTML = `
        <div class="relative image-preview-item border border-gray-200 rounded p-2" data-image-id="${id}" data-image-type="MAIN">
            <img src="${url}" class="w-full h-32 object-cover rounded image-thumbnail">
            <button type="button" onclick="removeMainImage()" class="absolute top-3 right-3 bg-red-500 text-white px-2 py-1 rounded text-xs">삭제</button>
        </div>
    `;
    setupImageErrorHandling();
}

function loadExistingSliderImage(url, id) {
    const listDiv = document.getElementById('sliderImageList');
    const itemDiv = document.createElement('div');
    itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 cursor-move image-preview-item';
    itemDiv.dataset.existingUrl = url;
    itemDiv.dataset.imageId = id; // [중요] ID 저장
    itemDiv.dataset.imageType = 'SLIDER';
    itemDiv.innerHTML = createSliderItemHTML(url, 0, `removeExistingImage(this)`, '기존 이미지');
    listDiv.appendChild(itemDiv);
    updateSliderImageOrder();
    setupImageErrorHandling();
}

function loadExistingDescImage(url, id) {
    const listDiv = document.getElementById('descImageList');
    const itemDiv = document.createElement('div');
    itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 image-preview-item';
    itemDiv.dataset.existingUrl = url;
    itemDiv.dataset.imageId = id; // [중요] ID 저장
    itemDiv.dataset.imageType = 'DESCRIPTION';
    itemDiv.innerHTML = createDescItemHTML(url, `removeExistingImage(this)`, '기존 이미지');
    listDiv.appendChild(itemDiv);
    setupImageErrorHandling();
}

function createSliderItemHTML(url, order, onclickFunc, label) {
    return `
        <span class="order-badge flex items-center justify-center w-6 h-6 bg-teal-500 text-white text-xs font-bold rounded-full">${order}</span>
        <img src="${url}" class="w-12 h-12 object-cover rounded image-thumbnail">
        <span class="flex-1 text-sm text-gray-700 truncate">${label}</span>
        <button type="button" onclick="${onclickFunc}" class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600">삭제</button>
    `;
}

function createDescItemHTML(url, onclickFunc, label) {
    return `
        <img src="${url}" class="w-12 h-12 object-cover rounded image-thumbnail">
        <span class="flex-1 text-sm text-gray-700 truncate">${label}</span>
        <button type="button" onclick="${onclickFunc}" class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600">삭제</button>
    `;
}

function removeExistingImage(btn) {
    // DOM에서 제거만 하면 collectImages() 실행 시 자동으로 제외됨
    btn.closest('.image-preview-item').remove();
    updateSliderImageOrder();
}

function removeMainImage() {
    mainImage = null;
    document.getElementById('mainImagePreview').innerHTML = '';
    document.getElementById('mainImageInput').value = '';
}

function renderSliderImages() {
    const listDiv = document.getElementById('sliderImageList');
    // 새로 추가된(미리보기용) 이미지만 지우고 다시 그림 (기존 이미지는 유지)
    listDiv.querySelectorAll('.image-preview-item:not([data-image-id])').forEach(item => item.remove());

    sliderImages.forEach((file, idx) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 cursor-move image-preview-item';
            // 신규 이미지는 data-image-id 없음
            itemDiv.innerHTML = createSliderItemHTML(e.target.result, 0, `removeNewSliderImage(${idx})`, file.name);
            listDiv.appendChild(itemDiv);
            updateSliderImageOrder();
            setupImageErrorHandling();
        };
        reader.readAsDataURL(file);
    });
}

function renderDescImages() {
    const listDiv = document.getElementById('descImageList');
    listDiv.querySelectorAll('.image-preview-item:not([data-image-id])').forEach(item => item.remove());

    descImages.forEach((file, idx) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 image-preview-item';
            itemDiv.innerHTML = createDescItemHTML(e.target.result, `removeNewDescImage(${idx})`, file.name);
            listDiv.appendChild(itemDiv);
            setupImageErrorHandling();
        };
        reader.readAsDataURL(file);
    });
}

function removeNewSliderImage(idx) {
    sliderImages.splice(idx, 1);
    renderSliderImages();
}

function removeNewDescImage(idx) {
    descImages.splice(idx, 1);
    renderDescImages();
}

function updateSliderImageOrder() {
    document.querySelectorAll('#sliderImageList .image-preview-item').forEach((item, idx) => {
        const badge = item.querySelector('.order-badge');
        if (badge) badge.textContent = idx + 1;
    });
}

function setupPriceInputListeners() {
    const priceInput = document.getElementById('regularPrice');
    const discountInput = document.getElementById('discountRate');

    [priceInput, discountInput].forEach(input => {
        if (!input) return;
        input.addEventListener('keydown', preventInvalidChars);
        input.addEventListener('input', (e) => {
            if (input === priceInput) limitLength(e, 8);
            else limitLength(e, 3, 100);
            calculateSalePrice();
        });
    });
}

function preventInvalidChars(e) {
    if (['e', 'E', '+', '-', '.'].includes(e.key)) e.preventDefault();
}

function limitLength(e, maxLength, maxValue) {
    const value = e.target.value;
    if (value.length > maxLength) e.target.value = value.slice(0, maxLength);
    if (maxValue && parseInt(value) > maxValue) e.target.value = String(maxValue);
}

function updateNameLength() {
    const input = document.getElementById('productName');
    const counter = document.getElementById('nameLength');
    if (input && counter) counter.textContent = input.value.length;
}

// --- 카테고리 모달 로직 ---
function openCategoryModal() {
    const modal = document.getElementById('categoryModal');
    modal.classList.remove('hidden');
    modal.classList.add('flex', 'modal-backdrop');
    loadCategories();
}

function closeCategoryModal() {
    const modal = document.getElementById('categoryModal');
    modal.classList.add('hidden');
    modal.classList.remove('flex', 'modal-backdrop');
}

function loadCategories() {
    const parentList = document.getElementById('parentCategoryList');
    parentList.innerHTML = '';

    categoryTreeData.forEach(parent => {
        const div = document.createElement('div');
        div.className = 'category-parent-item p-3 border-b border-gray-200 cursor-pointer hover:bg-gray-100';
        div.textContent = parent.name;
        div.onclick = (e) => selectParentCategory(e, parent);
        parentList.appendChild(div);
    });
}

function selectParentCategory(e, parent) {
    document.querySelectorAll('.category-parent-item').forEach(i => i.classList.remove('bg-teal-50', 'border-teal-500'));
    e.currentTarget.classList.add('bg-teal-50', 'border-teal-500');

    selectedCategoryId = parent.id;
    selectedCategoryPath = parent.name;

    const childList = document.getElementById('childCategoryList');
    childList.innerHTML = '';

    if (!parent.children || parent.children.length === 0) {
        childList.innerHTML = '<p class="p-3 text-sm text-gray-500 text-center">소분류가 없습니다.</p>';
        return;
    }

    parent.children.forEach(child => {
        const div = document.createElement('div');
        div.className = 'category-item p-3 hover:bg-teal-100 cursor-pointer border-b border-gray-200';
        div.textContent = child.name;
        div.onclick = (innerE) => {
            document.querySelectorAll('.category-item').forEach(i => i.classList.remove('bg-teal-50', 'border-teal-500'));
            innerE.currentTarget.classList.add('bg-teal-50', 'border-teal-500');
            selectedCategoryId = child.id;
            selectedCategoryPath = `${parent.name} > ${child.name}`;
            innerE.stopPropagation();
        };
        childList.appendChild(div);
    });
}

function confirmCategorySelection() {
    if (!selectedCategoryId) return alert('카테고리를 선택해주세요.');
    document.getElementById('categoryId').value = selectedCategoryId;
    document.getElementById('categoryDisplay').innerHTML = `<span class="text-gray-700">${selectedCategoryPath}</span>`;
    closeCategoryModal();
}

function calculateSalePrice() {
    const regular = parseFloat(document.getElementById('regularPrice').value) || 0;
    const discount = parseFloat(document.getElementById('discountRate').value) || 0;
    const salePrice = Math.floor(regular * (1 - discount / 100));
    document.getElementById('salePriceDisplay').textContent = salePrice.toLocaleString() + '원';
}

function addColorVariant() {
    const idx = colorVariantIndex++;
    const container = document.getElementById('colorVariantList');
    const div = document.createElement('div');
    div.className = 'color-variant-card border border-gray-200 rounded p-4 bg-gray-50 mb-4';
    div.id = `colorVariant${idx}`;
    div.innerHTML = `
        <div class="grid grid-cols-[1fr_64px] gap-2 mb-3">
            <input type="text" id="colorName${idx}" placeholder="색상명 (예: 블랙)" class="px-3 py-2 border border-gray-300 rounded focus:ring-teal-500 focus:border-teal-500" oninput="updateAllSkus(${idx})">
            <button type="button" onclick="removeColorVariant(${idx})" class="w-16 py-2 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">삭제</button>
        </div>
        <div id="sizeList${idx}" class="space-y-2"></div>
        <button type="button" onclick="addSizeVariant(${idx})" class="w-full py-2 border border-dashed border-gray-300 rounded text-sm text-gray-600 hover:border-teal-500 hover:text-teal-600 transition mt-2">+ 사이즈 추가</button>
    `;
    container.appendChild(div);
    addSizeVariant(idx);
}

function removeColorVariant(idx) {
    document.getElementById(`colorVariant${idx}`)?.remove();
}

function addSizeVariant(colorIdx) {
    const sizeIdx = (sizeVariantIndexMap[colorIdx] || 0);
    const sizeList = document.getElementById(`sizeList${colorIdx}`);
    const div = document.createElement('div');
    div.className = 'grid grid-cols-[1fr_64px] gap-2';
    div.id = `size${colorIdx}_${sizeIdx}`;
    div.innerHTML = `
        <div class="flex items-center gap-2">
            <input type="text" placeholder="사이즈 (예: M)" class="size-name w-28 px-2 py-1.5 border border-gray-300 rounded text-sm focus:ring-teal-500 focus:border-teal-500" oninput="updateSKU(${colorIdx}, ${sizeIdx})">
            <input type="text" readonly class="sku-display sku-auto flex-1 px-2 py-1.5 border border-gray-200 bg-gray-100 rounded text-sm text-gray-600" placeholder="SKU">
            <input type="number" placeholder="재고" class="size-stock w-20 px-2 py-1.5 border border-gray-300 rounded text-sm focus:ring-teal-500 focus:border-teal-500">
        </div>
        <button type="button" onclick="this.closest('.grid').remove()" class="w-16 py-1.5 bg-gray-300 text-gray-700 rounded text-xs hover:bg-gray-400 btn-delete">삭제</button>
    `;
    sizeList.appendChild(div);
    sizeVariantIndexMap[colorIdx] = sizeIdx + 1;
}

function updateSKU(colorIdx, sizeIdx) {
    const color = document.getElementById(`colorName${colorIdx}`).value.trim().toUpperCase();
    const sizeDiv = document.getElementById(`size${colorIdx}_${sizeIdx}`);
    const sizeInput = sizeDiv.querySelector('.size-name');
    if (!sizeInput) return; // 삭제된 경우

    const size = sizeInput.value.trim().toUpperCase();
    sizeDiv.querySelector('.sku-display').value = (color && size) ? `${color}-${size}` : '';
}

function updateAllSkus(colorIdx) {
    const sizeList = document.getElementById(`sizeList${colorIdx}`);
    sizeList.querySelectorAll(`[id^="size${colorIdx}_"]`).forEach(div => {
        const sizeIdx = parseInt(div.id.split('_')[1]);
        updateSKU(colorIdx, sizeIdx);
    });
}

// [핵심] 옵션 데이터 수집 (400 에러 해결)
function collectOptionGroups() {
    const groups = [];
    document.querySelectorAll('[id^="colorVariant"]').forEach((colorDiv, groupIdx) => {
        const colorIdx = colorDiv.id.replace('colorVariant', '');
        const colorInput = document.getElementById(`colorName${colorIdx}`);
        if (!colorInput || !colorInput.value.trim()) return;

        const details = [];
        colorDiv.querySelectorAll(`[id^="size${colorIdx}_"]`).forEach((sizeDiv, detailIdx) => {
            const sizeName = sizeDiv.querySelector('.size-name')?.value.trim();
            if (sizeName) {
                details.push({
                    id: sizeDiv.dataset.detailId ? parseInt(sizeDiv.dataset.detailId) : null,
                    name: sizeName,
                    sku: sizeDiv.querySelector('.sku-display').value,
                    stockQuantity: parseInt(sizeDiv.querySelector('.size-stock').value) || 0,
                    addPrice: 0, // [필수] 추가 금액
                    displayOrder: detailIdx + 1 // [필수] 상세 순서
                });
            }
        });

        if (details.length > 0) {
            groups.push({
                id: colorInput.dataset.groupId ? parseInt(colorInput.dataset.groupId) : null,
                name: colorInput.value.trim(),
                displayOrder: groupIdx + 1, // [필수] 그룹 순서
                details: details
            });
        }
    });
    return groups;
}

// [핵심] 이미지 데이터 수집 (이미지 증발 방지 및 순서 동기화)
function collectImages() {
    const images = [];

    // 1. 대표 이미지
    const mainItem = document.querySelector('#mainImagePreview .image-preview-item');
    if (mainItem && mainItem.dataset.imageId) {
        images.push({
            id: parseInt(mainItem.dataset.imageId),
            imageUrl: mainItem.querySelector('img').src,
            imageType: 'MAIN',
            displayOrder: 0
        });
    }

    // 2. 슬라이더 이미지 (화면 순서대로 displayOrder 할당)
    document.querySelectorAll('#sliderImageList .image-preview-item').forEach((item, idx) => {
        if (item.dataset.imageId) { // 기존 이미지만 수집 (신규는 파일로 전송됨)
            images.push({
                id: parseInt(item.dataset.imageId),
                imageUrl: item.dataset.existingUrl,
                imageType: 'SLIDER',
                displayOrder: idx + 1 // 1번부터 시작
            });
        }
    });

    // 3. 상세 이미지
    document.querySelectorAll('#descImageList .image-preview-item').forEach((item, idx) => {
        if (item.dataset.imageId) {
            images.push({
                id: parseInt(item.dataset.imageId),
                imageUrl: item.dataset.existingUrl,
                imageType: 'DESCRIPTION',
                displayOrder: idx
            });
        }
    });
    return images;
}

function submitProduct() {
    const name = document.getElementById('productName').value.trim();
    const categoryId = document.getElementById('categoryId').value;
    const regularPrice = parseFloat(document.getElementById('regularPrice').value);
    const discountRate = parseFloat(document.getElementById('discountRate').value) || 0;
    const description = document.getElementById('productDescription').value.trim();
    const status = document.querySelector('input[name="productStatus"]:checked')?.value || 'SELLING';

    if (!name || !categoryId || !regularPrice) return alert('필수 항목을 입력하세요.');

    // 옵션과 이미지 정보 수집
    const optionGroups = collectOptionGroups();
    const images = collectImages();

    // 400 에러를 방지하기 위해 필드 이름과 타입이 DTO와 정확히 일치해야 함
    const requestData = {
        name,
        price: regularPrice,
        discountRate,
        description,
        categoryId: parseInt(categoryId),
        status,
        optionGroups, // displayOrder, addPrice 포함됨
        images // 동기화할 기존 이미지 리스트
    };

    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(requestData)], { type: 'application/json' }));

    if (mainImage) formData.append('mainImage', mainImage);
    sliderImages.forEach(img => formData.append('sliderImages', img));
    descImages.forEach(img => formData.append('descImages', img));

    submitProductData(formData);
}

function submitProductData(formData) {
    const url = isEditMode ? `/api/admin/products/${productId}` : '/api/admin/products';
    const method = isEditMode ? 'PUT' : 'POST';

    fetch(url, { method, body: formData })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert(isEditMode ? "상품이 수정되었습니다." : "상품이 등록되었습니다.");
                location.href = '/admin/products';
            } else {
                alert("실패: " + data.message);
                // 유효성 검사 에러 상세 표시 (콘솔 확인용)
                if (data.data && data.data.errors) {
                    console.error('검증 에러:', data.data.errors);
                }
            }
        })
        .catch(err => {
            console.error('통신 오류:', err);
            alert("서버 통신 중 오류가 발생했습니다.");
        });
}

function setupImageErrorHandling() {
    document.querySelectorAll('img').forEach(img => {
        if (!img.dataset.errorBound) {
            img.dataset.errorBound = 'true';
            img.addEventListener('error', (e) => {
                e.target.src = '/images/default.jpg';
            });
        }
    });
}