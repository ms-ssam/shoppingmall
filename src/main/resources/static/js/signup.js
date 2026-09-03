document.addEventListener('DOMContentLoaded', () => {
    const signupForm = document.getElementById('signupForm');
    const signupError = document.getElementById('signupError');

    if (!signupForm || !signupError) return;

    signupForm.addEventListener('submit', (e) => {
        const passwordEl = document.getElementById('password');
        const passwordConfirmEl = document.getElementById('passwordConfirm');

        const password = passwordEl ? passwordEl.value : '';
        const passwordConfirm = passwordConfirmEl ? passwordConfirmEl.value : '';

        signupError.style.display = 'none';
        signupError.textContent = '';

        if (password !== passwordConfirm) {
            e.preventDefault();
            signupError.textContent = '비밀번호가 일치하지 않습니다.';
            signupError.style.display = 'block';
        }
    });
});
