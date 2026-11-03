/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.be.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("BodySanitizerFilter Tests")
@ExtendWith(MockitoExtension.class)
class BodySanitizerFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private UriInfo uriInfo;

    private BodySanitizerFilter bodySanitizerFilter;

    @BeforeEach
    void setUp() {
        bodySanitizerFilter = new BodySanitizerFilter();
    }

    @Test
    @DisplayName("Should process POST requests to /report/generate")
    void testProcessPostReportGenerate() throws Exception {
        String originalBody = "  \uFEFF  {\"param\": \"value\"}  ";
        String expectedSanitizedBody = "{\"param\": \"value\"}";
        
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/report/generate");
        when(requestContext.getMethod()).thenReturn("POST");
        
        InputStream inputStream = new ByteArrayInputStream(originalBody.getBytes());
        when(requestContext.getEntityStream()).thenReturn(inputStream);
        
        bodySanitizerFilter.filter(requestContext);
        
        verify(requestContext).setEntityStream(any(InputStream.class));
    }

    @Test
    @DisplayName("Should not process GET requests")
    void testNotProcessGetRequests() throws Exception {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/report/generate");
        when(requestContext.getMethod()).thenReturn("GET");
        
        bodySanitizerFilter.filter(requestContext);
        
        // Should not call setEntityStream for GET requests
        verify(requestContext, never()).setEntityStream(any(InputStream.class));
    }

    @Test
    @DisplayName("Should not process POST requests to other endpoints")
    void testNotProcessOtherEndpoints() throws Exception {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/other/endpoint");
        when(requestContext.getMethod()).thenReturn("POST");
        
        bodySanitizerFilter.filter(requestContext);
        
        // Should not call setEntityStream for other endpoints
        verify(requestContext, never()).setEntityStream(any(InputStream.class));
    }

    @Test
    @DisplayName("Should handle null request body")
    void testHandleNullRequestBody() throws Exception {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/report/generate");
        when(requestContext.getMethod()).thenReturn("POST");
        when(requestContext.getEntityStream()).thenReturn(null);
        
        bodySanitizerFilter.filter(requestContext);
        
        verify(requestContext).setEntityStream(any(InputStream.class));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void testHandleEmptyRequestBody() throws Exception {
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/report/generate");
        when(requestContext.getMethod()).thenReturn("POST");
        
        InputStream inputStream = new ByteArrayInputStream("".getBytes());
        when(requestContext.getEntityStream()).thenReturn(inputStream);
        
        bodySanitizerFilter.filter(requestContext);
        
        verify(requestContext).setEntityStream(any(InputStream.class));
    }

    @Test
    @DisplayName("Should remove BOM from request body")
    void testRemoveBomFromBody() throws Exception {
        String bodyWithBom = "\uFEFF{\"param\": \"value\"}";
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/report/generate");
        when(requestContext.getMethod()).thenReturn("POST");
        
        InputStream inputStream = new ByteArrayInputStream(bodyWithBom.getBytes());
        when(requestContext.getEntityStream()).thenReturn(inputStream);
        
        bodySanitizerFilter.filter(requestContext);
        
        verify(requestContext).setEntityStream(any(InputStream.class));
    }

    @Test
    @DisplayName("Should trim request body")
    void testTrimRequestBody() throws Exception {
        String bodyWithWhitespace = "   {\"param\": \"value\"}   ";
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/report/generate");
        when(requestContext.getMethod()).thenReturn("POST");
        
        InputStream inputStream = new ByteArrayInputStream(bodyWithWhitespace.getBytes());
        when(requestContext.getEntityStream()).thenReturn(inputStream);
        
        bodySanitizerFilter.filter(requestContext);
        
        verify(requestContext).setEntityStream(any(InputStream.class));
    }
}
