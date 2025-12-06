package com.example.elicesecondproject.mall.domain.member.validation.validator;

import com.example.elicesecondproject.mall.domain.member.validation.annotation.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String field;
    private String fieldMatch;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.fieldMatch = constraintAnnotation.fieldMatch();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        try {
            Field fieldValue = value.getClass().getDeclaredField(field);
            fieldValue.setAccessible(true);
            Object firstValue = fieldValue.get(value);

            Field fieldMatchValue = value.getClass().getDeclaredField(fieldMatch);
            fieldMatchValue.setAccessible(true);
            Object secondValue = fieldMatchValue.get(value);

/*            // null이면 실패로 볼지 정책 선택
            if (firstValue == null || secondValue == null) {
                return false;
            }*/

            boolean matches = firstValue.equals(secondValue);
            if (matches) {
                return true;
            }

            // 여기서부터 중요: 클래스 레벨 에러를 "필드 에러"로 매핑
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            context.getDefaultConstraintMessageTemplate()
                    )
                    .addPropertyNode(fieldMatch)  // newPasswordConfirm 쪽에 에러를 붙이겠다
                    .addConstraintViolation();

            return false;

        } catch (Exception e) {
            return false;
        }
    }
}