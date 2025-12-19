package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.entity.ImageType;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import com.example.elicesecondproject.mall.global.error.ErrorCode;
import com.example.elicesecondproject.mall.global.error.exception.BusinessException;
import com.example.elicesecondproject.mall.global.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // 부모 트랜잭션에 참여
public class ProductImageService {

    private final FileService fileService;


    public void uploadAndSaveImages(Product product, MultipartFile main, List<MultipartFile> sliders, List<MultipartFile> descs) {
        List<String> uploadedPaths = new ArrayList<>();

        try {
            // 1. MAIN 이미지 처리 (대표 이미지는 보통 0번 고정)
            if (main != null && !main.isEmpty()) {
                String url = fileService.saveImage(product.getId(), main, FileService.UploadTarget.MAIN);
                uploadedPaths.add(url);

                product.addImage(ProductImage.builder()
                        .imageUrl(url)
                        .imageType(ImageType.MAIN)
                        .displayOrder(0)
                        .build());
                product.updateThumbnailUrl(url);
            }

            // 2. SLIDER 이미지 처리 (기존 최대 순서값 파악 후 추가)
            if (sliders != null && !sliders.isEmpty()) {
                int nextOrder = getNextOrder(product, ImageType.SLIDER);
                for (MultipartFile file : sliders) {
                    if (file.isEmpty()) continue;
                    String url = fileService.saveImage(product.getId(), file, FileService.UploadTarget.SLIDER);
                    uploadedPaths.add(url);

                    product.addImage(ProductImage.builder()
                            .imageUrl(url)
                            .imageType(ImageType.SLIDER)
                            .displayOrder(nextOrder++)
                            .build());
                }
            }

            // 3. DESCRIPTION 이미지 처리
            if (descs != null && !descs.isEmpty()) {
                int nextOrder = getNextOrder(product, ImageType.DESCRIPTION);
                for (MultipartFile file : descs) {
                    if (file.isEmpty()) continue;
                    String url = fileService.saveImage(product.getId(), file, FileService.UploadTarget.DESCRIPTION);
                    uploadedPaths.add(url);

                    product.addImage(ProductImage.builder()
                            .imageUrl(url)
                            .imageType(ImageType.DESCRIPTION)
                            .displayOrder(nextOrder++)
                            .build());
                }
            }
        } catch (Exception e) {
            if (!uploadedPaths.isEmpty()) {
                log.error("[ProductImageService] 오류 발생으로 업로드된 파일 삭제: {}", uploadedPaths.size());
                fileService.deleteImages(uploadedPaths);
            }
            if (e instanceof BusinessException) throw (BusinessException) e;
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    public void updateImages(Product product, List<ProductImageDto> requestImages) {
        // 1. 삭제할 물리 파일 식별 및 삭제
        List<String> currentUrls = product.getImages().stream().map(ProductImage::getImageUrl).toList();
        Set<String> newUrls = requestImages != null ?
                requestImages.stream().map(ProductImageDto::getImageUrl).collect(Collectors.toSet()) : Set.of();

        List<String> deleteTargets = currentUrls.stream()
                .filter(url -> !newUrls.contains(url))
                .toList();

        if (!deleteTargets.isEmpty()) {
            fileService.deleteImages(deleteTargets);
        }

        if (requestImages == null || requestImages.isEmpty()) {
            product.getImages().clear();
            return;
        }

        // 2. DB 리스트 동기화 (ID가 없는 것은 삭제)
        List<Long> reqIds = requestImages.stream()
                .map(ProductImageDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        product.getImages().removeIf(img -> img.getId() != null && !reqIds.contains(img.getId()));

        // 3. 기존 이미지 정보 및 순서(displayOrder) 업데이트
        for (ProductImageDto imgDto : requestImages) {
            if (imgDto.getId() != null && imgDto.getId() > 0) {
                ProductImage existingImage = product.getImages().stream()
                        .filter(img -> img.getId().equals(imgDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));

                // 클라이언트가 보낸 순서값을 엔티티에 반영
                existingImage.updateImageInfo(
                        imgDto.getImageUrl(),
                        imgDto.getImageType(),
                        imgDto.getDisplayOrder()
                );
            }
        }
    }

    private int getNextOrder(Product product, ImageType type) {
        return product.getImages().stream()
                .filter(img -> img.getImageType() == type)
                .map(ProductImage::getDisplayOrder)
                .max(Comparator.naturalOrder())
                .orElse(-1) + 1;
    }
}