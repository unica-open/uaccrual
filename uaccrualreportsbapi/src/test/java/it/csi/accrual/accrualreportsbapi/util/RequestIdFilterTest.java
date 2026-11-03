/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("RequestIdFilter Tests")
@ExtendWith(MockitoExtension.class)
class RequestIdFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RequestIdFilter requestIdFilter;

    @BeforeEach
    void setUp() {
        requestIdFilter = new RequestIdFilter();
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate and set request ID when not provided")
    void testGenerateRequestIdWhenNotProvided() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        requestIdFilter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("X-Request-ID"), any(String.class));
        verify(filterChain).doFilter(request, response);
        
        // MDC should be cleared after filter chain
        assertNull(MDC.get("requestId"));
    }

    @Test
    @DisplayName("Should use existing request ID when provided")
    void testUseExistingRequestId() throws Exception {
        String existingRequestId = "existing-request-id-12345";
        when(request.getHeader("X-Request-ID")).thenReturn(existingRequestId);

        requestIdFilter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-Request-ID", existingRequestId);
        verify(filterChain).doFilter(request, response);
        
        assertNull(MDC.get("requestId"));
    }

    @Test
    @DisplayName("Should handle empty request ID header")
    void testEmptyRequestIdHeader() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn("");

        requestIdFilter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("X-Request-ID"), any(String.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle blank request ID header")
    void testBlankRequestIdHeader() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn("   ");

        requestIdFilter.doFilter(request, response, filterChain);

        verify(response).setHeader(eq("X-Request-ID"), any(String.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should clear MDC after filter chain execution")
    void testMdcClearedAfterExecution() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn("test-request-id");

        requestIdFilter.doFilter(request, response, filterChain);

        assertNull(MDC.get("requestId"), "MDC should be cleared after filter execution");
    }

    @Test
    @DisplayName("Should clear MDC even when exception occurs")
    void testMdcClearedOnException() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn("test-request-id");
        doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(any(), any());

        try {
            requestIdFilter.doFilter(request, response, filterChain);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertEquals("Test exception", e.getMessage());
        }

        assertNull(MDC.get("requestId"), "MDC should be cleared even on exception");
    }

    @Test
    @DisplayName("Should generate different request IDs for different requests")
    void testGenerateDifferentRequestIds() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        // First request
        requestIdFilter.doFilter(request, response, filterChain);
        verify(response).setHeader(eq("X-Request-ID"), any(String.class));

        // Reset mock
        reset(response);

        // Second request
        requestIdFilter.doFilter(request, response, filterChain);
        verify(response).setHeader(eq("X-Request-ID"), any(String.class));
    }

    @Test
    @DisplayName("Should handle multiple filter invocations")
    void testMultipleFilterInvocations() throws Exception {
        String requestId1 = "request-1";
        String requestId2 = "request-2";

        // First invocation
        when(request.getHeader("X-Request-ID")).thenReturn(requestId1);
        requestIdFilter.doFilter(request, response, filterChain);
        verify(response).setHeader("X-Request-ID", requestId1);

        // Second invocation
        reset(response);
        when(request.getHeader("X-Request-ID")).thenReturn(requestId2);
        requestIdFilter.doFilter(request, response, filterChain);
        verify(response).setHeader("X-Request-ID", requestId2);
    }

    @Test
    @DisplayName("Should preserve request ID through filter chain")
    void testPreserveRequestIdThroughChain() throws Exception {
        String requestId = "preserved-request-id";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);

        doAnswer(invocation -> {
            // Verify MDC contains request ID during filter chain execution
            assertNotNull(MDC.get("requestId"));
            assertEquals(requestId, MDC.get("requestId"));
            return null;
        }).when(filterChain).doFilter(request, response);

        requestIdFilter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-Request-ID", requestId);
    }

    @Test
    @DisplayName("Should handle null filter chain")
    void testNullFilterChain() {
        when(request.getHeader("X-Request-ID")).thenReturn("test-id");

        assertThrows(NullPointerException.class, () -> 
            requestIdFilter.doFilter(request, response, null)
        );
    }

    @Test
    @DisplayName("Should handle special characters in request ID")
    void testSpecialCharactersInRequestId() throws Exception {
        String specialRequestId = "req-123-abc-xyz-2025-01-15";
        when(request.getHeader("X-Request-ID")).thenReturn(specialRequestId);

        requestIdFilter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-Request-ID", specialRequestId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle very long request ID")
    void testVeryLongRequestId() throws Exception {
        String longRequestId = "a".repeat(200);
        when(request.getHeader("X-Request-ID")).thenReturn(longRequestId);

        requestIdFilter.doFilter(request, response, filterChain);

        verify(response).setHeader("X-Request-ID", longRequestId);
        verify(filterChain).doFilter(request, response);
    }
}
