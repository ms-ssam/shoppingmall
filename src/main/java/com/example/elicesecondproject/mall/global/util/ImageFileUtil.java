package com.example.elicesecondproject.mall.global.util;

import com.example.elicesecondproject.mall.global.config.FileConfig;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;

@UtilityClass
public class ImageFileUtil {

    public void validateExtension(MultipartFile file, FileConfig fileConfig) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null) {
            throw new IllegalArgumentException("확장자를 찾을 수 없습니다.");
        }

        String lower = ext.toLowerCase();
        boolean allowed = java.util.Arrays.stream(fileConfig.getAllowedExtensionArray())
                .map(String::toLowerCase)
                .anyMatch(e -> e.equals(lower));

        if (!allowed) {
            throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + ext);
        }
    }

    public void deleteByWebPath(String imageUrl, FileConfig fileConfig) throws Exception {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String prefix = "/uploads/";
        if (!imageUrl.startsWith(prefix)) return;

        String relative = imageUrl.substring(prefix.length());
        Path basePath = Paths.get(fileConfig.getBasePath());
        Path filePath = basePath.resolve(relative.replace("/", FileSystems.getDefault().getSeparator()));

        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}

