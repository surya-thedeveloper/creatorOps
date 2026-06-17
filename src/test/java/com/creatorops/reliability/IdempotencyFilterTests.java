package com.creatorops.reliability;

import com.creatorops.config.IdempotencyFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IdempotencyFilterTests {

    private IdempotencyFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    
    private StringWriter responseStringWriter;
    private ByteArrayOutputStream responseOutputStream;

    @BeforeEach
    public void setUp() throws IOException {
        filter = new IdempotencyFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        responseStringWriter = new StringWriter();
        responseOutputStream = new ByteArrayOutputStream();

        final int[] statusHolder = new int[1];
        final String[] contentTypeHolder = new String[1];

        doAnswer(invocation -> {
            statusHolder[0] = invocation.getArgument(0);
            return null;
        }).when(response).setStatus(anyInt());

        when(response.getStatus()).thenAnswer(invocation -> statusHolder[0]);

        doAnswer(invocation -> {
            contentTypeHolder[0] = invocation.getArgument(0);
            return null;
        }).when(response).setContentType(anyString());

        when(response.getContentType()).thenAnswer(invocation -> contentTypeHolder[0]);

        when(response.getWriter()).thenReturn(new PrintWriter(responseStringWriter));
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) {}
            @Override
            public void write(int b) throws IOException {
                responseOutputStream.write(b);
            }
        });
    }

    @Test
    public void testFirstRequestCachesSuccess() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/ai/contents/123/brainstorm");
        when(request.getHeader("Idempotency-Key")).thenReturn("key-12345");

        // First execution: proceed down the chain
        doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(1);
            resp.setStatus(200);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"result\":\"success\"}");
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        // Verify it called the chain
        verify(filterChain, times(1)).doFilter(any(), any());

        // Reset mocks for second request (duplicate)
        reset(filterChain);
        HttpServletResponse duplicateResponse = mock(HttpServletResponse.class);
        ByteArrayOutputStream duplicateOutputStream = new ByteArrayOutputStream();
        when(duplicateResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) {}
            @Override
            public void write(int b) throws IOException {
                duplicateOutputStream.write(b);
            }
        });

        // Second execution: must serve from cache directly without running chain
        filter.doFilter(request, duplicateResponse, filterChain);

        verifyNoInteractions(filterChain);
        verify(duplicateResponse).setStatus(200);
        verify(duplicateResponse).setContentType("application/json");
        assertEquals("{\"result\":\"success\"}", duplicateOutputStream.toString());
    }

    @Test
    public void testConcurrentInProgressRequestReturnsConflict() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/ai/contents/123/brainstorm");
        when(request.getHeader("Idempotency-Key")).thenReturn("key-concurrent");

        // First execution blocks or hangs down the chain (simulating running)
        doAnswer(invocation -> {
            // While running, a second request with same key arrives
            HttpServletRequest dupReq = mock(HttpServletRequest.class);
            HttpServletResponse dupResp = mock(HttpServletResponse.class);
            StringWriter dupWriter = new StringWriter();
            when(dupReq.getMethod()).thenReturn("POST");
            when(dupReq.getRequestURI()).thenReturn("/api/v1/ai/contents/123/brainstorm");
            when(dupReq.getHeader("Idempotency-Key")).thenReturn("key-concurrent");
            when(dupResp.getWriter()).thenReturn(new PrintWriter(dupWriter));

            filter.doFilter(dupReq, dupResp, filterChain);

            verify(dupResp).setStatus(409); // Conflict status
            assertTrue(dupWriter.toString().contains("already in progress"));
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);
    }

    @Test
    public void testFailedRequestDoesNotCache() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/ai/contents/123/brainstorm");
        when(request.getHeader("Idempotency-Key")).thenReturn("key-fail");

        // Mock error response
        doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(1);
            resp.setStatus(500);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        // Verification chain was executed
        verify(filterChain, times(1)).doFilter(any(), any());

        // Run second request: since the first one failed, the filter should attempt to run it again!
        reset(filterChain);
        filter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(), any());
    }
}
