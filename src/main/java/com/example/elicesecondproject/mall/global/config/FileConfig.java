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
    private String basePath;            // "/app/uploads"
    private String productPath;         // "products"
    private String allowedExtensions;   // "jpg,jpeg,png,webp"

    /** 상품 베이스 디렉터리 */
    public Path getProductBaseDir(Long productId) {
        return Paths.get(basePath, productPath, String.valueOf(productId));
    }

    // MAIN, SLIDER, DESCRIPTION, 색상별 대표 - 하위 폴더 반환
    public Path getMainDir(Long productId) {
        return getProductBaseDir(productId).resolve("main");
    }

    public Path getSliderDir(Long productId) {
        return getProductBaseDir(productId).resolve("slider");
    }

    public Path getDescriptionDir(Long productId) {
        return getProductBaseDir(productId).resolve("description");
    }

    /** color-main 이미지: color-옵션아이디 */
    public Path getColorDir(Long productId, Long colorId) {
        return getProductBaseDir(productId).resolve("color-" + colorId);
    }

    /** original, resized, thumbnail 하위 디렉터리 */
    public Path getOriginalDir(Path base) {
        return base.resolve("original");
    }
    public Path getResizedDir(Path base) {
        return base.resolve("resized");
    }
    public Path getThumbnailDir(Path base) {
        return base.resolve("thumbnail");
    }

    /** DB에 저장되는 웹 경로 prefix (예: /uploads/products/123/…) */
    public String getWebBasePath(Long productId) {
        return "/uploads/" + productPath + "/" + productId + "/";
    }
    public String[] getAllowedExtensionArray() {
        return allowedExtensions.split(",");
    }
}
