package com.example.elicesecondproject.mall.global.response;

import com.example.elicesecondproject.mall.global.dto.ErrorResponse;
import lombok.Getter;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;  // 응답 결과 메시지
    private final T data;  // 실제 응답 데이터

    private ApiResponse(boolean isSuccess, String code, String message, T data) {
        this.success = isSuccess;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청 성공", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ApiResponse<ErrorResponse> fail(ErrorCode errorCode, ErrorResponse errors) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), errors);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), data);
    }
}