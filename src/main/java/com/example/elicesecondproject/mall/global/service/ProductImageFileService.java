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
        SLIDER,         // 추가 이미지 (갤러리)
        DESCRIPTION     // 상세 설명 이미지
    }

    /**
     * 여러 파일 업로드
     *
     * @param productId 상품 ID
     * @param files 업로드할 파일 리스트
     * @param target 업로드 대상 타입
     * @return DB에 저장할 imageUrl 리스트
     */
    public List<String> saveImages(
            Long productId,
            List<MultipartFile> files,
            UploadTarget target
    ) throws IOException {

        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = saveImage(productId, file, target);
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
     * @param file 업로드할 파일
     * @param target 업로드 대상 타입
     * @return DB에 저장할 imageUrl (예: /uploads/products/123/main/thumbnail/abc.jpg)
     */
    public String saveImage(
            Long productId,
            MultipartFile file,
            UploadTarget target
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. 확장자 검증
        validateExtension(file);

        // 2. 대상 디렉토리 결정
        Path baseDir = resolveBaseDir(productId, target);
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

        // 6. 리사이즈 (1200px 최대, 비율 유지)
        Thumbnails.of(originalPath.toFile())
                .size(imageConfig.getResize().getMaxWidth(), imageConfig.getResize().getMaxWidth())
                .outputQuality(imageConfig.getResize().getQuality())
                .toFile(resizedPath.toFile());
        log.info("[ProductImageFileService] Resized saved: {}", resizedPath);

        // 7. 썸네일 생성 (MAIN, SLIDER만)
        if (target == UploadTarget.MAIN || target == UploadTarget.SLIDER) {
            int size = getThumbnailSize(target);
            Path thumbnailPath = thumbnailDir.resolve(filename);

            Thumbnails.of(resizedPath.toFile())
                    .size(size, size)
                    .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                    .toFile(thumbnailPath.toFile());
            log.info("[ProductImageFileService] Thumbnail saved: {}", thumbnailPath);
        }

        // 8. DB에 저장할 웹 경로 생성
        String imageUrl = buildWebPath(productId, target, filename);
        log.info("[ProductImageFileService] Image URL: {}", imageUrl);

        return imageUrl;
    }

    /**
     * 이미지 파일 삭제
     *
     * @param imageUrl DB에 저장된 imageUrl (예: /uploads/products/123/main/thumbnail/abc.jpg)
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String prefix = "/uploads/";
            if (!imageUrl.startsWith(prefix)) {
                log.warn("[ProductImageFileService] 관리 대상이 아닌 경로: {}", imageUrl);
                return;
            }

            // DB URL이 thumbnail 또는 resized 경로
            String relative = imageUrl.substring(prefix.length());
            Path basePath = Paths.get(fileConfig.getBasePath());
            Path dbImagePath = basePath.resolve(relative.replace("/", FileSystems.getDefault().getSeparator()));

            // 파일명과 상위 디렉토리 추출
            String filename = dbImagePath.getFileName().toString();
            Path parentDir = dbImagePath.getParent().getParent(); // .../main or .../slider

            // 3개 파일 모두 삭제
            Path originalPath = parentDir.resolve("original").resolve(filename);
            Path resizedPath = parentDir.resolve("resized").resolve(filename);
            Path thumbnailPath = parentDir.resolve("thumbnail").resolve(filename);

            if (Files.exists(originalPath)) {
                Files.delete(originalPath);
                log.info("[ProductImageFileService] Original 삭제: {}", originalPath);
            }

            if (Files.exists(resizedPath)) {
                Files.delete(resizedPath);
                log.info("[ProductImageFileService] Resized 삭제: {}", resizedPath);
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
    private Path resolveBaseDir(Long productId, UploadTarget target) {
        return switch (target) {
            case MAIN -> fileConfig.getMainDir(productId);
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
        // SLIDER: 100x100, MAIN: 300x300
        if (target == UploadTarget.SLIDER) {
            return imageConfig.getThumbnail().getSliderSize();
        }
        return imageConfig.getThumbnail().getMainSize();
    }

    /**
     * DB에 저장할 웹 경로 생성
     *
     * @param productId 상품 ID
     * @param target 업로드 타입
     * @param filename 파일명
     * @return 웹 경로 (예: /uploads/products/123/main/thumbnail/abc.jpg)
     */
    private String buildWebPath(Long productId, UploadTarget target, String filename) {
        String webBase = fileConfig.getWebBasePath(productId);

        String typePath = switch (target) {
            case MAIN -> "main";
            case SLIDER -> "slider";
            case DESCRIPTION -> "description";
        };

        // thumbnail 경로 저장 (DESCRIPTION만 resized)
        String directory = (target == UploadTarget.DESCRIPTION) ? "resized" : "thumbnail";

        return webBase + typePath + "/" + directory + "/" + filename;
    }
}
