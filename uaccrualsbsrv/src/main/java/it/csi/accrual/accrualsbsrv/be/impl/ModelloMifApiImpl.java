/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualsbsrv.be.ModellomifApi;
import it.csi.accrual.accrualsbsrv.be.service.ModelloMifService;
import it.csi.accrual.accrualsbsrv.dto.EntitaContabile;
import it.csi.accrual.accrualsbsrv.dto.ModelloMifData;
import java.util.List;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualcommonsrv.security.RequiredPermission;

@Component
public class ModelloMifApiImpl implements ModellomifApi {

    private static final Logger log = LoggerFactory.getLogger(ModelloMifApiImpl.class);

    @Autowired
    private ModelloMifService modelloMifService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Override
    public Response getDatiModelloMif(@NotNull Integer anno, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            ModelloMifData data = modelloMifService.getDatiModelloMif(anno);
            return Response.ok(data).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("Errore durante il recupero dati modello MIF per anno {}: {}", anno, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public Response getAnniModelloMif(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            List<EntitaContabile> anni = modelloMifService.getAnniDisponibili();
            return Response.ok(anni).build();
        } catch (Exception e) {
            log.error("Errore durante il recupero anni modello MIF: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
