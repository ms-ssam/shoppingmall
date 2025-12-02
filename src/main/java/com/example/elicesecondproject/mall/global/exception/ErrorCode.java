package com.example.elicesecondproject.mall.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 프로젝트에서 사용할 모든 예외 코드와 메시지를 정의하는 열거형(Enum)
@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C002", "입력 타입이 올바르지 않습니다."),

    CATEGORY_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "CAT003", "하위 카테고리가 존재하여 삭제할 수 없습니다."),

    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "IMG001", "지원하지 않는 이미지 형식입니다. (JPG, PNG, WEBP만 가능)"),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "IMG002", "이미지 크기가 너무 큽니다. (최대 5MB)"),

    PRODUCT_STOPPED(HttpStatus.BAD_REQUEST, "PR002", "현재 판매하지 않는 상품입니다."),

    SEARCH_KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "SRH001", "검색어를 입력해주세요."),
    SEARCH_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "SRH002", "검색어는 최소 2자 이상 입력해야 합니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "아이디 또는 비밀번호가 올바르지 않습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "RT001", "유효하지 않은 리프레시 토큰입니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PS001", "해당 게시물을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "RT002", "해당 리플레시 토큰을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT002", "해당 카테고리를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "해당 상품을 찾을 수 없습니다."),
    OPTION_COLOR_NOT_FOUND(HttpStatus.NOT_FOUND, "OPT001", "해당 색상 옵션을 찾을 수 없습니다."),
    OPTION_SIZE_NOT_FOUND(HttpStatus.NOT_FOUND, "OPT002", "해당 사이즈 옵션을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMG003", "해당 이미지를 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "C003", "이미 존재하는 데이터입니다."),
    DUPLICATE_CATEGORY_SLUG(HttpStatus.CONFLICT, "CAT001", "이미 존재하는 카테고리 슬러그입니다."),
    DUPLICATE_SKU(HttpStatus.CONFLICT, "OPT003", "중복된 SKU입니다."),
    NOT_ENOUGH_STOCK(HttpStatus.CONFLICT, "I001", "재고가 부족합니다."),
    INVALID_STOCK_QUANTITY(HttpStatus.CONFLICT, "I002", "재고 수량이 유효하지 않습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}