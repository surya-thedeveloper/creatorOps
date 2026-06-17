package com.creatorops.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate
                .minimumNumberOfCalls(5) // evaluate after 5 calls
                .slidingWindowSize(10)
                .waitDurationInOpenState(Duration.ofSeconds(10)) // 10s wait in open state
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // 3 attempts total
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(500, 2.0)) // exponential backoff
                .build();
        return RetryRegistry.of(config);
    }
}
