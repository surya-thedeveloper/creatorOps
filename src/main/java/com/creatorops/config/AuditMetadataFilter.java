package com.creatorops.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Run right after CorrelationIdFilter
public class AuditMetadataFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String USER_AGENT_MDC_KEY = "userAgent";
    public static final String CLIENT_IP_MDC_KEY = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.trim().isEmpty()) {
            userAgent = "unknown";
        }
        
        // Extract and mask client IP
        String rawIp = request.getHeader("X-Forwarded-For");
        if (rawIp == null || rawIp.trim().isEmpty()) {
            rawIp = request.getRemoteAddr();
        }
        String maskedIp = maskIp(rawIp);

        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        MDC.put(USER_AGENT_MDC_KEY, userAgent);
        MDC.put(CLIENT_IP_MDC_KEY, maskedIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY);
            MDC.remove(USER_AGENT_MDC_KEY);
            MDC.remove(CLIENT_IP_MDC_KEY);
        }
    }

    private String maskIp(String ipAddress) {
        if (ipAddress == null) return "unknown";
        
        // If comma-separated (e.g. from X-Forwarded-For), take first IP
        if (ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        if (ipAddress.contains(":")) {
            // IPv6 Address: mask last 80 bits (keep first 3 segments / 48 bits)
            String[] segments = ipAddress.split(":");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(segments.length, 3); i++) {
                sb.append(segments[i]).append(":");
            }
            sb.append("0:0:0:0:0");
            return sb.toString();
        } else if (ipAddress.contains(".")) {
            // IPv4 Address: zero out the last octet
            int lastDot = ipAddress.lastIndexOf('.');
            if (lastDot > 0) {
                return ipAddress.substring(0, lastDot) + ".0";
            }
        }
        return ipAddress;
    }
}
