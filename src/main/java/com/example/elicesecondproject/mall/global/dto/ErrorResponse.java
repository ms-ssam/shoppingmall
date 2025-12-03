package com.example.elicesecondproject.mall.global.dto;

import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ErrorResponse { // TODO : 맞게 수정해서 사용하기.

    private final String message;
    private final int status;
    private final List<CustomFieldError> errors;
    private final String code;

    private ErrorResponse(String message, int status, List<CustomFieldError> errors, String code) {
        this.message = message;
        this.status = status;
        this.errors = errors;
        this.code = code;
    }

    public static ErrorResponse of(String message, int status, BindingResult bindingResult) {
        return new ErrorResponse(message, status, CustomFieldError.of(bindingResult), "INVALID_INPUT_VALUE");
    }

    @Getter
    public static class CustomFieldError {
        private final String field;
        private final String value;
        private final String reason;

        private CustomFieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        public static List<CustomFieldError> of(BindingResult bindingResult) {
            final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new CustomFieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
    }
}