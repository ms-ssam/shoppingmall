package com.example.elicesecondproject.mall.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
@Getter
@Setter
public class FileConfig {

    // application.yml에서 주입
    private String basePath;             // "C:/uploads" (local) or "/app/uploads" (prod)
    private String productPath;          // "products"
    private String allowedExtensions;    // "jpg,jpeg,png,webp"

    /**
     * 상품 베이스 디렉터리
     * 예: C:/uploads/products/123
     */
    public Path getProductBaseDir(Long productId) {
        return Paths.get(basePath, productPath, String.valueOf(productId));
    }

    /**
     * MAIN 이미지 디렉터리
     * 예: C:/uploads/products/123/main
     */
    public Path getMainDir(Long productId) {
        return getProductBaseDir(productId).resolve("main");
    }

    /**
     * SLIDER 이미지 디렉터리
     * 예: C:/uploads/products/123/slider
     */
    public Path getSliderDir(Long productId) {
        return getProductBaseDir(productId).resolve("slider");
    }

    /**
     * DESCRIPTION 이미지 디렉터리
     * 예: C:/uploads/products/123/description
     */
    public Path getDescriptionDir(Long productId) {
        return getProductBaseDir(productId).resolve("description");
    }

    /**
     * original 하위 디렉터리
     * 예: .../main/original
     */
    public Path getOriginalDir(Path base) {
        return base.resolve("original");
    }

    /**
     * resized 하위 디렉터리
     * 예: .../main/resized
     */
    public Path getResizedDir(Path base) {
        return base.resolve("resized");
    }

    /**
     * thumbnail 하위 디렉터리
     * 예: .../main/thumbnail
     */
    public Path getThumbnailDir(Path base) {
        return base.resolve("thumbnail");
    }

    /**
     * DB에 저장되는 웹 경로 prefix
     * 예: /uploads/products/123/
     */
    public String getWebBasePath(Long productId) {
        return "/uploads/" + productPath + "/" + productId + "/";
    }

    /**
     * 허용된 파일 확장자 배열
     * 예: ["jpg", "jpeg", "png", "webp"]
     */
    public String[] getAllowedExtensionArray() {
        return allowedExtensions.split(",");
    }

    /**
     * basePath getter (업로드 루트 경로)
     * 예: C:/uploads
     */
    public String getBasePath() {
        return basePath;
    }
}
