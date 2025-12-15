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
        // 업로드 성공한 파일 경로들을 추적하기 위한 리스트 (롤백 시 삭제용)
        List<String> uploadedPaths = new ArrayList<>();

        try {
//            if (true) throw new RuntimeException("강제 롤백 테스트");
            // 1. MAIN 이미지 처리
            if (main != null && !main.isEmpty()) {
                String url = fileService.saveImage(product.getId(), main, FileService.UploadTarget.MAIN);
                uploadedPaths.add(url); // 성공 시 리스트에 추가

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
                    String url = fileService.saveImage(product.getId(), sliders.get(i), FileService.UploadTarget.SLIDER);
                    uploadedPaths.add(url); // 성공 시 리스트에 추가

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
                    String url = fileService.saveImage(product.getId(), descs.get(i), FileService.UploadTarget.DESCRIPTION);
                    uploadedPaths.add(url); // 성공 시 리스트에 추가

                    product.addImage(ProductImage.builder()
                            .imageUrl(url)
                            .imageType(ImageType.DESCRIPTION)
                            .displayOrder(i)
                            .build());
                }
            }
        } catch (IllegalArgumentException e) {
            // [수정] 파일 확장자 오류 등(400 Bad Request)은 500 에러로 변환하지 않고 그대로 던짐
            // 단, 이미 업로드된 파일이 있다면 지워야 함 (보상 트랜잭션)
            if (!uploadedPaths.isEmpty()) {
                log.warn("[ProductImageService] 잘못된 요청으로 인한 파일 삭제: {}", uploadedPaths.size());
                fileService.deleteImages(uploadedPaths);
            }
            throw e;

        } catch (Exception e) {
            // 그 외 시스템 오류(IO 등)는 500 에러로 변환 및 파일 삭제
            if (!uploadedPaths.isEmpty()) {
                log.error("[ProductImageService] 이미지 저장 중 오류 발생. 업로드된 파일 {}개를 삭제합니다.", uploadedPaths.size());
                fileService.deleteImages(uploadedPaths);
            }

            // 비즈니스 예외라면 그대로 던지고, 아니라면 INTERNAL_SERVER_ERROR로 변환
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            log.error("[ProductImageService] 시스템 오류: {}", e.getMessage(), e);
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
            fileService.deleteImages(deleteTargets);
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