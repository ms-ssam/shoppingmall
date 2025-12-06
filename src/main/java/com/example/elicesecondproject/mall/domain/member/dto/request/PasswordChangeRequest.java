package com.example.elicesecondproject.mall.domain.member.dto.request;

import com.example.elicesecondproject.mall.domain.member.validation.annotation.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@PasswordMatch(field = "newPassword", fieldMatch = "newPasswordConfirm",
        message = "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.")
public class PasswordChangeRequest {
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$",
            message = "영문, 숫자, 특수문자(@$!%*#?&)를 최소 1개씩 포함해야 합니다."
    )
    //@ValidPassword
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String newPasswordConfirm;

}
