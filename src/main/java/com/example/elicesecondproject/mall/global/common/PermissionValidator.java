package com.example.elicesecondproject.mall.global.common;

import com.example.elicesecondproject.mall.domain.member.entity.Member;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class PermissionValidator {

    public void validate(Ownable resource, Member member) {
        Long ownerId = resource.getOwnerId();
        validateOwnerOrAdmin(ownerId, member);
    }

    private void validateOwnerOrAdmin(Long ownerId, Member member) {
        boolean isOwner = ownerId.equals(member.getId());
        boolean isAdmin = member.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public void validateAdminOnly(Member member) {
        if (!member.isAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}

