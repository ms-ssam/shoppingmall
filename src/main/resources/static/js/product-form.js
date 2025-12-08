// ==================== 전역 변수 ====================
let mainImage = null;
let sliderImages = [];
let descImages = [];
let colorVariantIndex = 0;
let sizeVariantIndexMap = {};

// Thymeleaf에서 주입받을 전역 변수 (HTML에서 설정됨)
let categoryTreeData = [];
let productData = null;
let isEditMode = false;
let productId = null;

// ==================== 초기화 함수 ====================
function initializeProductForm(categoryTree, product) {
    categoryTreeData = categoryTree;
    productData = product;
    isEditMode = productData !== null;
    productId = isEditMode ? productData.id : null;

    console.log('카테고리 데이터:', categoryTreeData);
    console.log('수정 모드:', isEditMode);
    console.log('상품 데이터:', productData);

    if (isEditMode) {
        loadExistingProductData();

        // 카테고리 이름 채우기
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
    // 대표 이미지
    document.getElementById('mainImageUpload').addEventListener('click', () => {
        document.getElementById('mainImageInput').click();
    });

    document.getElementById('mainImageInput').addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleMainImage(e.target.files[0]);
        }
    });

    // 슬라이더 이미지
    document.getElementById('addSliderImageBtn').addEventListener('click', () => {
        if (sliderImages.length >= 4) {
            alert('슬라이더 이미지는 최대 4장까지 등록 가능합니다.');
            return;
        }
        document.getElementById('sliderImageInput').click();
    });

    document.getElementById('sliderImageInput').addEventListener('change', (e) => {
        const files = Array.from(e.target.files);
        files.forEach(file => {
            if (sliderImages.length < 4) {
                handleSliderImage(file);
            }
        });
        e.target.value = '';
    });

    // 상세 설명 이미지
    document.getElementById('addDescImageBtn').addEventListener('click', () => {
        if (descImages.length >= 10) {
            alert('상세 설명 이미지는 최대 10장까지 등록 가능합니다.');
            return;
        }
        document.getElementById('descImageInput').click();
    });

    document.getElementById('descImageInput').addEventListener('change', (e) => {
        const files = Array.from(e.target.files);
        files.forEach(file => {
            if (descImages.length < 10) {
                handleDescImage(file);
            }
        });
        e.target.value = '';
    });

    // 드래그 앤 드롭
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
    new Sortable(document.getElementById('sliderImageList'), {
        animation: 150,
        ghostClass: 'sortable-ghost',
        dragClass: 'sortable-drag',
        onEnd: function(evt) {
            const oldIndex = evt.oldIndex;
            const newIndex = evt.newIndex;
            const temp = sliderImages[oldIndex];
            sliderImages.splice(oldIndex, 1);
            sliderImages.splice(newIndex, 0, temp);
            renderSliderImages();
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

    if (!productData.optionGroups || productData.optionGroups.length === 0) {
        console.warn('⚠️ optionGroups가 비어있습니다!');
        return;
    }

    console.log('옵션 그룹 개수:', productData.optionGroups.length);

    productData.optionGroups.forEach((group, groupIdx) => {
        console.log(`\n--- 그룹 ${groupIdx} 처리 시작: ${group.name} ---`);

        const currentColorIndex = colorVariantIndex;
        addColorVariant();

        // 색상 설정
        const colorInput = document.getElementById(`colorName${currentColorIndex}`);
        if (colorInput) {
            colorInput.value = group.name || '';
            colorInput.dataset.groupId = group.id || '';
            console.log(`✅ 색상 설정 완료: ${group.name}, ID: ${group.id}`);
        } else {
            console.error(`❌ colorName${currentColorIndex} 요소를 찾을 수 없음`);
        }

        // 각 사이즈 추가
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
    if (sliderImages.length >= 4) {
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
    listDiv.innerHTML = '';
    sliderImages.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 cursor-move image-preview-item';
            itemDiv.dataset.index = index;
            itemDiv.innerHTML = `
                <span class="order-badge flex items-center justify-center w-6 h-6 bg-teal-500 text-white text-xs font-bold rounded-full">${index + 1}</span>
                <img src="${e.target.result}" class="w-12 h-12 object-cover rounded image-thumbnail">
                <span class="flex-1 text-sm text-gray-700 truncate">${file.name}</span>
                <button type="button" onclick="removeSliderImage(${index})"
                        class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
                    삭제
                </button>
            `;
            listDiv.appendChild(itemDiv);
        };
        reader.readAsDataURL(file);
    });
}

// ==================== 상세 설명 이미지 처리 ====================
function handleDescImage(file) {
    if (descImages.length >= 10) {
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
    listDiv.innerHTML = '';
    descImages.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = (e) => {
            const itemDiv = document.createElement('div');
            itemDiv.className = 'flex items-center gap-3 p-2 border border-gray-200 rounded bg-gray-50 image-preview-item';
            itemDiv.innerHTML = `
                <img src="${e.target.result}" class="w-12 h-12 object-cover rounded image-thumbnail">
                <span class="flex-1 text-sm text-gray-700 truncate">${file.name}</span>
                <button type="button" onclick="removeDescImage(${index})"
                        class="px-2 py-1 bg-red-500 text-white rounded text-xs hover:bg-red-600 btn-delete">
                    삭제
                </button>
            `;
            listDiv.appendChild(itemDiv);
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

    // 옵션 그룹 수집 (ID 포함)
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

        let displayOrder = 1;
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
                    displayOrder: displayOrder++
                });
            }
        });

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

    // FormData 생성
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
            if(data.success) {
                alert(isEditMode ? "상품이 수정되었습니다." : "상품이 성공적으로 등록되었습니다.");
                location.href = '/admin/products';
            } else {
                alert(isEditMode ? "수정 실패: " + data.message : "등록 실패: " + data.message);
            }
        })
        .catch(err => {
            console.error(err);
            alert("서버 오류가 발생했습니다.");
        });
}
