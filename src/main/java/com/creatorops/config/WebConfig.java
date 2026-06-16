package com.creatorops.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AiRateLimitingInterceptor aiRateLimitingInterceptor;

    @Autowired
    public WebConfig(AiRateLimitingInterceptor aiRateLimitingInterceptor) {
        this.aiRateLimitingInterceptor = aiRateLimitingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(aiRateLimitingInterceptor)
                .addPathPatterns("/api/v1/ai/contents/*/brainstorm", "/api/v1/ai/contents/*/generate-script");
    }
}
