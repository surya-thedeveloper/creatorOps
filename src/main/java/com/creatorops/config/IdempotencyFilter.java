package com.creatorops.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final Map<String, IdempotencyRecord> cache = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        // Match POST /api/v1/ai/contents/{contentId}/brainstorm and POST /api/v1/ai/contents/{contentId}/generate-script
        boolean isAiPost = method.equalsIgnoreCase("POST") && 
                           (path.matches("/api/v1/ai/contents/\\d+/brainstorm") || 
                            path.matches("/api/v1/ai/contents/\\d+/generate-script"));
        return !isAiPost;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getHeader(IDEMPOTENCY_HEADER);
        if (key == null || key.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        IdempotencyRecord record = cache.get(key);
        if (record != null) {
            if (record.getState() == IdempotencyRecord.State.IN_PROGRESS) {
                log.warn("Duplicate request detected for key {} (IN_PROGRESS). Returning 409 Conflict.", key);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"A request with the same idempotency key is already in progress.\"}");
                return;
            } else {
                log.info("Duplicate request detected for key {} (COMPLETED). Returning cached response.", key);
                response.setStatus(record.getStatus());
                if (record.getContentType() != null) {
                    response.setContentType(record.getContentType());
                }
                response.getOutputStream().write(record.getBody());
                response.getOutputStream().flush();
                return;
            }
        }

        // Register the key as IN_PROGRESS
        IdempotencyRecord inProgressRecord = new IdempotencyRecord(
                IdempotencyRecord.State.IN_PROGRESS,
                0,
                null,
                null,
                System.currentTimeMillis()
        );
        IdempotencyRecord existing = cache.putIfAbsent(key, inProgressRecord);
        if (existing != null) {
            // Race condition: another thread inserted it
            if (existing.getState() == IdempotencyRecord.State.IN_PROGRESS) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"A request with the same idempotency key is already in progress.\"}");
                return;
            } else {
                response.setStatus(existing.getStatus());
                if (existing.getContentType() != null) {
                    response.setContentType(existing.getContentType());
                }
                response.getOutputStream().write(existing.getBody());
                response.getOutputStream().flush();
                return;
            }
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrappedResponse);
            int status = wrappedResponse.getStatus();
            // Cache successful responses (2xx)
            if (status >= 200 && status < 300) {
                byte[] body = wrappedResponse.getContentAsByteArray();
                String contentType = wrappedResponse.getContentType();
                IdempotencyRecord completedRecord = new IdempotencyRecord(
                        IdempotencyRecord.State.COMPLETED,
                        status,
                        contentType,
                        body,
                        System.currentTimeMillis()
                );
                cache.put(key, completedRecord);
            } else {
                // If call failed, remove entry from cache to allow retrying
                cache.remove(key);
            }
            wrappedResponse.copyBodyToResponse();
        } catch (Exception e) {
            cache.remove(key);
            throw e;
        }
    }

    /**
     * Periodically clean records older than 24 hours (86,400,000 milliseconds) to prevent memory growth.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanExpiredRecords() {
        long now = System.currentTimeMillis();
        int beforeSize = cache.size();
        cache.entrySet().removeIf(entry -> (now - entry.getValue().getCreatedAt()) > 86400000);
        int afterSize = cache.size();
        if (beforeSize != afterSize) {
            log.info("Idempotency cache cleanup run. Evicted {} expired records.", (beforeSize - afterSize));
        }
    }

    public static class IdempotencyRecord {
        public enum State { IN_PROGRESS, COMPLETED }

        private final State state;
        private final int status;
        private final String contentType;
        private final byte[] body;
        private final long createdAt;

        public IdempotencyRecord(State state, int status, String contentType, byte[] body, long createdAt) {
            this.state = state;
            this.status = status;
            this.contentType = contentType;
            this.body = body;
            this.createdAt = createdAt;
        }

        public State getState() { return state; }
        public int getStatus() { return status; }
        public String getContentType() { return contentType; }
        public byte[] getBody() { return body; }
        public long getCreatedAt() { return createdAt; }
    }
}
