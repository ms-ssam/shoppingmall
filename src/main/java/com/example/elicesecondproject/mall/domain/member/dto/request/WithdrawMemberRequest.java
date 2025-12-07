package com.example.elicesecondproject.mall.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class WithdrawMemberRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
