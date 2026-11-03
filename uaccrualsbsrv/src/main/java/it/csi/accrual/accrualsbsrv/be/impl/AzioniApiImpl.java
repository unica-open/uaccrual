/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import it.csi.accrual.accrualsbsrv.be.AzioniApi;
import it.csi.accrual.accrualsbsrv.be.service.AzioniService;
import it.csi.accrual.accrualsbsrv.dto.Azione;
import it.csi.accrual.accrualsbsrv.dto.GruppoAzione;
import org.springframework.stereotype.Component;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@Component
public class AzioniApiImpl implements AzioniApi {

    @Inject
    private AzioniService azioniService;

    @Override
    public Response getAzioni(String groupBy, SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        String authHeader = httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String jwtToken = authHeader.substring(7);

        if ("gruppo".equals(groupBy)) {
            List<GruppoAzione> result = azioniService.getAzioniGrouped(jwtToken);
            return Response.ok(result).build();
        } else {
            List<Azione> result = azioniService.getAzioni(jwtToken);
            return Response.ok(result).build();
        }
    }
}
