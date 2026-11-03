/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.be.impl;

import it.csi.accrual.accrualreportsbapi.be.PingApi;
import it.csi.accrual.accrualcommonsrv.api.impl.BasePingApiImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.springframework.stereotype.Component;

@Component
@Path("/ping")
public class PingApiImpl extends BasePingApiImpl implements PingApi {

    @Override
    public Response pingGet(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        return Response.ok("PONG").build();
    }
}