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
        // [수정] clear()는 orphanRemoval에 의해 물리 삭제를 유발하므로 softDelete로 변경
        if (requestGroups == null || requestGroups.isEmpty()) {
            product.getOptionGroups().forEach(ProductOptionGroup::softDelete);
            return;
        }

        // 1. 삭제 (DELETE) - Soft Delete 적용
        List<Long> requestIds = requestGroups.stream()
                .map(ProductOptionGroupDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        // 리스트에서 제거하지 않고 softDelete 호출 (물리 삭제 방지)
        product.getOptionGroups().stream()
                .filter(group -> group.getId() != null && !requestIds.contains(group.getId()))
                .forEach(ProductOptionGroup::softDelete);

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

                // 상세 옵션 동기화 호출
                updateOptionDetails(existingGroup, groupDto.getDetails());
            }
        }
    }

    private void updateOptionDetails(ProductOptionGroup group, List<OptionDetailDto> requestDetails) {
        // [수정] clear() 대신 softDelete 호출하여 논리 삭제 유지
        if (requestDetails == null || requestDetails.isEmpty()) {
            group.getDetails().forEach(OptionDetail::softDelete);
            return;
        }

        List<Long> requestIds = requestDetails.stream()
                .map(OptionDetailDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        group.getDetails().stream()
                .filter(detail -> detail.getId() != null && !requestIds.contains(detail.getId()))
                .forEach(OptionDetail::softDelete);

        for (OptionDetailDto detailDto : requestDetails) {
            if (detailDto.getId() == null || detailDto.getId() == 0) {
                // [추가] 상세 옵션 추가 시에도 SKU 중복 체크 필수
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

                // SKU가 변경되었을 때만 중복 체크 (자기 ID 제외)
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


    public void decreaseStock(Long optionDetailId, int quantity) {
        OptionDetail optionDetail = optionDetailRepository.findById(optionDetailId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_SIZE_NOT_FOUND));

        // 1. 옵션 재고 차감
        optionDetail.removeStock(quantity);

        // 2. 상품(Product) 총 재고 재계산 (트랜잭션 내에서 한 번만 호출)
        Product product = optionDetail.getProductOptionGroup().getProduct();
        product.recalculateTotalStock();
    }
}
