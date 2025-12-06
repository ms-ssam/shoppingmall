package com.example.elicesecondproject.mall.domain.product.service;

import com.example.elicesecondproject.mall.domain.product.dto.ProductImageDto;
import com.example.elicesecondproject.mall.domain.product.entity.Product;
import com.example.elicesecondproject.mall.domain.product.entity.ProductImage;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
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

        // [수정 1] 요청 이미지가 없으면 전체 Soft Delete (clear() 사용 시 물리 삭제됨)
        if (requestImages == null || requestImages.isEmpty()) {
            product.getImages().forEach(ProductImage::softDelete);
            return;
        }

        // 현재 등록된 이미지 URL 목록 추출 (비교용)
        List<String> currentUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .toList();

        Set<String> newUrls = requestImages.stream()
                .map(ProductImageDto::getImageUrl)
                .collect(Collectors.toSet());

        // [수정 2] 파일 삭제 로직 주석 처리 (선택 사항)
        // 이유: DB는 Soft Delete(기록 유지) 했는데 파일만 지우면, 나중에 DB기록을 봐도 이미지가 안 뜸.
        // 완전한 이력 관리를 위해선 파일 삭제도 보류하거나, 별도 스케줄러로 처리해야 함.
        /*
        List<String> deleteTargets = currentUrls.stream()
                .filter(url -> !newUrls.contains(url))
                .toList();

        if (!deleteTargets.isEmpty()) {
            productImageFileService.deleteImages(deleteTargets);
        }
        */

        // [수정 3] DB 리스트 동기화 (removeIf -> softDelete)
        // removeIf를 쓰면 orphanRemoval에 의해 DB에서 아예 사라짐.
        List<Long> reqIds = requestImages.stream()
                .map(ProductImageDto::getId)
                .filter(id -> id != null && id > 0)
                .toList();

        product.getImages().stream()
                .filter(img -> img.getId() != null && !reqIds.contains(img.getId()))
                .forEach(ProductImage::softDelete); // 논리 삭제 적용

        // 4. 생성 및 수정
        for (ProductImageDto imgDto : requestImages) {
            if (imgDto.getId() == null || imgDto.getId() == 0) {
                // 신규 이미지 추가
                ProductImage newImage = ProductImage.builder()
                        .imageUrl(imgDto.getImageUrl())
                        .imageType(imgDto.getImageType())
                        .displayOrder(imgDto.getDisplayOrder())
                        .build();
                product.addImage(newImage);
            } else {
                // 기존 이미지 정보 수정
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