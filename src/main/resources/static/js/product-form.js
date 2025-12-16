let mainImage = null;
let sliderImages = [];
let descImages = [];
let colorVariantIndex = 0;
let sizeVariantIndexMap = {};
let isEditMode = false;
let productId = null;
let deletedExistingImages = [];
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

    console.log('카테고리 데이터:', categoryTreeData);
    console.log('수정 모드:', isEditMode);

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
        const file = event.target.files[0];
        mainImage = file;
        renderImagePreview('mainImagePreview', file, true);
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
    const existingCount = document.querySelectorAll(`#${listId} [data-existing-url]`).length;
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

    const existingCount = document.querySelectorAll(`#${listId} [data-existing-url]`).length;
    const currentCount = existingCount + imageArray.length;

    if (currentCount >= limit) {
        alert(`${typeName} 이미지는 최대 ${limit}장까지 등록 가능합니다.`);
        event.target.value = '';
        return;
    }

    const allowedCount = limit - currentCount;
    let addedCount = 0;

    files.slice(0, allowedCount).forEach(file => {
        const isDuplicate = imageArray.some(img => img.name === file.name && img.size === file.size);
        if (!isDuplicate) {
            imageArray.push(file);
            addedCount++;
        }
    });

    if (addedCount > 0) renderFunc();
    if (files.length > addedCount) {
        alert(`${addedCount}개 추가되었습니다. (${files.length - addedCount}개는 제한 또는 중복으로 제외)`);
    }

    event.target.value = '';
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

function initializeSortable() {
    if (typeof Sortable === 'undefined') {
        console.warn('Sortable.js가 로드되지 않았습니다.');
        return;
    }
    new Sortable(document.getElementById('sliderImageList'), {
        animation: 150,
        ghostClass: 'sortable-ghost',
        dragClass: 'sortable-drag',
        onEnd: updateSliderImageOrder
    });
}

function loadExistingProductData() {
    if (!productData) return;

    if (existingImages?.length > 0) {
        const imageLoaders = {
            'MAIN': loadExistingMainImage,
            'SLIDER': loadExistingSliderImage,
            'DESCRIPTION': loadExistingDescImage
        };
        existingImages.forEach(img => imageLoaders[img.imageType]?.(img.imageUrl));
    }

    productData.optionGroups?.forEach(group => {
        const colorIdx = colorVariantIndex;
        addColorVariant();

        const colorInput = document.getElementById(`colorName${colorIdx}`);
        if (colorInput) {
            colorInput.value = group.name || '';
            colorInput.dataset.groupId = group.id || '';
        }

        group.details?.forEach((detail, idx) => {
            if (idx > 0) addSizeVariant(colorIdx);

            const sizeDiv = document.getElementById(`size${colorIdx}_${idx}`);
            if (sizeDiv) {
                sizeDiv.querySelector('.size-name').value = detail.name || '';
                sizeDiv.querySelector('.size-stock').value = detail.stockQuantity || 0;
                sizeDiv.querySelector('.sku-display').value = detail.sku || '';
                sizeDiv.dataset.detailId = detail.id || '';
            }
        });
    });
}

function renderImagePreview(containerId, file, isSingle = false) {
    const reader = new FileReader();
    reader.onload = (e) => {
        const container = document.getElementById(containerId);
        const html = `
            <div class="relative image-preview-item border border-gray-200 rounded p-2">
                <img src="${e.target.result}" class="w-full h-32 object-cover rounded image-thumbnail">
                <button type="button" onclick="removeMainImage()"
                        class="absolute top-3 right-3 bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600 btn-delete">
                    삭제
                </button>
            </div>
        `;
        container.innerHTML = html;
        setupImageErrorHandling();
    };
    reader.readAsDataURL(file);
}

function loadExistingMainImage(url) {
    const preview = document.getElementById('mainImagePreview');
    preview.innerHTML = createImageHTML(url, 'removeMainImage()', true);
    setupImageErrorHandling();
}

function loadExistingSliderImage(url) {
    const listDiv = document.getElementById('sliderImageList');
    const count = listDiv.querySelectorAll('.image-preview-item').length;
    const itemDiv = document.createElement('div');
    itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 cursor-move image-preview-item';
    itemDiv.dataset.existingUrl = url;
    itemDiv.innerHTML = createSliderItemHTML(url, count + 1, `removeExistingSliderImage('${url}')`, '기존 이미지');
    listDiv.appendChild(itemDiv);
    setupImageErrorHandling();
}

function loadExistingDescImage(url) {
    const listDiv = document.getElementById('descImageList');
    const itemDiv = document.createElement('div');
    itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 image-preview-item';
    itemDiv.dataset.existingUrl = url;
    itemDiv.innerHTML = createDescItemHTML(url, `removeExistingDescImage('${url}')`, '기존 이미지');
    listDiv.appendChild(itemDiv);
    setupImageErrorHandling();
}

function createImageHTML(url, onclickFunc, isAbsolute = false) {
    const btnClass = isAbsolute ? 'absolute top-3 right-3' : '';
    return `
        <div class="relative image-preview-item border border-gray-200 rounded p-2">
            <img src="${url}" class="w-full h-32 object-cover rounded image-thumbnail">
            <button type="button" onclick="${onclickFunc}"
                    class="${btnClass} bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600 btn-delete">
                삭제
            </button>
        </div>
    `;
}

function createSliderItemHTML(url, order, onclickFunc, label) {
    return `
        <span class="order-badge flex items-center justify-center w-6 h-6 bg-teal-500 text-white text-xs font-bold rounded-full">${order}</span>
        <img src="${url}" class="w-12 h-12 object-cover rounded image-thumbnail">
        <span class="flex-1 text-sm text-gray-700 truncate">${label}</span>
        <button type="button" onclick="${onclickFunc}"
                class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
            삭제
        </button>
    `;
}

function createDescItemHTML(url, onclickFunc, label) {
    return `
        <img src="${url}" class="w-12 h-12 object-cover rounded image-thumbnail">
        <span class="flex-1 text-sm text-gray-700 truncate">${label}</span>
        <button type="button" onclick="${onclickFunc}"
                class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
            삭제
        </button>
    `;
}

function removeExistingSliderImage(url) {
    deletedExistingImages.push(url);
    document.querySelector(`#sliderImageList [data-existing-url="${url}"]`)?.remove();
    updateSliderImageOrder();
}

function removeExistingDescImage(url) {
    deletedExistingImages.push(url);
    document.querySelector(`#descImageList [data-existing-url="${url}"]`)?.remove();
}

function removeMainImage() {
    mainImage = null;
    document.getElementById('mainImagePreview').innerHTML = '';
    document.getElementById('mainImageInput').value = '';
}

function removeSliderImage(idx) {
    sliderImages.splice(idx, 1);
    renderSliderImages();
}

function removeDescImage(idx) {
    descImages.splice(idx, 1);
    renderDescImages();
}

function renderSliderImages() {
    const listDiv = document.getElementById('sliderImageList');
    const existingCount = listDiv.querySelectorAll('[data-existing-url]').length;

    listDiv.querySelectorAll('[data-index]').forEach(item => item.remove());

    sliderImages.forEach((file, idx) => {
        const render = (url) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 cursor-move image-preview-item';
            itemDiv.dataset.index = idx;
            itemDiv.innerHTML = createSliderItemHTML(url, existingCount + idx + 1, `removeSliderImage(${idx})`, file.name);
            listDiv.appendChild(itemDiv);
            updateSliderImageOrder();
            setupImageErrorHandling();
        };

        if (file._previewUrl) render(file._previewUrl);
        else readFileAsDataURL(file, render);
    });
}

function renderDescImages() {
    const listDiv = document.getElementById('descImageList');
    listDiv.querySelectorAll('[data-index]').forEach(item => item.remove());

    descImages.forEach((file, idx) => {
        const render = (url) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 image-preview-item';
            itemDiv.dataset.index = idx;
            itemDiv.innerHTML = createDescItemHTML(url, `removeDescImage(${idx})`, file.name);
            listDiv.appendChild(itemDiv);
            setupImageErrorHandling();
        };

        if (file._previewUrl) render(file._previewUrl);
        else readFileAsDataURL(file, render);
    });
}

function readFileAsDataURL(file, callback) {
    const reader = new FileReader();
    reader.onload = (e) => {
        file._previewUrl = e.target.result;
        callback(e.target.result);
    };
    reader.readAsDataURL(file);
}

function updateSliderImageOrder() {
    document.querySelectorAll('#sliderImageList .image-preview-item').forEach((item, idx) => {
        const badge = item.querySelector('.order-badge');
        if (badge) badge.textContent = idx + 1;
    });
}

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

    // 선택 상태 초기화
    selectedCategoryId = null;
    selectedCategoryPath = '';

    // 하이라이트 제거
    document.querySelectorAll('.category-parent-item, .category-item').forEach(item => {
        item.classList.remove('bg-teal-50', 'border-teal-500');
    });
}

function loadCategories() {
    const parentList = document.getElementById('parentCategoryList');
    parentList.innerHTML = '';

    categoryTreeData.forEach(parent => {
        const div = document.createElement('div');
        div.className = 'category-parent-item p-3 border-b border-gray-200 cursor-pointer hover:bg-gray-100';
        div.innerHTML = `
            <div class="flex items-center justify-between">
                <span class="flex-1" onclick="event.stopPropagation(); selectParentCategory(${parent.id}, ${JSON.stringify(parent.children).replace(/"/g, '&quot;')}, '${parent.name}')">${parent.name}</span>
            </div>
        `;
        parentList.appendChild(div);
    });
}

function selectParentCategory(parentId, children, parentName) {
    // 대분류 선택 시 하이라이트
    document.querySelectorAll('.category-parent-item').forEach(item => {
        item.classList.remove('bg-teal-50', 'border-teal-500');
    });
    event.currentTarget.closest('.category-parent-item').classList.add('bg-teal-50', 'border-teal-500');

    // 임시로 저장 (대분류만 선택한 경우)
    selectedCategoryId = parentId;
    selectedCategoryPath = parentName;

    // 소분류 로드
    loadChildCategories(children, parentName);
}

function loadChildCategories(children, parentName) {
    const childList = document.getElementById('childCategoryList');
    childList.innerHTML = '';

    if (!children || children.length === 0) {
        childList.innerHTML = '<p class="p-3 text-sm text-gray-500 text-center">소분류가 없습니다.</p>';
        return;
    }

    children.forEach(child => {
        const div = document.createElement('div');
        div.className = 'category-item p-3 hover:bg-teal-100 cursor-pointer border-b border-gray-200';
        div.textContent = child.name;
        div.onclick = () => selectChildCategory(child.id, `${parentName} > ${child.name}`);
        childList.appendChild(div);
    });
}

function selectChildCategory(childId, fullPath) {
    // 소분류 선택 시 하이라이트
    document.querySelectorAll('.category-item').forEach(item => {
        item.classList.remove('bg-teal-50', 'border-teal-500');
    });
    event.currentTarget.classList.add('bg-teal-50', 'border-teal-500');

    // 임시로 저장 (소분류까지 선택한 경우)
    selectedCategoryId = childId;
    selectedCategoryPath = fullPath;
}

function confirmCategorySelection() {
    if (!selectedCategoryId) {
        alert('카테고리를 선택해주세요.');
        return;
    }

    // 최종 확정
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
    div.className = 'color-variant-card border border-gray-200 rounded p-4 bg-gray-50';
    div.id = `colorVariant${idx}`;
    div.innerHTML = `
        <div class="grid grid-cols-[1fr_64px] gap-2 mb-3">
            <input type="text" id="colorName${idx}" placeholder="색상명 (예: 블랙)"
                   class="px-3 py-2 border border-gray-300 rounded focus:ring-teal-500 focus:border-teal-500"
                   oninput="updateAllSkus(${idx})">
            <button type="button" onclick="removeColorVariant(${idx})"
                    class="w-16 py-2 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete whitespace-nowrap">
                삭제
            </button>
        </div>
        <div id="sizeList${idx}" class="space-y-2"></div>
        <button type="button" onclick="addSizeVariant(${idx})"
                class="w-full py-2 border border-dashed border-gray-300 rounded text-sm text-gray-600 hover:border-teal-500 hover:text-teal-600 transition mt-2">
            + 사이즈 추가
        </button>
    `;
    container.appendChild(div);

    sizeVariantIndexMap[idx] = 0;
    addSizeVariant(idx);
}

function removeColorVariant(idx) {
    document.getElementById(`colorVariant${idx}`)?.remove();
}

function addSizeVariant(colorIdx) {
    if (!sizeVariantIndexMap[colorIdx]) sizeVariantIndexMap[colorIdx] = 0;

    const sizeIdx = sizeVariantIndexMap[colorIdx]++;
    const sizeList = document.getElementById(`sizeList${colorIdx}`);
    const div = document.createElement('div');
    div.className = 'grid grid-cols-[1fr_64px] gap-2';
    div.id = `size${colorIdx}_${sizeIdx}`;
    div.innerHTML = `
        <div class="flex items-center gap-2">
            <input type="text" placeholder="사이즈 (예: M)"
                   class="size-name w-28 px-2 py-1.5 border border-gray-300 rounded text-sm focus:ring-teal-500 focus:border-teal-500"
                   oninput="updateSKU(${colorIdx}, ${sizeIdx})">
            <input type="text" readonly
                   class="sku-display sku-auto flex-1 px-2 py-1.5 border border-gray-200 bg-gray-100 rounded text-sm text-gray-600"
                   placeholder="상품 코드(자동생성)">
            <input type="number" placeholder="재고" min="0"
                   class="size-stock w-20 px-2 py-1.5 border border-gray-300 rounded text-sm focus:ring-teal-500 focus:border-teal-500">
        </div>
        <button type="button" onclick="removeSizeVariant(${colorIdx}, ${sizeIdx})"
                class="w-16 py-1.5 bg-gray-300 text-gray-700 rounded text-xs hover:bg-gray-400 btn-delete whitespace-nowrap">
            삭제
        </button>
    `;
    sizeList.appendChild(div);
}

function removeSizeVariant(colorIdx, sizeIdx) {
    document.getElementById(`size${colorIdx}_${sizeIdx}`)?.remove();
}

function updateSKU(colorIdx, sizeIdx) {
    const colorName = document.getElementById(`colorName${colorIdx}`).value.trim().toUpperCase();
    const sizeDiv = document.getElementById(`size${colorIdx}_${sizeIdx}`);
    const sizeName = sizeDiv.querySelector('.size-name').value.trim().toUpperCase();
    const skuDisplay = sizeDiv.querySelector('.sku-display');

    skuDisplay.value = (colorName && sizeName) ? `${colorName}-${sizeName}` : '';
}

function updateAllSkus(colorIdx) {
    const sizeList = document.getElementById(`sizeList${colorIdx}`);
    sizeList.querySelectorAll(`[id^="size${colorIdx}_"]`).forEach(div => {
        const sizeIdx = parseInt(div.id.split('_')[1]);
        updateSKU(colorIdx, sizeIdx);
    });
}

function submitProduct() {
    const name = document.getElementById('productName').value.trim();
    const categoryId = document.getElementById('categoryId').value;
    const regularPrice = parseFloat(document.getElementById('regularPrice').value);
    const discountRate = parseFloat(document.getElementById('discountRate').value) || 0;
    const description = document.getElementById('productDescription').value.trim();
    const status = document.querySelector('input[name="productStatus"]:checked')?.value || 'SELLING';

    if (!validateProductData(name, categoryId, regularPrice)) return;

    const optionGroups = collectOptionGroups();
    if (optionGroups.length === 0) {
        alert('최소 1개 이상의 색상/사이즈 옵션을 추가하세요.');
        return;
    }

    const formData = createFormData({ name, price: regularPrice, discountRate, description, categoryId: parseInt(categoryId), status, optionGroups });
    submitProductData(formData);
}

function validateProductData(name, categoryId, price) {
    if (!name) return focusOnField('productName', '상품명을 입력하세요.'), false;
    if (!categoryId) return alert('카테고리를 선택하세요.'), false;
    if (!price || price <= 0) return focusOnField('regularPrice', '가격을 입력하세요.'), false;
    if (!isEditMode && !mainImage && !document.querySelector('#mainImagePreview .image-preview-item')) {
        return alert('대표 이미지를 업로드하세요.'), false;
    }
    return true;
}

function collectOptionGroups() {
    const groups = [];
    document.querySelectorAll('[id^="colorVariant"]').forEach(colorDiv => {
        const colorIdx = colorDiv.id.replace('colorVariant', '');
        const colorInput = document.getElementById(`colorName${colorIdx}`);
        if (!colorInput) return;

        const colorName = colorInput.value.trim();
        if (!colorName) return;

        const details = [];
        colorDiv.querySelectorAll(`[id^="size${colorIdx}_"]`).forEach(sizeDiv => {
            const sizeName = sizeDiv.querySelector('.size-name')?.value.trim();
            const sku = sizeDiv.querySelector('.sku-display')?.value.trim();
            const stock = parseInt(sizeDiv.querySelector('.size-stock')?.value) || 0;

            if (sizeName && sku) {
                details.push({
                    id: sizeDiv.dataset.detailId ? parseInt(sizeDiv.dataset.detailId) : null,
                    name: sizeName,
                    sku,
                    addPrice: 0,
                    stockQuantity: stock,
                    displayOrder: details.length + 1
                });
            }
        });

        if (details.length > 0) {
            groups.push({
                id: colorInput.dataset.groupId ? parseInt(colorInput.dataset.groupId) : null,
                name: colorName,
                displayOrder: groups.length + 1,
                details
            });
        }
    });
    return groups;
}

function createFormData(requestData) {
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(requestData)], { type: 'application/json' }));

    if (mainImage) formData.append('mainImage', mainImage);
    sliderImages.forEach(img => formData.append('sliderImages', img));
    descImages.forEach(img => formData.append('descImages', img));

    if (isEditMode && deletedExistingImages.length > 0) {
        formData.append('deletedImageUrls', JSON.stringify(deletedExistingImages));
    }

    return formData;
}

function submitProductData(formData) {
    const url = isEditMode ? `/api/admin/products/${productId}` : '/api/admin/products';
    const method = isEditMode ? 'PUT' : 'POST';

    fetch(url, { method, body: formData })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert(isEditMode ? "상품이 수정되었습니다." : "상품이 성공적으로 등록되었습니다.");
                if (!isEditMode) location.href = '/admin/products';
            } else {
                alert((isEditMode ? "수정 실패: " : "등록 실패: ") + data.message);
            }
        })
        .catch(err => {
            console.error('서버 오류:', err);
            alert("서버 오류가 발생했습니다.");
        });
}

function focusOnField(fieldId, errorMessage) {
    alert(errorMessage);
    const field = document.getElementById(fieldId);
    if (field) {
        field.focus();
        field.scrollIntoView({ behavior: 'smooth', block: 'center' });
        field.classList.add('border-red-500', 'ring-2', 'ring-red-200');
        setTimeout(() => field.classList.remove('border-red-500', 'ring-2', 'ring-red-200'), 3000);
    }
}

function setupImageErrorHandling() {
    document.querySelectorAll('img').forEach(img => {
        if (!img.dataset.errorBound) {
            img.dataset.errorBound = 'true';
            img.addEventListener('error', (e) => {
                if (!e.target.dataset.errorHandled) {
                    e.target.dataset.errorHandled = 'true';
                    e.target.src = '/images/default.jpg';
                    e.target.alt = '이미지를 불러올 수 없습니다';
                }
            });
        }
    });
}
