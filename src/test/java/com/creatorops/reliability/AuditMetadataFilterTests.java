package com.creatorops.reliability;

import com.creatorops.config.AuditMetadataFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuditMetadataFilterTests {

    @Test
    public void testAuditMetadataMdcMappingAndIpMasking() throws ServletException, IOException {
        AuditMetadataFilter filter = new AuditMetadataFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0)");
        when(request.getRemoteAddr()).thenReturn("192.168.1.155");

        doAnswer(invocation -> {
            // Verify MDC has the populated values inside the request chain execution
            assertNotNull(MDC.get(AuditMetadataFilter.REQUEST_ID_MDC_KEY));
            assertEquals("Mozilla/5.0 (Windows NT 10.0)", MDC.get(AuditMetadataFilter.USER_AGENT_MDC_KEY));
            assertEquals("192.168.1.0", MDC.get(AuditMetadataFilter.CLIENT_IP_MDC_KEY)); // Verify masked IP
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        // Verify MDC clean-up occurred after filter completion
        assertNull(MDC.get(AuditMetadataFilter.REQUEST_ID_MDC_KEY));
        assertNull(MDC.get(AuditMetadataFilter.USER_AGENT_MDC_KEY));
        assertNull(MDC.get(AuditMetadataFilter.CLIENT_IP_MDC_KEY));
    }
    
    @Test
    public void testAuditMetadataIpv6Masking() throws ServletException, IOException {
        AuditMetadataFilter filter = new AuditMetadataFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(request.getRemoteAddr()).thenReturn("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        doAnswer(invocation -> {
            assertEquals("2001:0db8:85a3:0:0:0:0:0", MDC.get(AuditMetadataFilter.CLIENT_IP_MDC_KEY)); // Masked IPv6
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);
    }
}
