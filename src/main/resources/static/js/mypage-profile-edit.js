document.addEventListener('DOMContentLoaded', function () {
    const editButton = document.getElementById('edit-button');
    const submitButton = document.getElementById('submit-button');
    const nicknameInput = document.getElementById('nickname');
    const phoneInput = document.getElementById('phone');

    // 에러가 있는지 확인 (유효성 검증 실패 시 만들어진 .error 요소들)
    const hasErrors = document.querySelectorAll('.error').length > 0;

    if (hasErrors) {
        // 에러가 있으면: 이미 수정 중인 상태로 시작
        nicknameInput.removeAttribute('readonly');
        phoneInput.removeAttribute('readonly');
        submitButton.disabled = false;

        if (editButton) {
            editButton.disabled = true;
        }
    } else {
        // 에러가 없으면: 처음에는 조회 모드 (readonly + submit 비활성화)
        // readonly는 이미 HTML에 달려 있으니까 여기선 submit만 비활성화하면 돼
        if (submitButton) {
            submitButton.disabled = true;
        }
    }

    // "수정하기" 버튼 눌렀을 때만 수정 모드로 전환
    if (editButton) {
        editButton.addEventListener('click', function () {
            nicknameInput.removeAttribute('readonly');
            phoneInput.removeAttribute('readonly');

            submitButton.disabled = false;
            editButton.disabled = true;
        });
    }
});
