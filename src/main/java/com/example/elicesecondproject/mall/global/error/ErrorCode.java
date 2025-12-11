package com.example.elicesecondproject.mall.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 프로젝트에서 사용할 모든 예외 코드와 메시지를 정의하는 열거형(Enum)
@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C002", "입력 타입이 올바르지 않습니다."),

    CATEGORY_HAS_CHILDREN(HttpStatus.BAD_REQUEST, "CAT003", "하위 카테고리가 존재하여 삭제할 수 없습니다."),
    CATEGORY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "CAT004", "카테고리는 2단계까지만 생성할 수 있습니다."),
    CATEGORY_CANNOT_BE_ITS_OWN_PARENT(HttpStatus.BAD_REQUEST, "CAT005", "카테고리는 자기 자신을 부모로 설정할 수 없습니다."),

    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "IMG001", "지원하지 않는 이미지 형식입니다. (JPG, PNG, WEBP만 가능)"),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "IMG002", "이미지 크기가 너무 큽니다. (최대 5MB)"),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "IMG004", "이미지 URL이 유효하지 않습니다."),

    PRODUCT_STOPPED(HttpStatus.BAD_REQUEST, "PR002", "현재 판매하지 않는 상품입니다."),

    SEARCH_KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "SRH001", "검색어를 입력해주세요."),
    SEARCH_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "SRH002", "검색어는 최소 2자 이상 입력해야 합니다."),

    INVALID_ORDER_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "OD002", "허용되지 않은 주문 상태 변경입니다."),

    ORDER_CART_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "OD003", "선택한 장바구니 상품이 존재하지 않습니다."),
    ORDER_CART_ITEMS_INVALID(HttpStatus.BAD_REQUEST, "OD005", "유효하지 않은 장바구니 상품이 포함되어 있습니다."),

    ORDER_OPTION_INVALID(HttpStatus.BAD_REQUEST, "OPT004", "유효하지 않은 상품 옵션입니다."),
    OPTION_SOLD_OUT(HttpStatus.BAD_REQUEST, "OPT005", "선택하신 옵션은 품절되었습니다."),

    INVALID_PAYMENT_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "PM003", "허용되지 않은 결제 상태 변경입니다."),


    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A003", "아이디 또는 비밀번호가 올바르지 않습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "RT001", "유효하지 않은 리프레시 토큰입니다."),
    MEMBER_WITHDRAWN(HttpStatus.UNAUTHORIZED, "M001", "탈퇴한 회원입니다. 로그인이 불가능합니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RV002", "이 리뷰에 접근할 권한이 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "OD004", "해당 주문에 접근할 권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PS001", "해당 게시물을 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "RT002", "해당 리플레시 토큰을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT002", "해당 카테고리를 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "해당 장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CI001", "해당 장바구니 항목을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "해당 상품을 찾을 수 없습니다."),
    OPTION_COLOR_NOT_FOUND(HttpStatus.NOT_FOUND, "OPT001", "해당 색상 옵션을 찾을 수 없습니다."),
    OPTION_SIZE_NOT_FOUND(HttpStatus.NOT_FOUND, "OPT002", "해당 사이즈 옵션을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMG003", "해당 이미지를 찾을 수 없습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "RV001", "해당 리뷰를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OD001", "해당 주문을 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "C003", "이미 존재하는 데이터입니다."),
    DUPLICATE_CATEGORY_SLUG(HttpStatus.CONFLICT, "CAT001", "이미 존재하는 카테고리 슬러그입니다."),
    DUPLICATE_SKU(HttpStatus.CONFLICT, "OPT003", "중복된 SKU입니다."),
    NOT_ENOUGH_STOCK(HttpStatus.CONFLICT, "I001", "재고가 부족합니다."),
    INVALID_STOCK_QUANTITY(HttpStatus.CONFLICT, "I002", "재고 수량이 유효하지 않습니다."),
    CART_ITEM_PRODUCT_MISMATCH(HttpStatus.CONFLICT, "CI002", "해당 상품의 옵션이 아닙니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류가 발생했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002","파일 업로드에 실패했습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.INTERNAL_SERVER_ERROR, "PM001", "주문 금액과 결제 금액이 일치하지 않습니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PM002", "결제 승인에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}