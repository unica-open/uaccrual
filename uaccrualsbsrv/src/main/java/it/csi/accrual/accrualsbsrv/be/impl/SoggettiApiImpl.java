/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;

import it.csi.accrual.accrualsbsrv.be.SoggettiApi;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.service.SoggettoService;
import it.csi.accrual.accrualsbsrv.dto.Soggetto;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

@Component
public class SoggettiApiImpl implements SoggettiApi {

    @Autowired
    private SoggettoService soggettoService;
    
    @Autowired
    private SecurityContextHelper securityContextHelper;

    public SoggettiApiImpl(SoggettoService soggettoService) {
        this.soggettoService = soggettoService;
    }

    @Override
    public Response getSoggetti(@Valid @QueryParam("fields") String fields, @HeaderParam("customHeader") String customHeader, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        // Leggi enteCodice dal JWT
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        // Placeholder implementation
        return Response.ok().build();
    }

    @Override
    public Response getSoggettoByCodice(@Valid @PathParam("codice") String codice, @Valid @QueryParam("fields") String fields, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        // Leggi enteCodice dal JWT
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        try {
            Soggetto soggetto = soggettoService.getSoggettoByCodice(codice);
            return Response.ok().entity(soggetto).build();
        } catch (NotFoundException e) {
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Risorsa non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
    }

    @Override
    public Response getSoggettoByCodiceFiscale(@Valid @PathParam("codiceFiscale") String codiceFiscale, @Valid @QueryParam("fields") String fields, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        // Leggi enteCodice dal JWT
        String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
        try {
            Soggetto soggetto = soggettoService.getSoggettoByCodiceFiscale(codiceFiscale);
            return Response.ok().entity(soggetto).build();
        } catch (NotFoundException e) {
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Risorsa non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }
    }
}
