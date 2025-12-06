package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.service.ProductImageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 부모 트랜잭션에 참여
public class ProductImageService {

    private final ProductImageFileService productImageFileService;

    public void updateImages(Product product, List<ProductImageDto> requestImages) {
        List<String> currentUrls = product.getImages().stream().map(ProductImage::getImageUrl).toList();
        Set<String> newUrls = requestImages != null ?
                requestImages.stream().map(ProductImageDto::getImageUrl).collect(Collectors.toSet())
                : Set.of();

        List<String> deleteTargets = currentUrls.stream()
                .filter(url -> !newUrls.contains(url))
                .toList();

        if (!deleteTargets.isEmpty()) {
            productImageFileService.deleteImages(deleteTargets);
        }

        if (requestImages == null || requestImages.isEmpty()) {
            product.getImages().clear();
            return;
        }

        // 2. DB 리스트 동기화 (삭제)
        List<Long> reqIds = requestImages.stream()
                .map(ProductImageDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        product.getImages().removeIf(img -> img.getId() != null && !reqIds.contains(img.getId()));

        // 3. DB 리스트 동기화 (생성 및 수정)
        for (ProductImageDto imgDto : requestImages) {
            if (imgDto.getId() == null || imgDto.getId() == 0) {
                // 신규
                ProductImage newImage = ProductImage.builder()
                        .imageUrl(imgDto.getImageUrl())
                        .imageType(imgDto.getImageType())
                        .displayOrder(imgDto.getDisplayOrder())
                        .build();
                product.addImage(newImage);
            } else {
                // 수정
                ProductImage existingImage = product.getImages().stream()
                        .filter(img -> img.getId().equals(imgDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

                existingImage.updateImageInfo(
                        imgDto.getImageUrl(),
                        imgDto.getImageType(),
                        imgDto.getDisplayOrder()
                );
            }
        }
    }
}