/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.be.impl;

import it.csi.accrual.accrualreportsbapi.be.PingApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PingApiImpl Tests")
class PingApiImplTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private HttpServletRequest httpRequest;

    private PingApiImpl pingApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pingApi = new PingApiImpl();
    }

    @Test
    @DisplayName("Should return OK status for ping")
    void testPingSuccess() {
        Response response = pingApi.pingGet(securityContext, httpHeaders, httpRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    @DisplayName("Should return consistent response")
    void testPingConsistency() {
        Response response1 = pingApi.pingGet(securityContext, httpHeaders, httpRequest);
        Response response2 = pingApi.pingGet(securityContext, httpHeaders, httpRequest);

        assertEquals(response1.getStatus(), response2.getStatus());
        assertNotNull(response1.getEntity());
        assertNotNull(response2.getEntity());
    }

    @Test
    @DisplayName("Should return valid response entity")
    void testPingResponseEntity() {
        Response response = pingApi.pingGet(securityContext, httpHeaders, httpRequest);

        assertNotNull(response.getEntity());
    }
}
