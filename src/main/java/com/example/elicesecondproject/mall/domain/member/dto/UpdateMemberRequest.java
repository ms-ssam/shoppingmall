package com.example.elicesecondproject.mall.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRequest {

    @NotBlank
    private String nickname;
    @NotBlank
    private String phone;
}
