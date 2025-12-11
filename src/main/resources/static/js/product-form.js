// ==================== 전역 변수 ====================
let mainImage = null;
let sliderImages = [];
let descImages = [];
let colorVariantIndex = 0;
let sizeVariantIndexMap = {};
let isEditMode = false;
let productId = null;

// ==================== 초기화 함수 ====================
function initializeProductForm() {
    isEditMode = productData !== null;
    productId = isEditMode ? productData.id : null;

    console.log('카테고리 데이터:', categoryTreeData);
    console.log('수정 모드:', isEditMode);
    console.log('상품 데이터:', productData);

    if (isEditMode) {
        loadExistingProductData();

        if (productData.category) {
            let categoryName = productData.category.name;
            if (productData.category.parentName) {
                categoryName = productData.category.parentName + ' > ' + categoryName;
            }
            document.getElementById('categoryDisplay').value = categoryName;
        }
    }

    calculateSalePrice();
    initializeEventListeners();
    initializeSortable();
}

// ==================== 이벤트 리스너 초기화 ====================
function initializeEventListeners() {
    document.getElementById('mainImageUpload').addEventListener('click', () => {
        document.getElementById('mainImageInput').click();
    });

    document.getElementById('mainImageInput').addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleMainImage(e.target.files[0]);
        }
    });

    document.getElementById('addSliderImageBtn').addEventListener('click', () => {
        const currentCount = document.querySelectorAll('#sliderImageList .image-preview-item').length;
        if (currentCount >= 4) {
            alert('슬라이더 이미지는 최대 4장까지 등록 가능합니다.');
            return;
        }
        document.getElementById('sliderImageInput').click();
    });

    document.getElementById('sliderImageInput').addEventListener('change', (e) => {
        const files = Array.from(e.target.files);
        const currentCount = document.querySelectorAll('#sliderImageList .image-preview-item').length;

        files.forEach(file => {
            if (currentCount + sliderImages.length < 4) {
                handleSliderImage(file);
            }
        });
        e.target.value = '';
    });

    document.getElementById('addDescImageBtn').addEventListener('click', () => {
        const currentCount = document.querySelectorAll('#descImageList .image-preview-item').length;
        if (currentCount >= 10) {
            alert('상세 설명 이미지는 최대 10장까지 등록 가능합니다.');
            return;
        }
        document.getElementById('descImageInput').click();
    });

    document.getElementById('descImageInput').addEventListener('change', (e) => {
        const files = Array.from(e.target.files);
        const currentCount = document.querySelectorAll('#descImageList .image-preview-item').length;

        files.forEach(file => {
            if (currentCount + descImages.length < 10) {
                handleDescImage(file);
            }
        });
        e.target.value = '';
    });

    const mainUploadArea = document.getElementById('mainImageUpload');

    mainUploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        mainUploadArea.classList.add('drag-over');
    });

    mainUploadArea.addEventListener('dragleave', () => {
        mainUploadArea.classList.remove('drag-over');
    });

    mainUploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        mainUploadArea.classList.remove('drag-over');
        if (e.dataTransfer.files.length > 0) {
            handleMainImage(e.dataTransfer.files[0]);
        }
    });
}

// ==================== Sortable 초기화 ====================
function initializeSortable() {
    if (typeof Sortable === 'undefined') {
        console.warn('Sortable.js가 로드되지 않았습니다.');
        return;
    }

    new Sortable(document.getElementById('sliderImageList'), {
        animation: 150,
        ghostClass: 'sortable-ghost',
        dragClass: 'sortable-drag',
        onEnd: function(evt) {
            updateSliderImageOrder();
        }
    });
}

// ==================== 기존 상품 데이터 로드 ====================
function loadExistingProductData() {
    console.log('=== 기존 상품 데이터 로드 시작 ===');
    console.log('isEditMode:', isEditMode);
    console.log('productData:', productData);

    if (!productData) {
        console.error('❌ productData가 없습니다!');
        return;
    }

    // 기존 이미지 로드
    if (typeof existingImages !== 'undefined' && existingImages && existingImages.length > 0) {
        console.log('이미지 개수:', existingImages.length);
        existingImages.forEach(image => {
            if (image.imageType === 'MAIN') {
                loadExistingMainImage(image.imageUrl);
            } else if (image.imageType === 'SLIDER') {
                loadExistingSliderImage(image.imageUrl);
            } else if (image.imageType === 'DESCRIPTION') {
                loadExistingDescImage(image.imageUrl);
            }
        });
    } else {
        console.warn('⚠️ 기존 이미지가 없습니다');
    }

    if (!productData.optionGroups || productData.optionGroups.length === 0) {
        console.warn('⚠️ optionGroups가 비어있습니다!');
        return;
    }

    console.log('옵션 그룹 개수:', productData.optionGroups.length);

    productData.optionGroups.forEach((group, groupIdx) => {
        console.log(`\n--- 그룹 ${groupIdx} 처리 시작: ${group.name} ---`);

        const currentColorIndex = colorVariantIndex;
        addColorVariant();

        const colorInput = document.getElementById(`colorName${currentColorIndex}`);
        if (colorInput) {
            colorInput.value = group.name || '';
            colorInput.dataset.groupId = group.id || '';
            console.log(`✅ 색상 설정 완료: ${group.name}, ID: ${group.id}`);
        } else {
            console.error(`❌ colorName${currentColorIndex} 요소를 찾을 수 없음`);
        }

        if (group.details && group.details.length > 0) {
            console.log(`사이즈 개수: ${group.details.length}`);

            group.details.forEach((detail, detailIdx) => {
                console.log(`  사이즈 ${detailIdx}: ${detail.name}, SKU: ${detail.sku}`);

                addSizeVariant(currentColorIndex);

                const lastSizeIndex = sizeVariantIndexMap[currentColorIndex] - 1;
                const sizeDiv = document.getElementById(`size${currentColorIndex}_${lastSizeIndex}`);

                if (sizeDiv) {
                    const sizeNameInput = sizeDiv.querySelector('.size-name');
                    const skuInput = sizeDiv.querySelector('.sku-display');
                    const stockInput = sizeDiv.querySelector('.size-stock');

                    if (sizeNameInput) sizeNameInput.value = detail.name || '';
                    if (stockInput) stockInput.value = detail.stockQuantity || 0;
                    if (skuInput) skuInput.value = detail.sku || '';

                    sizeDiv.dataset.detailId = detail.id || '';

                    console.log(`  ✅ 사이즈 설정 완료: ${detail.name}`);
                } else {
                    console.error(`  ❌ size${currentColorIndex}_${lastSizeIndex} 요소를 찾을 수 없음`);
                }
            });
        } else {
            console.warn(`  ⚠️ 그룹 "${group.name}"에 details가 없습니다`);
        }
    });

    console.log('=== 기존 상품 데이터 로드 완료 ===\n');
}

// 기존 대표 이미지 로드 함수
function loadExistingMainImage(imageUrl) {
    const preview = document.getElementById('mainImagePreview');
    preview.innerHTML = `
        <div class="relative image-preview-item">
            <img src="${imageUrl}" class="w-full h-32 object-cover rounded image-thumbnail">
            <button type="button" onclick="removeMainImage()"
                    class="absolute top-2 right-2 bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600 btn-delete">
                삭제
            </button>
        </div>
    `;
    setupImageErrorHandling();
    console.log('✅ 대표 이미지 로드:', imageUrl);
}

// 기존 슬라이더 이미지 로드 함수
function loadExistingSliderImage(imageUrl) {
    const listDiv = document.getElementById('sliderImageList');
    const currentCount = listDiv.querySelectorAll('.image-preview-item').length;

    const itemDiv = document.createElement('div');
    itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 cursor-move image-preview-item';
    itemDiv.dataset.existingUrl = imageUrl;
    itemDiv.innerHTML = `
        <span class="order-badge flex items-center justify-center w-6 h-6 bg-teal-500 text-white text-xs font-bold rounded-full">${currentCount + 1}</span>
        <img src="${imageUrl}" class="w-12 h-12 object-cover rounded image-thumbnail">
        <span class="flex-1 text-sm text-gray-700 truncate">기존 이미지</span>
        <button type="button" onclick="this.closest('.image-preview-item').remove(); updateSliderImageOrder();"
                class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
            삭제
        </button>
    `;
    listDiv.appendChild(itemDiv);
    setupImageErrorHandling();
    console.log('✅ 슬라이더 이미지 로드:', imageUrl);
}

// 기존 상세 이미지 로드 함수
function loadExistingDescImage(imageUrl) {
    const listDiv = document.getElementById('descImageList');

    const itemDiv = document.createElement('div');
    itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 image-preview-item';
    itemDiv.dataset.existingUrl = imageUrl;
    itemDiv.innerHTML = `
        <img src="${imageUrl}" class="w-12 h-12 object-cover rounded image-thumbnail">
        <span class="flex-1 text-sm text-gray-700 truncate">기존 이미지</span>
        <button type="button" onclick="this.closest('.image-preview-item').remove();"
                class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
            삭제
        </button>
    `;
    listDiv.appendChild(itemDiv);
    setupImageErrorHandling();
    console.log('✅ 상세 이미지 로드:', imageUrl);
}

// 슬라이더 이미지 순서 업데이트 함수
function updateSliderImageOrder() {
    const items = document.querySelectorAll('#sliderImageList .image-preview-item');
    items.forEach((item, index) => {
        const badge = item.querySelector('.order-badge');
        if (badge) {
            badge.textContent = index + 1;
        }
    });
}

// ==================== 대표 이미지 처리 ====================
function handleMainImage(file) {
    mainImage = file;
    const reader = new FileReader();
    reader.onload = (e) => {
        const preview = document.getElementById('mainImagePreview');
        preview.innerHTML = `
            <div class="relative image-preview-item">
                <img src="${e.target.result}" class="w-full h-32 object-cover rounded image-thumbnail">
                <button type="button" onclick="removeMainImage()"
                        class="absolute top-2 right-2 bg-red-500 text-white px-2 py-1 rounded text-xs hover:bg-red-600 btn-delete">
                    삭제
                </button>
            </div>
        `;
        setupImageErrorHandling();
    };
    reader.readAsDataURL(file);
}

function removeMainImage() {
    mainImage = null;
    document.getElementById('mainImagePreview').innerHTML = '';
    document.getElementById('mainImageInput').value = '';
}

// ==================== 슬라이더 이미지 처리 ====================
function handleSliderImage(file) {
    const currentCount = document.querySelectorAll('#sliderImageList .image-preview-item').length;
    if (currentCount + sliderImages.length >= 4) {
        alert('슬라이더 이미지는 최대 4장까지 등록 가능합니다.');
        return;
    }

    sliderImages.push(file);
    renderSliderImages();
}

function removeSliderImage(index) {
    sliderImages.splice(index, 1);
    renderSliderImages();
}

function renderSliderImages() {
    const listDiv = document.getElementById('sliderImageList');

    // 기존 이미지 개수 확인
    const existingItems = listDiv.querySelectorAll('[data-existing-url]');
    const startIndex = existingItems.length;

    // 기존에 추가된 새 이미지들 제거
    const newItems = listDiv.querySelectorAll('[data-index]');
    newItems.forEach(item => item.remove());

    // 새 이미지 추가
    sliderImages.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 cursor-move image-preview-item';
            itemDiv.dataset.index = index;
            itemDiv.innerHTML = `
                <span class="order-badge flex items-center justify-center w-6 h-6 bg-teal-500 text-white text-xs font-bold rounded-full">${startIndex + index + 1}</span>
                <img src="${e.target.result}" class="w-12 h-12 object-cover rounded image-thumbnail">
                <span class="flex-1 text-sm text-gray-700 truncate">${file.name}</span>
                <button type="button" onclick="removeSliderImage(${index})"
                        class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
                    삭제
                </button>
            `;
            listDiv.appendChild(itemDiv);
            setupImageErrorHandling();
        };
        reader.readAsDataURL(file);
    });

    updateSliderImageOrder();
}

// ==================== 상세 설명 이미지 처리 ====================
function handleDescImage(file) {
    const currentCount = document.querySelectorAll('#descImageList .image-preview-item').length;
    if (currentCount + descImages.length >= 10) {
        alert('상세 설명 이미지는 최대 10장까지 등록 가능합니다.');
        return;
    }

    descImages.push(file);
    renderDescImages();
}

function removeDescImage(index) {
    descImages.splice(index, 1);
    renderDescImages();
}

function renderDescImages() {
    const listDiv = document.getElementById('descImageList');

    // 기존에 추가된 새 이미지들 제거
    const newItems = listDiv.querySelectorAll('[data-index]');
    newItems.forEach(item => item.remove());

    // 새 이미지 추가
    descImages.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 image-preview-item';
            itemDiv.dataset.index = index;
            itemDiv.innerHTML = `
                <img src="${e.target.result}" class="w-12 h-12 object-cover rounded image-thumbnail">
                <span class="flex-1 text-sm text-gray-700 truncate">${file.name}</span>
                <button type="button" onclick="removeDescImage(${index})"
                        class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
                    삭제
                </button>
            `;
            listDiv.appendChild(itemDiv);
            setupImageErrorHandling();
        };
        reader.readAsDataURL(file);
    });
}

// ==================== 카테고리 모달 ====================
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
        div.className = 'category-item p-3 hover:bg-gray-100 cursor-pointer border-b border-gray-200';
        div.textContent = parent.name;
        div.onclick = () => loadChildCategories(parent.id, parent.children, parent.name);
        parentList.appendChild(div);
    });
}

function loadChildCategories(parentId, children, parentName) {
    const childList = document.getElementById('childCategoryList');
    childList.innerHTML = '';

    children.forEach(child => {
        const div = document.createElement('div');
        div.className = 'category-item p-3 hover:bg-teal-100 cursor-pointer border-b border-gray-200';
        div.textContent = child.name;
        div.onclick = () => selectCategory(child.id, `${parentName} > ${child.name}`);
        childList.appendChild(div);
    });
}

function selectCategory(categoryId, fullPath) {
    document.getElementById('categoryId').value = categoryId;
    document.getElementById('categoryDisplay').value = fullPath;
    closeCategoryModal();
}

// ==================== 가격 자동 계산 ====================
function calculateSalePrice() {
    const regular = parseFloat(document.getElementById('regularPrice').value) || 0;
    const discount = parseFloat(document.getElementById('discountRate').value) || 0;
    const salePrice = Math.floor(regular * (1 - discount / 100));
    document.getElementById('salePriceDisplay').textContent = salePrice.toLocaleString() + '원';
}

// ==================== 색상 옵션 관리 ====================
function addColorVariant() {
    const index = colorVariantIndex++;
    const container = document.getElementById('colorVariantList');
    const variantDiv = document.createElement('div');
    variantDiv.className = 'color-variant-card border border-gray-200 rounded p-4 bg-gray-50';
    variantDiv.id = `colorVariant${index}`;
    variantDiv.innerHTML = `
        <div class="flex items-center justify-between mb-3">
            <input type="text" id="colorName${index}" placeholder="색상명 (예: 블랙)"
                   class="flex-1 px-3 py-2 border border-gray-300 rounded focus:ring-teal-500 focus:border-teal-500 mr-2"
                   oninput="updateAllSkus(${index})">
            <button type="button" onclick="removeColorVariant(${index})"
                    class="px-3 py-2 bg-red-500 text-white rounded text-sm hover:bg-red-600 btn-delete">
                삭제
            </button>
        </div>
        <div id="sizeList${index}" class="space-y-2"></div>
        <button type="button" onclick="addSizeVariant(${index})"
                class="w-full py-2 border border-dashed border-gray-300 rounded text-sm text-gray-600 hover:border-teal-500 hover:text-teal-600 transition mt-2">
            + 사이즈 추가
        </button>
    `;
    container.appendChild(variantDiv);
}

function removeColorVariant(colorIndex) {
    document.getElementById(`colorVariant${colorIndex}`).remove();
}

function addSizeVariant(colorIndex) {
    if (!sizeVariantIndexMap[colorIndex]) {
        sizeVariantIndexMap[colorIndex] = 0;
    }
    const sizeIndex = sizeVariantIndexMap[colorIndex]++;
    const sizeList = document.getElementById(`sizeList${colorIndex}`);
    const sizeDiv = document.createElement('div');
    sizeDiv.className = 'grid grid-cols-12 gap-2 items-center';
    sizeDiv.id = `size${colorIndex}_${sizeIndex}`;
    sizeDiv.innerHTML = `
        <input type="text" placeholder="사이즈 (예: M)"
               class="size-name col-span-3 px-2 py-1 border border-gray-300 rounded text-sm focus:ring-teal-500 focus:border-teal-500"
               oninput="updateSKU(${colorIndex}, ${sizeIndex})">
        <input type="text" readonly
               class="sku-display sku-auto col-span-4 px-2 py-1 border border-gray-200 bg-gray-100 rounded text-sm text-gray-600"
               placeholder="SKU (자동생성)">
        <input type="number" placeholder="재고"
               class="size-stock col-span-3 px-2 py-1 border border-gray-300 rounded text-sm focus:ring-teal-500 focus:border-teal-500">
        <button type="button" onclick="removeSizeVariant(${colorIndex}, ${sizeIndex})"
                class="col-span-2 px-2 py-1 bg-gray-300 text-gray-700 rounded text-xs hover:bg-gray-400 btn-delete">
            삭제
        </button>
    `;
    sizeList.appendChild(sizeDiv);
}

function removeSizeVariant(colorIndex, sizeIndex) {
    document.getElementById(`size${colorIndex}_${sizeIndex}`).remove();
}

// ==================== SKU 자동 생성 ====================
function updateSKU(colorIndex, sizeIndex) {
    const colorName = document.getElementById(`colorName${colorIndex}`).value.trim().toUpperCase();
    const sizeDiv = document.getElementById(`size${colorIndex}_${sizeIndex}`);
    const sizeName = sizeDiv.querySelector('.size-name').value.trim().toUpperCase();
    const skuDisplay = sizeDiv.querySelector('.sku-display');

    if (colorName && sizeName) {
        skuDisplay.value = `${colorName}-${sizeName}`;
    } else {
        skuDisplay.value = '';
    }
}

function updateAllSkus(colorIndex) {
    const sizeList = document.getElementById(`sizeList${colorIndex}`);
    const sizeDivs = sizeList.querySelectorAll('[id^="size' + colorIndex + '_"]');
    sizeDivs.forEach((div) => {
        const sizeIndex = parseInt(div.id.split('_')[1]);
        updateSKU(colorIndex, sizeIndex);
    });
}

// ==================== displayOrder 자동 조정 함수 ====================
function normalizeDisplayOrders(details) {
    if (!details || details.length === 0) {
        return;
    }

    const usedOrders = new Set();
    let nextOrder = 1;

    details.forEach(detail => {
        if (detail.displayOrder == null || usedOrders.has(detail.displayOrder)) {
            while (usedOrders.has(nextOrder)) {
                nextOrder++;
            }
            detail.displayOrder = nextOrder;
        }

        usedOrders.add(detail.displayOrder);

        if (detail.displayOrder >= nextOrder) {
            nextOrder = detail.displayOrder + 1;
        }
    });

    console.log('✅ displayOrder 정규화 완료:', details.map(d => d.displayOrder));
}

// ==================== 상품 등록/수정 ====================
function submitProduct() {
    const name = document.getElementById('productName').value.trim();
    const categoryId = document.getElementById('categoryId').value;
    const regularPrice = parseFloat(document.getElementById('regularPrice').value);
    const discountRate = parseFloat(document.getElementById('discountRate').value) || 0;
    const description = document.getElementById('productDescription').value.trim();
    const status = document.getElementById('productStatus').value;

    if (!name) {
        alert('상품명을 입력하세요.');
        return;
    }
    if (!categoryId) {
        alert('카테고리를 선택하세요.');
        return;
    }
    if (!regularPrice || regularPrice <= 0) {
        alert('정가를 입력하세요.');
        return;
    }
    if (!isEditMode && !mainImage) {
        alert('대표 이미지를 업로드하세요.');
        return;
    }

    const optionGroups = [];
    const colorDivs = document.querySelectorAll('[id^="colorVariant"]');

    colorDivs.forEach((colorDiv) => {
        const colorIndex = colorDiv.id.replace('colorVariant', '');
        const colorNameInput = document.getElementById(`colorName${colorIndex}`);

        if (!colorNameInput) return;

        const colorName = colorNameInput.value.trim();
        if (!colorName) return;

        const groupId = colorNameInput.dataset.groupId ? parseInt(colorNameInput.dataset.groupId) : null;

        const details = [];
        const sizeDivs = colorDiv.querySelectorAll(`[id^="size${colorIndex}_"]`);

        sizeDivs.forEach((sizeDiv) => {
            const sizeNameInput = sizeDiv.querySelector('.size-name');
            const skuInput = sizeDiv.querySelector('.sku-display');
            const stockInput = sizeDiv.querySelector('.size-stock');

            if (!sizeNameInput || !skuInput || !stockInput) return;

            const sizeName = sizeNameInput.value.trim();
            const sku = skuInput.value.trim();
            const stock = parseInt(stockInput.value) || 0;

            const detailId = sizeDiv.dataset.detailId ? parseInt(sizeDiv.dataset.detailId) : null;

            if (sizeName && sku) {
                details.push({
                    id: detailId,
                    name: sizeName,
                    sku: sku,
                    addPrice: 0,
                    stockQuantity: stock,
                    displayOrder: null
                });
            }
        });

        normalizeDisplayOrders(details);

        if (details.length > 0) {
            optionGroups.push({
                id: groupId,
                name: colorName,
                displayOrder: optionGroups.length + 1,
                details: details
            });
        }
    });

    if (optionGroups.length === 0) {
        alert('최소 1개 이상의 색상/사이즈 옵션을 추가하세요.');
        return;
    }

    const formData = new FormData();

    const requestData = {
        name: name,
        price: regularPrice,
        discountRate: discountRate,
        description: description,
        categoryId: parseInt(categoryId),
        status: status,
        optionGroups: optionGroups
    };

    formData.append('data', new Blob([JSON.stringify(requestData)], {type: 'application/json'}));

    if (mainImage) {
        formData.append('mainImage', mainImage);
    }
    sliderImages.forEach(img => {
        formData.append('sliderImages', img);
    });
    descImages.forEach(img => {
        formData.append('descImages', img);
    });

    console.log('전송할 데이터:', requestData);

    const url = isEditMode ? `/api/admin/products/${productId}` : '/api/admin/products';
    const method = isEditMode ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        body: formData
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert(isEditMode ? "상품이 수정되었습니다." : "상품이 성공적으로 등록되었습니다.");

                if (!isEditMode) {
                    location.href = '/admin/products';
                }
            } else {
                alert((isEditMode ? "수정 실패: " : "등록 실패: ") + data.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert("서버 오류가 발생했습니다.");
        });
}

// ==================== 이미지 에러 핸들링 ====================
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
