const startDateInput = document.getElementById("startDate");
const endDateInput = document.getElementById("endDate");

// 시작일 변경 시 종료일 최소값 설정
startDateInput.addEventListener("change", function () {
    const start = this.value;

    if (start) {
        // 종료일 최소값을 시작일로 설정
        endDateInput.min = start;

        // 종료일이 시작일보다 빠르면 자동 보정
        if (endDateInput.value && endDateInput.value < start) {
            endDateInput.value = start;
        }
    } else {
        // 시작일이 비어있으면 종료일 제한 해제
        endDateInput.min = "";
    }
});

// 종료일 선택 시 시작일보다 빠른 경우 방지
endDateInput.addEventListener("change", function () {
    const end = this.value;
    const start = startDateInput.value;

    if (end && start && end < start) {
        alert("종료일은 시작일보다 빠를 수 없습니다.");
        this.value = start;
        this.focus();
    }
});
