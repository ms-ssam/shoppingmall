package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.member.entity.MemberStatus;
import com.example.elicesecondproject.mall.domain.member.repositorty.MemberRepository;
import com.example.elicesecondproject.mall.domain.product.dto.WishListProductResponse;
import com.example.elicesecondproject.mall.domain.product.entity.WishList;
import com.example.elicesecondproject.mall.domain.product.mapper.ProductMapper;
import com.example.elicesecondproject.mall.domain.product.repository.WishListRepository;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishListService {
    private final WishListRepository wishListRepository;
    private final MemberRepository memberRepository;
    private final ProductMapper productMapper;

    public Page<WishListProductResponse> getWishListByMember(Long memberId, Pageable pageable) {
        // 1. 회원 상태 검증
        if (!memberRepository.existsByIdAndStatus(memberId, MemberStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 위시리스트 페이징 조회 (WishList 엔티티 Page)
        Page<WishList> wishListPage = wishListRepository.findAllByMemberId(memberId, pageable);

        // 3. Page.map()으로 DTO 변환
        return wishListPage.map(w -> productMapper.toWishListProductResponse(w.getProduct()));
    }
}
