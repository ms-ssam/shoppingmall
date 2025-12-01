package com.example.elicesecondproject.mall.global.service;


import com.example.elicesecondproject.mall.global.config.FileConfig;
import com.example.elicesecondproject.mall.global.config.ImageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageFileService {

    private final FileConfig fileConfig;
    private final ImageConfig imageConfig;

    /**
     * 이미지 업로드 대상 타입
     */
    public enum UploadTarget {
        MAIN,           // 상품 대표 이미지
        COLOR,          // 색상별 대표 이미지
        SLIDER,         // 추가 이미지 (갤러리)
        DESCRIPTION     // 상세 설명 이미지
    }

    /**
     * 여러 파일 업로드
     *
     * @param productId 상품 ID
     * @param colorOptionGroupId 색상 옵션 그룹 ID (COLOR 타입만 사용, 나머지는 null)
     * @param files 업로드할 파일 리스트
     * @param target 업로드 대상 타입
     * @return DB에 저장할 imageUrl 리스트
     */
    public List<String> saveImages(
            Long productId,
            Long colorOptionGroupId,
            List<MultipartFile> files,
            UploadTarget target
    ) throws IOException {

        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = saveImage(productId, colorOptionGroupId, file, target);
            if (imageUrl != null) {
                imageUrls.add(imageUrl);
            }
        }
        return imageUrls;
    }

    /**
     * 단일 파일 업로드
     *
     * @param productId 상품 ID
     * @param colorOptionGroupId 색상 옵션 그룹 ID (COLOR 타입만 사용)
     * @param file 업로드할 파일
     * @param target 업로드 대상 타입
     * @return DB에 저장할 imageUrl (예: /uploads/products/123/main/resized/abc.jpg)
     */
    public String saveImage(
            Long productId,
            Long colorOptionGroupId,
            MultipartFile file,
            UploadTarget target
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. 확장자 검증
        validateExtension(file);

        // 2. 대상 디렉토리 결정
        Path baseDir = resolveBaseDir(productId, colorOptionGroupId, target);
        Path originalDir = fileConfig.getOriginalDir(baseDir);
        Path resizedDir = fileConfig.getResizedDir(baseDir);
        Path thumbnailDir = fileConfig.getThumbnailDir(baseDir);

        // 3. 디렉토리 생성
        createDirectoriesIfNotExists(originalDir, resizedDir, thumbnailDir, target);

        // 4. 파일명 생성 (UUID + 원본 확장자)
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + "." + ext;

        Path originalPath = originalDir.resolve(filename);
        Path resizedPath = resizedDir.resolve(filename);

        // 5. 원본 파일 저장
        Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("[ProductImageFileService] Original saved: {}", originalPath);

        // 6. 리사이즈 (1920px 최대, 비율 유지)
        Thumbnails.of(originalPath.toFile())
                .size(imageConfig.getResize().getMaxWidth(), imageConfig.getResize().getMaxWidth())
                .outputQuality(imageConfig.getResize().getQuality())
                .toFile(resizedPath.toFile());
        log.info("[ProductImageFileService] Resized saved: {}", resizedPath);

        // 7. 썸네일 생성 (MAIN, COLOR, SLIDER만)
        if (target == UploadTarget.MAIN || target == UploadTarget.COLOR || target == UploadTarget.SLIDER) {
            int size = getThumbnailSize(target);
            Path thumbnailPath = thumbnailDir.resolve(filename);

            Thumbnails.of(resizedPath.toFile())
                    .size(size, size)
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                    .toFile(thumbnailPath.toFile());
            log.info("[ProductImageFileService] Thumbnail saved: {}", thumbnailPath);
        }

        // 8. DB에 저장할 웹 경로 생성
        String imageUrl = buildWebPath(productId, colorOptionGroupId, target, filename);
        log.info("[ProductImageFileService] Image URL: {}", imageUrl);

        return imageUrl;
    }

    /**
     * 이미지 파일 삭제
     *
     * @param imageUrl DB에 저장된 imageUrl (예: /uploads/products/123/main/resized/abc.jpg)
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            // /uploads/ 이후 경로 추출
            String prefix = "/uploads/";
            if (!imageUrl.startsWith(prefix)) {
                log.warn("[ProductImageFileService] 관리 대상이 아닌 경로: {}", imageUrl);
                return;
            }

            // 웹 경로 → 파일 시스템 경로 변환
            String relative = imageUrl.substring(prefix.length()); // products/123/main/resized/abc.jpg
            Path basePath = Paths.get(fileConfig.getBasePath());
            Path resizedPath = basePath.resolve(relative.replace("/", FileSystems.getDefault().getSeparator()));

            // resized 파일 삭제
            if (Files.exists(resizedPath)) {
                Files.delete(resizedPath);
                log.info("[ProductImageFileService] Resized 삭제: {}", resizedPath);
            }

            // original, thumbnail 경로 계산 및 삭제
            Path parentDir = resizedPath.getParent(); // .../resized
            String filename = resizedPath.getFileName().toString();

            Path originalPath = parentDir.getParent().resolve("original").resolve(filename);
            Path thumbnailPath = parentDir.getParent().resolve("thumbnail").resolve(filename);

            if (Files.exists(originalPath)) {
                Files.delete(originalPath);
                log.info("[ProductImageFileService] Original 삭제: {}", originalPath);
            }
            if (Files.exists(thumbnailPath)) {
                Files.delete(thumbnailPath);
                log.info("[ProductImageFileService] Thumbnail 삭제: {}", thumbnailPath);
            }

        } catch (Exception e) {
            log.error("[ProductImageFileService] 이미지 삭제 실패: {}", imageUrl, e);
        }
    }

    /**
     * 여러 이미지 파일 삭제
     *
     * @param imageUrls DB에 저장된 imageUrl 리스트
     */
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String imageUrl : imageUrls) {
            deleteImage(imageUrl);
        }
    }

    // ==================== 내부 유틸리티 메서드 ====================

    /**
     * 파일 확장자 검증
     */
    private void validateExtension(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null) {
            throw new IllegalArgumentException("확장자를 찾을 수 없습니다.");
        }

        String lower = ext.toLowerCase();
        boolean allowed = Arrays.stream(fileConfig.getAllowedExtensionArray())
                .map(String::toLowerCase)
                .anyMatch(e -> e.equals(lower));

        if (!allowed) {
            throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + ext);
        }
    }

    /**
     * 업로드 타입에 따른 베이스 디렉토리 반환
     */
    private Path resolveBaseDir(Long productId, Long colorOptionGroupId, UploadTarget target) {
        return switch (target) {
            case MAIN -> fileConfig.getMainDir(productId);
            case COLOR -> fileConfig.getColorDir(productId, colorOptionGroupId);
            case SLIDER -> fileConfig.getSliderDir(productId);
            case DESCRIPTION -> fileConfig.getDescriptionDir(productId);
        };
    }

    /**
     * 디렉토리 생성 (없으면)
     */
    private void createDirectoriesIfNotExists(Path originalDir, Path resizedDir, Path thumbnailDir, UploadTarget target) throws IOException {
        if (!Files.exists(originalDir)) {
            Files.createDirectories(originalDir);
            log.info("[ProductImageFileService] 디렉토리 생성: {}", originalDir);
        }
        if (!Files.exists(resizedDir)) {
            Files.createDirectories(resizedDir);
            log.info("[ProductImageFileService] 디렉토리 생성: {}", resizedDir);
        }

        // DESCRIPTION은 썸네일 생성 안 함
        if (target != UploadTarget.DESCRIPTION && !Files.exists(thumbnailDir)) {
            Files.createDirectories(thumbnailDir);
            log.info("[ProductImageFileService] 디렉토리 생성: {}", thumbnailDir);
        }
    }

    /**
     * 썸네일 크기 반환
     */
    private int getThumbnailSize(UploadTarget target) {
        // SLIDER만 100x100, 나머지(MAIN, COLOR)는 300x300
        if (target == UploadTarget.SLIDER) {
            return imageConfig.getThumbnail().getSliderSize();
        }
        return imageConfig.getThumbnail().getMainSize();
    }

    /**
     * DB에 저장할 웹 경로 생성
     * 예: /uploads/products/123/main/resized/abc.jpg
     */
    private String buildWebPath(Long productId, Long colorOptionGroupId, UploadTarget target, String filename) {
        String webBase = fileConfig.getWebBasePath(productId); // /uploads/products/123/
        String typePath = switch (target) {
            case MAIN -> "main";
            case COLOR -> "color-" + colorOptionGroupId;
            case SLIDER -> "slider";
            case DESCRIPTION -> "description";
        };

        return webBase + typePath + "/resized/" + filename;
    }
}
