package com.example.elicesecondproject.mall.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image")
@Getter
@Setter
public class ImageConfig {

    private Resize resize = new Resize();
    private Thumbnail thumbnail = new Thumbnail();

    @Getter
    @Setter
    public static class Resize {
        private int maxWidth;     // 1920
        private double quality;   // 0.85
    }

    @Getter
    @Setter
    public static class Thumbnail {
        private int mainSize;     // 300
        private int sliderSize;   // 100
    }
}
