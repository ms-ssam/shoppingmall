package com.example.elicesecondproject.mall.domain.option.service;

import com.example.elicesecondproject.mall.domain.option.dto.OptionDetailDto;
import com.example.elicesecondproject.mall.domain.option.dto.ProductOptionGroupDto;
import com.example.elicesecondproject.mall.domain.option.entity.OptionDetail;
import com.example.elicesecondproject.mall.domain.option.entity.ProductOptionGroup;
import com.example.elicesecondproject.mall.domain.option.repository.OptionDetailRepository;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional // 부모 트랜잭션에 참여
public class ProductOptionService {

    private final OptionDetailRepository optionDetailRepository;

    public ProductOptionService(OptionDetailRepository optionDetailRepository) {
        this.optionDetailRepository = optionDetailRepository;
    }

    public void updateOptionGroups(Product product, List<ProductOptionGroupDto> requestGroups) {
        if (requestGroups == null || requestGroups.isEmpty()) {
            product.getOptionGroups().clear();
            return;
        }

        // 1. 삭제 (DELETE)
        List<Long> requestIds = requestGroups.stream()
                .map(ProductOptionGroupDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        product.getOptionGroups().removeIf(group ->
                group.getId() != null && !requestIds.contains(group.getId()));

        // 2. 생성 및 수정 (CREATE & UPDATE)
        for (ProductOptionGroupDto groupDto : requestGroups) {
            if (groupDto.getId() == null || groupDto.getId() == 0) {
                // 신규 생성
                ProductOptionGroup newGroup = ProductOptionGroup.builder()
                        .name(groupDto.getName())
                        .displayOrder(groupDto.getDisplayOrder())
                        .build();

                if (groupDto.getDetails() != null) {
                    groupDto.getDetails().forEach(detailDto -> {
                        //SKU 중복 체크
                        if (optionDetailRepository.existsBySku(detailDto.getSku())) {
                            throw new BusinessException(ErrorCode.DUPLICATE_SKU);
                        }
                        OptionDetail newDetail = OptionDetail.builder()
                                .name(detailDto.getName())
                                .sku(detailDto.getSku())
                                .addPrice(detailDto.getAddPrice())
                                .stockQuantity(detailDto.getStockQuantity())
                                .displayOrder(detailDto.getDisplayOrder())
                                .build();
                        newGroup.addDetail(newDetail);
                    });
                }
                product.addOptionGroup(newGroup);

            } else {
                // 수정
                ProductOptionGroup existingGroup = product.getOptionGroups().stream()
                        .filter(g -> g.getId().equals(groupDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_COLOR_NOT_FOUND)); // 적절한 에러코드 사용

                existingGroup.update(groupDto.getName(), groupDto.getDisplayOrder());

                // 상세 옵션 동기화 호출
                updateOptionDetails(existingGroup, groupDto.getDetails());
            }
        }
    }

    private void updateOptionDetails(ProductOptionGroup group, List<OptionDetailDto> requestDetails) {
        if (requestDetails == null || requestDetails.isEmpty()) {
            group.getDetails().clear();
            return;
        }

        List<Long> requestIds = requestDetails.stream()
                .map(OptionDetailDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        group.getDetails().removeIf(detail ->
                detail.getId() != null && !requestIds.contains(detail.getId()));

        for (OptionDetailDto detailDto : requestDetails) {
            if (detailDto.getId() == null || detailDto.getId() == 0) {
                OptionDetail newDetail = OptionDetail.builder()
                        .name(detailDto.getName())
                        .sku(detailDto.getSku())
                        .addPrice(detailDto.getAddPrice())
                        .stockQuantity(detailDto.getStockQuantity())
                        .displayOrder(detailDto.getDisplayOrder())
                        .build();
                group.addDetail(newDetail);
            } else {
                OptionDetail existingDetail = group.getDetails().stream()
                        .filter(d -> d.getId().equals(detailDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

                existingDetail.update(
                        detailDto.getName(),
                        detailDto.getSku(),
                        detailDto.getAddPrice(),
                        detailDto.getStockQuantity(),
                        detailDto.getDisplayOrder()
                );
            }
        }
    }
}