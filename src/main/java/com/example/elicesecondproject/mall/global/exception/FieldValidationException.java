package com.example.elicesecondproject.mall.global.exception;

import lombok.Getter;

@Getter
public class FieldValidationException extends RuntimeException {
    private final String field;
    private final String value;
    private final String reason;

    public FieldValidationException(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value == null ? "" : value;
        this.reason = message;
    }
}