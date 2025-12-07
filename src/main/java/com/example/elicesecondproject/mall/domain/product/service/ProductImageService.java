package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.service.ProductImageFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 부모 트랜잭션에 참여
public class ProductImageService {

    private final ProductImageFileService productImageFileService;

    public void uploadAndSaveImages(Product product, MultipartFile main, List<MultipartFile> sliders, List<MultipartFile> descs) {
        try {
            // 1. MAIN 이미지 처리
            if (main != null && !main.isEmpty()) {
                String url = productImageFileService.saveImage(product.getId(), main, ProductImageFileService.UploadTarget.MAIN);

                product.addImage(ProductImage.builder()
                        .imageUrl(url)
                        .imageType(ImageType.MAIN)
                        .displayOrder(0)
                        .build());

                // Product 썸네일 필드 업데이트
                product.updateThumbnailUrl(url);
            }

            // 2. SLIDER 이미지 처리
            if (sliders != null && !sliders.isEmpty()) {
                for (int i = 0; i < sliders.size(); i++) {
                    String url = productImageFileService.saveImage(product.getId(), sliders.get(i), ProductImageFileService.UploadTarget.SLIDER);
                    product.addImage(ProductImage.builder()
                            .imageUrl(url)
                            .imageType(ImageType.SLIDER)
                            .displayOrder(i)
                            .build());
                }
            }

            // 3. DESCRIPTION 이미지 처리
            if (descs != null && !descs.isEmpty()) {
                for (int i = 0; i < descs.size(); i++) {
                    String url = productImageFileService.saveImage(product.getId(), descs.get(i), ProductImageFileService.UploadTarget.DESCRIPTION);
                    product.addImage(ProductImage.builder()
                            .imageUrl(url)
                            .imageType(ImageType.DESCRIPTION)
                            .displayOrder(i)
                            .build());
                }
            }
        } catch (IOException e) {
            // 체크 예외를 런타임 예외로 감싸서 서비스 계층 밖으로 던짐 (트랜잭션 롤백 유도)
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }



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