package com.example.elicesecondproject.mall.global.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import com.example.elicesecondproject.mall.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * @Valid 어노테이션을 사용한 DTO의 유효성 검증 실패 시 발생하는 예외를 처리.
     * (주로 @RequestBody에 대한 유효성 검증 실패)
     * @param ex MethodArgumentNotValidException
     * @return 400 Bad Request와 함께 유효성 검증 실패 메시지를 담은 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed (RequestBody): {}", errorMessage);

        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE));
    }

    /**
     * @Valid 어노테이션을 사용한 PathVariable 또는 RequestParam의 유효성 검증 실패 시 발생하는 예외를 처리.
     * @param ex ConstraintViolationException
     * @return 400 Bad Request와 함께 유효성 검증 실패 메시지를 담은 응답
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed (PathVariable/RequestParam): {}", errorMessage);

        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE));
    }

    /**
     * 비즈니스 로직 상의 예외(BusinessException)를 처리.
     * ErrorCode에 정의된 상태 코드와 메시지를 사용하여 응답.
     * @param ex BusinessException
     * @return ErrorCode에 명시된 HTTP 상태 코드와 에러 메시지를 담은 응답
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("BusinessException: status={}, code={}, message={}",
                errorCode.getStatus(), errorCode.name(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    /**
     * 위에서 처리되지 않은 모든 예외를 최종적으로 처리.
     * 서버 내부 오류로 간주하고 500 Internal Server Error를 반환.
     * @param ex Exception
     * @return 500 Internal Server Error와 함께 일반적인 서버 오류 메시지를 담은 응답
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex); // 스택 트레이스를 포함하여 로깅
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}