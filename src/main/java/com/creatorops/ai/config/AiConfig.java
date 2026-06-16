package com.creatorops.ai.config;

import com.creatorops.ai.provider.AIProvider;
import com.creatorops.ai.provider.GeminiAIProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h3>AiConfig</h3>
 * Registers properties mapping and dynamically sets up the AI provider bean based on the environment configurations.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "creatorops.ai.provider", havingValue = "gemini", matchIfMissing = true)
    public AIProvider geminiAIProvider(AiProperties properties) {
        return new GeminiAIProvider(properties);
    }
}
