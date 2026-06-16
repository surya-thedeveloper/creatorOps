package com.creatorops.config;

import com.creatorops.auth.security.UserPrincipal;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimitingInterceptor implements HandlerInterceptor {

    private final Map<Long, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${creatorops.ai.rate-limit.capacity:5}")
    private int capacity;

    @Value("${creatorops.ai.rate-limit.refill-tokens:5}")
    private int refillTokens;

    @Value("${creatorops.ai.rate-limit.refill-duration-minutes:1}")
    private int refillDurationMinutes;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long userId = principal.getId();

            Bucket bucket = cache.computeIfAbsent(userId, this::createNewBucket);
            if (!bucket.tryConsume(1)) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"AI rate limit exceeded. Please try again later.\"}");
                return false;
            }
        }
        return true;
    }

    private Bucket createNewBucket(Long userId) {
        Refill refill = Refill.intervally(refillTokens, Duration.ofMinutes(refillDurationMinutes));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public void clearCache() {
        cache.clear();
    }
}
