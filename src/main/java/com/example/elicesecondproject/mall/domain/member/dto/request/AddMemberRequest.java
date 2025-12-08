package com.example.elicesecondproject.mall.domain.member.dto.request;

import com.example.elicesecondproject.mall.domain.member.validation.annotation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch(field = "password", fieldMatch = "passwordConfirm",
        message = "입력하신 비밀번호가 서로 일치하지 않습니다.")
public class AddMemberRequest {
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    //영문자, 숫자, 특수문자 최소 1개씩 포함한 최소 8자의 비밀번호
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 최소 1개씩 포함해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호를 한번 더 입력해주세요.")
    private String confirmPassword;

    @NotBlank(message = "이름을 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$",
            message = "이름은 한글 또는 영문만 입력할 수 있습니다.")
    private String name;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{10,11}$",
            message = "전화번호는 숫자만 입력하며 10~11자리여야 합니다.")
    private String phone;
}
