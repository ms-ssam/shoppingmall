package com.example.elicesecondproject.mall.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해주세요.")
/*    @Pattern(regexp = "^[0-9]{10,11}$",
            message = "전화번호는 숫자만 입력하며 10~11자리여야 합니다.")*/
    private String phone;
}
