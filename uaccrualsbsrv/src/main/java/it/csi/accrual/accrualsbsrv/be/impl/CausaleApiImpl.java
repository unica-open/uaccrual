/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualsbsrv.be.CausaliApi;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.service.CausaleService;
import it.csi.accrual.accrualsbsrv.dto.Causale;
import it.csi.accrual.accrualsbsrv.dto.CausaleRequest;
import it.csi.accrual.accrualsbsrv.dto.CausaleStato;
import it.csi.accrual.accrualsbsrv.dto.CausaleTipo;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.dto.PagedCausale;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualcommonsrv.security.RequiredPermission;

@Component
public class CausaleApiImpl implements CausaliApi {

    private static final Logger LOG = LoggerFactory.getLogger(CausaleApiImpl.class);

    @Autowired
    private CausaleService causaleService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    public CausaleApiImpl(CausaleService causaleService) {
        this.causaleService = causaleService;
    }

    public CausaleApiImpl() {
        // Default constructor
    }

    @Override
    @Produces({ "application/json", "application/problem+json" })
    public Response getPagedCausali(
            @Valid @QueryParam("filter") String filter,
            @Valid @QueryParam("limit") Integer limit,
            @Valid @QueryParam("offset") Integer offset,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            PagedCausale pagedCausali = causaleService.getPagedCausali(filter, limit, offset);
            return Response.ok().entity(pagedCausali).build();
        } catch (Exception e) {
            LOG.error("Error retrieving Paged Causali list", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getCausaleById(
            @PathParam("id") Long id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            Causale causale = causaleService.getCausaleById(id);
            return Response.ok().entity(causale).build();
        } catch (NotFoundException e) {
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Causale non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error retrieving Causale by id: " + id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getTipiCausale(
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            List<CausaleTipo> tipiCausale = causaleService.getTipiCausale();
            return Response.ok().entity(tipiCausale).build();
        } catch (Exception e) {
            LOG.error("Error retrieving Causale Tipi", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }


    @Override
    public Response getCausaleStati(
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            List<CausaleStato> causaleStati = causaleService.getCausaleStati();
            return Response.ok().entity(causaleStati).build();
        } catch (Exception e) {
            LOG.error("Error retrieving Causale Stati", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getContiCollegati(
            @PathParam("id") Long id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        LOG.info("CausaleApiImpl.getContiCollegati - Invocato per id: {}", id);
        try {
            List<it.csi.accrual.accrualsbsrv.dto.ContoCollegato> contiCollegati = causaleService.getContiCollegati(id);
            return Response.ok().entity(contiCollegati).build();
        } catch (NotFoundException e) {
            LOG.error("CausaleApiImpl.getContiCollegati - NotFoundException: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Causale o ente non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (IllegalArgumentException e) {
            LOG.error("CausaleApiImpl.getContiCollegati - IllegalArgumentException: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            LOG.error("CausaleApiImpl.getContiCollegati - Errore generico durante il recupero dei Conti Collegati per id: " + id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/problem+json" })
    public Response createCausale(
            @Valid CausaleRequest body,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            Causale createdCausale = causaleService.createCausale(body);
            return Response.status(Response.Status.CREATED).entity(createdCausale).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid argument for createCausale: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (NotFoundException e) {
            LOG.warn("Resource not found during createCausale: {}", e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Risorsa non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error creating Causale", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    @PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/problem+json" })
    public Response updateCausale(
            @Valid CausaleRequest body,
            @Valid @PathParam("id") Long id,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            Causale updatedCausale = causaleService.updateCausale(id, body);
            return Response.ok().entity(updatedCausale).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid argument for updateCausale {}: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (NotFoundException e) {
            LOG.warn("Causale not found for update {}: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Causale non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error updating Causale {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response validaCausale(Long id, SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        try {
            causaleService.validaCausale(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            LOG.warn("Causale not found for validation {}: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Causale non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid argument for validaCausale {}: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error validating Causale {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response deleteCausale(Long id, SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        try {
            causaleService.deleteCausale(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            LOG.warn("Causale not found for deletion {}: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Causale non trovata");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid argument for deleteCausale {}: {}", id, e.getMessage());
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error deleting Causale {}", id, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getEventiCausale(
            @PathParam("eventoCodice") String eventoCodice,
            @QueryParam("statoCodice") String statoCodice,
            @Context SecurityContext securityContext,
            @Context HttpHeaders httpHeaders,
            @Context HttpServletRequest httpRequest) {
        try {
            List<it.csi.accrual.accrualsbsrv.dto.Causale> causali = causaleService.getEventiCausale(eventoCodice, statoCodice);
            return Response.ok().entity(causali).build();
        } catch (NotFoundException e) {
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("Ente non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (IllegalArgumentException e) {
            Error error = new Error();
            error.setStatus("400");
            error.setCode("BAD_REQUEST");
            error.setTitle("Richiesta non valida");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error retrieving Causali by eventoCodice: " + eventoCodice, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
