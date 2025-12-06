package com.example.elicesecondproject.mall.global.service;

import com.example.elicesecondproject.mall.global.config.FileConfig;
import com.example.elicesecondproject.mall.global.exception.BusinessException;
import com.example.elicesecondproject.mall.global.exception.ErrorCode;
import com.example.elicesecondproject.mall.global.util.ImageFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalImageFileService {

    private final FileConfig fileConfig;
    private final ImageFileUtil imageFileUtil;

    /**
     * 공통 이미지 저장 메서드
     *
     * @param productId 상품 ID (리뷰/문의 모두 상품 기준으로 묶는다고 가정)
     * @param file 업로드 파일
     * @param category REVIEW / INQUIRY
     * @return DB에 저장할 imageUrl (예: /uploads/reviews/1/uuid.jpg)
     */
    public String saveImage(Long productId,
                            MultipartFile file,
                            ImageCategory category) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1) 확장자 검증
        imageFileUtil.validateExtension(file, fileConfig);

        // 2) 저장 디렉토리 결정
        Path baseDir = resolveBaseDir(productId, category);

        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
            log.info("[GlobalImageFileService] 디렉토리 생성: {}", baseDir);
        }

        // 3) 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);

        String randomName = UUID.randomUUID().toString();
        String fileName = (ext != null && !ext.isBlank())
                ? randomName + "." + ext
                : randomName;

        // 4) 실제 저장 경로
        Path savePath = baseDir.resolve(fileName);
        file.transferTo(savePath.toFile());
        log.info("[GlobalImageFileService] 파일 저장: {}", savePath);

        // 5) DB에 저장할 웹 경로 생성
        String imageUrl = buildWebPath(productId, category, fileName);
        log.info("[GlobalImageFileService] Image URL: {}", imageUrl);

        return imageUrl;
    }

    /**
     * 리뷰 이미지 전용 저장
     */
    public String saveReviewImage(Long productId, MultipartFile file) {
        try {
            return saveImage(productId, file, ImageCategory.REVIEW);
        } catch (IOException e) {
            log.error("[GlobalImageFileService] 리뷰 이미지 업로드 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 문의 이미지 전용 저장
     */
    public String saveInquiryImage(Long productId, MultipartFile file) {
        try {
            return saveImage(productId, file, ImageCategory.INQUIRY);
        } catch (IOException e) {
            log.error("[GlobalImageFileService] 문의 이미지 업로드 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 이미지 삭제
     *
     * @param imageUrl DB에 저장된 imageUrl (예: /uploads/reviews/1/uuid.jpg)
     */
    public void deleteImage(String imageUrl) {

        try {
            imageFileUtil.deleteByWebPath(imageUrl, fileConfig);

        } catch (Exception e) {
            log.error("[GlobalImageFileService] 이미지 삭제 실패: {}", imageUrl, e);
        }
    }

    /**
     * 여러 이미지 삭제
     */
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        for (String url : imageUrls) {
            deleteImage(url);
        }
    }

    // ==================== 내부 유틸 메서드 ====================

    /**
     * 리뷰/문의 베이스 디렉토리 선택
     */
    private Path resolveBaseDir(Long productId, ImageCategory category) {
        return switch (category) {
            case REVIEW -> fileConfig.getReviewBaseDir(productId);
            case INQUIRY -> fileConfig.getInquiryBaseDir(productId);
        };
    }

    /**
     * 웹 경로 생성
     * REVIEW  → /uploads/reviews/{productId}/{filename}
     * INQUIRY → /uploads/inquiries/{productId}/{filename}
     */
    private String buildWebPath(Long productId, ImageCategory category, String filename) {
        String base = switch (category) {
            case REVIEW -> fileConfig.getReviewWebBasePath(productId);
            case INQUIRY -> fileConfig.getInquiryWebBasePath(productId);
        };
        return base + filename;
    }
}
