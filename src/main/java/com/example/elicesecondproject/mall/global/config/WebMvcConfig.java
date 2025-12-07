package com.example.elicesecondproject.mall.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileConfig productFileConfig;

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();

        // 1. 페이지 번호를 1부터 시작하게 설정 (요청: page=1 -> 서버: page=0)
        resolver.setOneIndexedParameters(true);

        // 2. 한 번에 조회 가능한 최대 개수 제한 (보안)
        resolver.setMaxPageSize(2000);

        // 3. 페이징 정보 누락 시 기본값 (1페이지, 10개)
        resolver.setFallbackPageable(PageRequest.of(0, 10));

        resolvers.add(resolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 상품 이미지 정적 리소스 매핑
        String basePath = productFileConfig.getBasePath();

        // Windows/Linux 모두 호환되도록 경로 정규화
        Path normalizedPath = Path.of(basePath).toAbsolutePath().normalize();
        /*String resourceLocation = "file:///" + normalizedPath.toString().replace("\\", "/");*/
        String resourceLocation = normalizedPath.toUri().toString();

        log.info("=======================================");
        log.info("[WebMvcConfig] Static resource mapping");
        log.info("[WebMvcConfig] URL Pattern: /uploads/**");
        log.info("[WebMvcConfig] Base Path: {}", basePath);
        log.info("[WebMvcConfig] Normalized Path: {}", normalizedPath);
        log.info("[WebMvcConfig] Resource Location: {}", resourceLocation);
        log.info("=======================================");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
