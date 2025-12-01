package com.example.elicesecondproject.mall.domain.Member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMemberRequest {
    private String email;
    private String password;
}
