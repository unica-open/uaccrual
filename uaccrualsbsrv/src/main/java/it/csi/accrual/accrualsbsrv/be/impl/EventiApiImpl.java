/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualsbsrv.be.EventiApi;
import it.csi.accrual.accrualsbsrv.be.service.EventoService;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.dto.Evento;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

@Component
public class EventiApiImpl implements EventiApi {

    private static final Logger LOG = LoggerFactory.getLogger(EventiApiImpl.class);

    @Autowired
    private EventoService eventoService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    public EventiApiImpl(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    public EventiApiImpl() {
        // Default constructor
    }

    @Override
    public Response getEventi(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
            List<Evento> eventi = eventoService.getEventiByEnte(enteCodice);
            return Response.ok().entity(eventi).build();
        } catch (Exception e) {
            LOG.error("Error retrieving eventi", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getEventiByFonte(@PathParam("codiceFonte") String codiceFonte, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            String enteCodice = securityContextHelper.getEnteCodiceUnivoco();
            if (codiceFonte != null && !codiceFonte.trim().isEmpty()) {
                List<Evento> eventi = eventoService.getEventiByFonteAndEnte(codiceFonte, enteCodice);
                return Response.ok().entity(eventi).build();
            } else {
                // Se il parametro è mancante, restituire una risposta di errore appropriata
                Error error = new Error();
                error.setStatus("400");
                error.setCode("BAD_REQUEST");
                error.setTitle("Richiesta non valida");
                error.setDetail("Codice fonte è obbligatorio");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving eventi by fonte", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
