package com.example.elicesecondproject.mall.global.service;

import com.example.elicesecondproject.mall.global.config.FileConfig;
import com.example.elicesecondproject.mall.global.config.ImageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
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
public class ProductImageFileService {

    private final FileConfig fileConfig;
    private final ImageConfig imageConfig;

    /**
     * 이미지 업로드 대상 타입
     */
    public enum UploadTarget {
        MAIN,           // 상품 대표 이미지
        SLIDER,         // 추가 이미지 (갤러리)
        DESCRIPTION,    // 상세 설명 이미지
        REVIEW,         // 리뷰 이미지
        PROFILE         // 프로필 이미지
    }

    /**
     * 여러 파일 업로드
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

        // 6. 리사이즈 (안전하게 처리)
        try {
            BufferedImage originalImage = ImageIO.read(originalPath.toFile());
            if (originalImage == null) {
                throw new IOException("이미지 파일을 읽을 수 없습니다: " + filename);
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // 기본값 처리
            int maxWidth = imageConfig.getResize().getMaxWidth();
            if (maxWidth <= 0) {
                maxWidth = 1200;
                log.warn("[ProductImageFileService] maxWidth가 설정되지 않아 기본값 1200 사용");
            }

            double quality = imageConfig.getResize().getQuality();
            if (quality <= 0 || quality > 1) {
                quality = 0.85;
                log.warn("[ProductImageFileService] quality가 잘못 설정되어 기본값 0.85 사용");
            }

            log.info("[ProductImageFileService] 원본 이미지 크기: {}x{}, 목표: {}",
                    originalWidth, originalHeight, maxWidth);

            // 원본이 목표 크기보다 작으면 그대로 복사
            if (originalWidth <= maxWidth && originalHeight <= maxWidth) {
                Files.copy(originalPath, resizedPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("[ProductImageFileService] 원본 크기가 작아 그대로 복사");
            } else {
                Thumbnails.of(originalPath.toFile())
                        .size(maxWidth, maxWidth)
                        .outputQuality(quality)
                        .toFile(resizedPath.toFile());
                log.info("[ProductImageFileService] Resized saved: {}", resizedPath);
            }

        } catch (Exception e) {
            log.error("[ProductImageFileService] 리사이징 실패, 원본 복사: {}", e.getMessage());
            Files.copy(originalPath, resizedPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 7. 썸네일 생성 (DESCRIPTION 제외)
        if (target != UploadTarget.DESCRIPTION) {
            try {
                int size = getThumbnailSize(target);

                // 기본값 처리
                if (size <= 0) {
                    size = switch (target) {
                        case MAIN -> 300;
                        case SLIDER -> 100;
                        case REVIEW -> 100;
                        case PROFILE -> 200;
                        default -> 300;
                    };
                    log.warn("[ProductImageFileService] 썸네일 크기가 설정되지 않아 기본값 사용: {}", size);
                }

                Path thumbnailPath = thumbnailDir.resolve(filename);

                BufferedImage resizedImage = ImageIO.read(resizedPath.toFile());
                if (resizedImage != null) {
                    int resizedWidth = resizedImage.getWidth();
                    int resizedHeight = resizedImage.getHeight();

                    log.info("[ProductImageFileService] 썸네일 생성: {}x{} -> {}x{}",
                            resizedWidth, resizedHeight, size, size);

                    // 리사이즈된 이미지가 썸네일보다 작으면 그대로 복사
                    if (resizedWidth <= size && resizedHeight <= size) {
                        Files.copy(resizedPath, thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
                        log.info("[ProductImageFileService] 이미지가 작아 썸네일 생성 생략");
                    } else {
                        Thumbnails.of(resizedPath.toFile())
                                .size(size, size)
                                .crop(net.coobird.thumbnailator.geometry.Positions.CENTER)
                                .toFile(thumbnailPath.toFile());
                        log.info("[ProductImageFileService] Thumbnail saved: {}", thumbnailPath);
                    }
                }
            } catch (Exception e) {
                log.error("[ProductImageFileService] 썸네일 생성 실패, 리사이즈 이미지 복사: {}", e.getMessage(), e);
                Path thumbnailPath = thumbnailDir.resolve(filename);
                Files.copy(resizedPath, thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // 8. DB에 저장할 웹 경로 생성
        String imageUrl = buildWebPath(productId, target, filename);
        log.info("[ProductImageFileService] Image URL: {}", imageUrl);

        return imageUrl;
    }

    /**
     * 이미지 파일 삭제
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

            String relative = imageUrl.substring(prefix.length());
            Path basePath = Paths.get(fileConfig.getBasePath());
            Path dbImagePath = basePath.resolve(relative.replace("/", FileSystems.getDefault().getSeparator()));

            String filename = dbImagePath.getFileName().toString();
            Path parentDir = dbImagePath.getParent().getParent();

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

    private Path resolveBaseDir(Long productId, UploadTarget target) {
        return switch (target) {
            case MAIN -> fileConfig.getMainDir(productId);
            case SLIDER -> fileConfig.getSliderDir(productId);
            case DESCRIPTION -> fileConfig.getDescriptionDir(productId);
            case REVIEW -> fileConfig.getReviewDir(productId);
            case PROFILE -> fileConfig.getProfileDir(productId);
        };
    }

    private void createDirectoriesIfNotExists(Path originalDir, Path resizedDir, Path thumbnailDir, UploadTarget target) throws IOException {
        if (!Files.exists(originalDir)) {
            Files.createDirectories(originalDir);
            log.info("[ProductImageFileService] 디렉토리 생성: {}", originalDir);
        }
        if (!Files.exists(resizedDir)) {
            Files.createDirectories(resizedDir);
            log.info("[ProductImageFileService] 디렉토리 생성: {}", resizedDir);
        }

        if (target != UploadTarget.DESCRIPTION && !Files.exists(thumbnailDir)) {
            Files.createDirectories(thumbnailDir);
            log.info("[ProductImageFileService] 디렉토리 생성: {}", thumbnailDir);
        }
    }

    private int getThumbnailSize(UploadTarget target) {
        int size = switch (target) {
            case MAIN -> imageConfig.getThumbnail().getMainSize();
            case SLIDER -> imageConfig.getThumbnail().getSliderSize();
            case REVIEW -> 100;
            case PROFILE -> 200;
            default -> 0;
        };

        if (size <= 0) {
            return switch (target) {
                case MAIN -> 300;
                case SLIDER -> 100;
                case REVIEW -> 100;
                case PROFILE -> 200;
                default -> 0;
            };
        }

        return size;
    }

    private String buildWebPath(Long productId, UploadTarget target, String filename) {
        String webBase = switch (target) {
            case MAIN, SLIDER, DESCRIPTION -> fileConfig.getWebBasePath(productId);
            case REVIEW -> fileConfig.getWebBasePathForReview(productId);
            case PROFILE -> fileConfig.getWebBasePathForProfile(productId);
        };

        String typePath = switch (target) {
            case MAIN -> "main";
            case SLIDER -> "slider";
            case DESCRIPTION -> "description";
            case REVIEW -> "review";
            case PROFILE -> "profile";
        };

        String directory = (target == UploadTarget.DESCRIPTION) ? "resized" : "thumbnail";

        return webBase + typePath + "/" + directory + "/" + filename;
    }
}
