package com.example.elicesecondproject.mall.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min=2, max=20, message = "닉네임은 2~20자여야 합니다.")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^010[0-9]{8}$",
            message = "전화번호는 010으로 시작하며 숫자만 입력한 11자리여야 합니다.")
    private String phone;
}
