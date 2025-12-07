document.addEventListener('DOMContentLoaded', function () {
    const fileInput = document.getElementById('imageFile');
    const uploadCount = document.getElementById('uploadCount');
    const selectedFileInfo = document.getElementById('selectedFileInfo');
    const deleteImageHidden = document.getElementById('deleteImageHidden');
    const deleteImageBtn = document.getElementById('deleteImageBtn');
    const currentImageWrapper = document.getElementById('currentImageWrapper');

    const hadExistingImage = !!currentImageWrapper;

    // 🔹 기존 이미지 삭제(X 버튼 클릭)
    if (deleteImageBtn) {
        deleteImageBtn.addEventListener('click', () => {
            // 화면에서 기존 이미지 제거
            currentImageWrapper.style.display = 'none';

            // 서버로 기존 이미지 삭제 요청
            deleteImageHidden.value = "true";

            // 카운트 변경
            uploadCount.textContent = "0 / 1";

            // 파일 첨부 초기화
            if (selectedFileInfo) selectedFileInfo.textContent = "";
            if (fileInput) fileInput.value = "";
        });
    }

    // 🔹 새 파일 선택 시 동작
    if (fileInput) {
        fileInput.addEventListener('change', function () {
            const file = fileInput.files[0];

            if (file) {
                // 카운트 업데이트
                uploadCount.textContent = "1 / 1";

                // 기존 이미지가 있었다면 화면에서 숨기기
                if (currentImageWrapper) {
                    currentImageWrapper.style.display = 'none';
                }

                // 기존 삭제값 초기화
                if (deleteImageHidden) {
                    deleteImageHidden.value = "false";
                }

                // 파일 이름 + 용량 표시
                const sizeMB = (file.size / 1024 / 1024).toFixed(2);
                selectedFileInfo.textContent = `선택된 파일: ${file.name} (${sizeMB}MB)`;

            } else {
                // 파일 제거된 상태
                selectedFileInfo.textContent = "";
                uploadCount.textContent = hadExistingImage ? "1 / 1" : "0 / 1";

                // 기존 이미지 다시 보이기
                if (hadExistingImage && currentImageWrapper) {
                    currentImageWrapper.style.display = "";
                }
            }
        });
    }
});
