package com.example.elicesecondproject.mall.global.service;

import com.example.elicesecondproject.mall.global.config.FileConfig;
import com.example.elicesecondproject.mall.global.config.ImageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileConfig fileConfig;
    private final ImageConfig imageConfig;

    public enum UploadTarget {
        MAIN,           // 상품 대표 이미지 (목록용 썸네일 생성 O)
        SLIDER,         // 슬라이더 (원본 사용)
        DESCRIPTION     // 상세 설명 (원본 사용)
    }

    public List<String> saveImages(Long productId, List<MultipartFile> files, UploadTarget target) throws IOException {
        if (files == null || files.isEmpty()) return Collections.emptyList();
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String imageUrl = saveImage(productId, file, target);
            if (imageUrl != null) imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    public String saveImage(Long productId, MultipartFile file, UploadTarget target) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 1. 확장자 검증
        validateExtension(file);

        // 2. 디렉토리 결정 (Resized 제거됨)
        Path baseDir = resolveBaseDir(productId, target);
        Path originalDir = fileConfig.getOriginalDir(baseDir);
        Path thumbnailDir = fileConfig.getThumbnailDir(baseDir); // MAIN 전용

        // 3. 디렉토리 생성
        if (!Files.exists(originalDir)) {
            Files.createDirectories(originalDir);
        }

        // MAIN인 경우에만 썸네일 폴더 생성
        if (target == UploadTarget.MAIN && !Files.exists(thumbnailDir)) {
            Files.createDirectories(thumbnailDir);
        }

        // 4. 파일명 생성 (UUID)
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + "." + ext;

        Path originalPath = originalDir.resolve(filename);
        Path thumbnailPath = thumbnailDir.resolve(filename);

        // 5. [Original] 원본 저장 (모든 타입 공통)
        Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("[ProductImageFileService] Original saved: {}", originalPath);

        // 6. [Thumbnail] 썸네일 생성 (MAIN 타입만!)
        if (target == UploadTarget.MAIN) {
            try {
                int size = imageConfig.getThumbnail().getMainSize();
                if (size <= 0) size = 300;

                // 원본 -> 썸네일 변환
                Thumbnails.of(originalPath.toFile())
                        .size(size, size)
                        .crop(Positions.CENTER)
                        .toFile(thumbnailPath.toFile());

                log.info("[ProductImageFileService] Thumbnail saved: {}", thumbnailPath);

            } catch (Exception e) {
                // [테스트 대응] 가짜 데이터 등으로 실패 시 -> 원본 복사
                log.warn("[ProductImageFileService] 썸네일 생성 실패(단순복사): {}", e.getMessage());
                Files.copy(originalPath, thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // 7. 웹 경로 반환
        return buildWebPath(productId, target, filename);
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String prefix = "/uploads/";
            if (!imageUrl.startsWith(prefix)) return;

            String relative = imageUrl.substring(prefix.length());
            Path basePath = Paths.get(fileConfig.getBasePath());

            // OS 호환 경로 변환
            Path dbImagePath = basePath.resolve(relative.replace("/", FileSystems.getDefault().getSeparator()));

            String filename = dbImagePath.getFileName().toString();
            Path parentDir = dbImagePath.getParent().getParent(); // .../main

            // 삭제 대상: Original, Thumbnail (Resized 삭제됨)
            Path originalPath = parentDir.resolve("original").resolve(filename);
            Path thumbnailPath = parentDir.resolve("thumbnail").resolve(filename);

            Files.deleteIfExists(originalPath);
            Files.deleteIfExists(thumbnailPath);

        } catch (Exception e) {
            log.error("이미지 삭제 실패: {}", imageUrl, e);
        }
    }

    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null) return;
        for (String url : imageUrls) deleteImage(url);
    }

    private void validateExtension(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || !Arrays.stream(fileConfig.getAllowedExtensionArray())
                .anyMatch(allowed -> allowed.equalsIgnoreCase(ext))) {
            throw new IllegalArgumentException("허용되지 않은 확장자: " + ext);
        }
    }

    private Path resolveBaseDir(Long productId, UploadTarget target) {
        return switch (target) {
            case MAIN -> fileConfig.getMainDir(productId);
            case SLIDER -> fileConfig.getSliderDir(productId);
            case DESCRIPTION -> fileConfig.getDescriptionDir(productId);
        };
    }

    private String buildWebPath(Long productId, UploadTarget target, String filename) {
        String webBase = fileConfig.getWebBasePath(productId);
        String typePath = switch (target) {
            case MAIN -> "main";
            case SLIDER -> "slider";
            case DESCRIPTION -> "description";
        };

        // MAIN만 thumbnail 폴더, 나머지는 original 폴더 사용
        String directory = (target == UploadTarget.MAIN) ? "thumbnail" : "original";

        return webBase + typePath + "/" + directory + "/" + filename;
    }
}