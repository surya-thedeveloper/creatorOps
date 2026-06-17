package com.creatorops.ai.config;

import com.creatorops.ai.provider.AIProvider;
import com.creatorops.ai.provider.GeminiAIProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.creatorops.common.metrics.MetricsService;

/**
 * <h3>AiConfig</h3>
 * Registers properties mapping and dynamically sets up the AI provider bean based on the environment configurations.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "creatorops.ai.provider", havingValue = "gemini", matchIfMissing = true)
    public AIProvider geminiAIProvider(AiProperties properties,
                                        CircuitBreakerRegistry circuitBreakerRegistry,
                                        RetryRegistry retryRegistry,
                                        MetricsService metricsService) {
        return new GeminiAIProvider(properties, circuitBreakerRegistry, retryRegistry, metricsService);
    }
}
