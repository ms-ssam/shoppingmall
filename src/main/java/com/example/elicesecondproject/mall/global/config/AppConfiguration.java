package com.example.elicesecondproject.mall.global.config;

import lombok.RequiredArgsConstructor;
import com.example.elicesecondproject.mall.global.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AppConfiguration implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // loggingInterceptor 적용
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**");
    }
}