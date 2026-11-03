/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualsbsrv.be.TipiFileApi;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.service.FlussoService;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.dto.TipoFile;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

import java.util.List;

@Component
public class TipiFileApiImpl implements TipiFileApi {

    private static final Logger LOG = LoggerFactory.getLogger(TipiFileApiImpl.class);

    @Autowired
    private FlussoService flussoService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Override
    public Response getTipiFile(
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            List<TipoFile> tipiFile = flussoService.getTipiFile();
            return Response.ok().entity(tipiFile).build();
        } catch (NotFoundException e) {
            LOG.warn("TipiFileApiImpl.getTipiFile - NotFoundException: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Risorsa non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("TipiFileApiImpl.getTipiFile - Errore generico durante il recupero dei tipi di file", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
