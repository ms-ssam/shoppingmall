package com.example.elicesecondproject.mall.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final List<CustomFieldError> errors;

    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(CustomFieldError.of(bindingResult));
    }

    public static ErrorResponse of(String field, String value, String reason) {
        return new ErrorResponse(
                List.of(new CustomFieldError(field, value, reason))
        );
    }

    @Getter
    @AllArgsConstructor
    static class CustomFieldError {
        private final String field;
        private final String value;
        private final String reason;

        public static List<CustomFieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> new CustomFieldError(
                            error.getField(),
                            toSafeValue(error.getField(), error.getRejectedValue()),
                            //error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()
                    ))
                    .toList();
        }

        private static String toSafeValue(String field, Object rejectedValue) {
            if (rejectedValue == null) {
                return "";
            }

            // 비밀번호 관련 필드는 값 숨기기
            String lower = field.toLowerCase();
            if (lower.contains("password")) {
                return "";           // 혹은 "********" 같은 마스킹 문자열
            }

            return rejectedValue.toString();
        }
    }
}