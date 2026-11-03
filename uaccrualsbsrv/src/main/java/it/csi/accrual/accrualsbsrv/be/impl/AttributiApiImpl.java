/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import it.csi.accrual.accrualsbsrv.be.AttributiApi;
import it.csi.accrual.accrualsbsrv.dto.AttributoInfoDTO;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.be.service.AttributoService;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Implementazione dell'API per la gestione degli attributi
 */
@Component
public class AttributiApiImpl implements AttributiApi {

    @Autowired
    private AttributoService attributoService;
    
    @Autowired
    private SecurityContextHelper securityContextHelper;

    public AttributiApiImpl(AttributoService attributoService) {
        this.attributoService = attributoService;
    }

    @Override
    public Response getAttributiByEnte(
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {

        try {
            List<AttributoInfoDTO> attributi = attributoService.getAttributiByEnte();
            return Response.ok(attributi).build();
        } catch (Exception e) {
            Error error = new Error();
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore");
            error.setDetail("Si è verificato un errore imprevisto: " + e.getMessage());
            return Response.serverError().entity(error).build();
        }
    }
}
