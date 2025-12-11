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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ProductOptionService {

    private final OptionDetailRepository optionDetailRepository;

    public ProductOptionService(OptionDetailRepository optionDetailRepository) {
        this.optionDetailRepository = optionDetailRepository;
    }

    public void updateOptionGroups(Product product, List<ProductOptionGroupDto> requestGroups) {
        if (requestGroups == null || requestGroups.isEmpty()) {
            product.getOptionGroups().clear(); // 소프트 딜리트 -> 전체 그룹 + 하위 옵션 물리 삭제
            return;
        }

        // 1. 삭제 (DELETE) - Soft Delete 적용
        List<Long> requestIds = requestGroups.stream()
                .map(ProductOptionGroupDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        /*product.getOptionGroups().stream()
                .filter(group -> group.getId() != null && !requestIds.contains(group.getId()));
                .forEach(ProductOptionGroup::softDelete); 소프트 딜리트 삭제*/

        // ✅ 소프트 딜리트 삭제
        product.getOptionGroups().removeIf(group ->
                group.getId() != null && !requestIds.contains(group.getId())
        );

        // 2. 생성 및 수정 (CREATE & UPDATE)
        for (ProductOptionGroupDto groupDto : requestGroups) {
            if (groupDto.getId() == null || groupDto.getId() == 0) {
                // 신규 생성
                ProductOptionGroup newGroup = ProductOptionGroup.builder()
                        .name(groupDto.getName())
                        .displayOrder(groupDto.getDisplayOrder())
                        .build();

                if (groupDto.getDetails() != null) {
                    normalizeDisplayOrders(groupDto.getDetails());

                    groupDto.getDetails().forEach(detailDto -> {
                        // SKU 중복 체크
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
                        .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_COLOR_NOT_FOUND));

                existingGroup.update(groupDto.getName(), groupDto.getDisplayOrder());
                updateOptionDetails(existingGroup, groupDto.getDetails());
            }
        }
    }

    private void updateOptionDetails(ProductOptionGroup group, List<OptionDetailDto> requestDetails) {
        if (requestDetails == null || requestDetails.isEmpty()) {
            group.getDetails().clear(); // 전체 삭제
            return;
        }

        // 요청에 포함된 ID 목록
        List<Long> requestIds = requestDetails.stream()
                .map(OptionDetailDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        // 요청에 없는 기존 디테일 제거
        group.getDetails().removeIf(detail ->
                detail.getId() != null && !requestIds.contains(detail.getId())
        );

        normalizeDisplayOrders(requestDetails);

        for (OptionDetailDto detailDto : requestDetails) {
            if (detailDto.getId() == null || detailDto.getId() == 0) {
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
                group.addDetail(newDetail);
            } else {
                OptionDetail existingDetail = group.getDetails().stream()
                        .filter(d -> d.getId().equals(detailDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

                if (!existingDetail.getSku().equals(detailDto.getSku())) {
                    if (optionDetailRepository.existsBySkuAndIdNot(detailDto.getSku(), existingDetail.getId())) {
                        throw new BusinessException(ErrorCode.DUPLICATE_SKU);
                    }
                }

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

    // [추가] displayOrder 중복 제거 및 자동 조정
    private void normalizeDisplayOrders(List<OptionDetailDto> details) {
        if (details == null || details.isEmpty()) {
            return;
        }

        Set<Integer> usedOrders = new HashSet<>();
        int nextOrder = 1;

        for (OptionDetailDto detail : details) {
            // displayOrder가 null이거나 중복된 경우
            if (detail.getDisplayOrder() == null || usedOrders.contains(detail.getDisplayOrder())) {
                // 사용 가능한 다음 순서 찾기
                while (usedOrders.contains(nextOrder)) {
                    nextOrder++;
                }
                detail.setDisplayOrder(nextOrder);
            }

            usedOrders.add(detail.getDisplayOrder());

            // 다음 순서 업데이트
            if (detail.getDisplayOrder() >= nextOrder) {
                nextOrder = detail.getDisplayOrder() + 1;
            }
        }
    }

    public void decreaseStock(Long optionDetailId, int quantity) {
        OptionDetail optionDetail = optionDetailRepository.findById(optionDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

        optionDetail.removeStock(quantity);

        //TODO: 리팩토링 - 총 재고 필드 삭제
        /*Product product = optionDetail.getProductOptionGroup().getProduct();
        product.recalculateTotalStock();*/
    }
}
