/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.List;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import it.csi.accrual.accrualsbsrv.be.PdcApi;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.exception.ValidationException;
import it.csi.accrual.accrualsbsrv.be.exception.DuplicateRecordException;
import it.csi.accrual.accrualsbsrv.be.exception.ForbiddenOperationException;
import it.csi.accrual.accrualsbsrv.be.exception.IntegrityConstraintException;
import it.csi.accrual.accrualsbsrv.be.service.PdcService;
import it.csi.accrual.accrualsbsrv.dto.Error;
import it.csi.accrual.accrualsbsrv.dto.PagedPdc;
import it.csi.accrual.accrualsbsrv.dto.Pdc;
import it.csi.accrual.accrualsbsrv.dto.PdcClasse;
import it.csi.accrual.accrualsbsrv.dto.PdcFamiglia;
import it.csi.accrual.accrualsbsrv.dto.PdcStato;
import it.csi.accrual.accrualsbsrv.dto.PdcTipo;
import it.csi.accrual.accrualsbsrv.dto.PdcContoRelazione;
import it.csi.accrual.accrualsbsrv.dto.PdcFilter;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;
import it.csi.accrual.accrualcommonsrv.security.RequiredPermission;

@Component
public class PdcApiImpl implements PdcApi {

    private static final Logger LOG = LoggerFactory.getLogger(PdcApiImpl.class);

    @Autowired
    private PdcService pdcService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    public PdcApiImpl() {
        // Default constructor
    }

    @Autowired
    public PdcApiImpl(PdcService pdcService) {
        this.pdcService = pdcService;
    }

    @Override
    public Response createPdc(@Valid Pdc body, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            Pdc createdPdc = pdcService.createPdc(body);
            return Response.status(Response.Status.CREATED).entity(createdPdc).build();
        } catch (ValidationException e) {
            LOG.warn("Validation error creating PDC", e);
            Error error = new Error();
            error.setStatus("400");
            error.setCode("VALIDATION_ERROR");
            error.setTitle("Errore di validazione");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (DuplicateRecordException e) {
            LOG.warn("Duplicate record error creating PDC", e);
            Error error = new Error();
            error.setStatus("409");
            error.setCode("DUPLICATE_RECORD");
            error.setTitle("Record duplicato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        } catch (ForbiddenOperationException e) {
            LOG.warn("Forbidden operation error creating PDC", e);
            Error error = new Error();
            error.setStatus("403");
            error.setCode("FORBIDDEN_OPERATION");
            error.setTitle("Operazione non consentita");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error creating PDC", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response deletePdcByTripleKey(@Valid @PathParam("codiceUfficiale") String codiceUfficiale,
                             @Valid @PathParam("codiceClasse") String codiceClasse,
                             @Valid @PathParam("codiceFamiglia") String codiceFamiglia,
                             @Context SecurityContext securityContext,
                             @Context HttpHeaders httpHeaders,
                             @Context HttpServletRequest httpRequest) {
        LOG.info("Richiesta deletePdcByTripleKey ricevuta per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                 codiceUfficiale, codiceClasse, codiceFamiglia);

        try {
            pdcService.deletePdcByTripleKey(codiceUfficiale, codiceClasse, codiceFamiglia);
            LOG.info("DeletePdcByTripleKey completato con successo per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                     codiceUfficiale, codiceClasse, codiceFamiglia);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            LOG.warn("NotFoundException in deletePdcByTripleKey per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                     codiceUfficiale, codiceClasse, codiceFamiglia, e);
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("PDC non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (ValidationException e) {
            LOG.warn("Validation error deleting PDC", e);
            Error error = new Error();
            error.setStatus("400");
            error.setCode("VALIDATION_ERROR");
            error.setTitle("Errore di validazione");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (IntegrityConstraintException e) {
            LOG.warn("Integrity constraint error deleting PDC", e);
            Error error = new Error();
            error.setStatus("409");
            error.setCode("INTEGRITY_CONSTRAINT_VIOLATION");
            error.setTitle("Vincolo di integrità violato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        } catch (ForbiddenOperationException e) {
            LOG.warn("Forbidden operation error deleting PDC", e);
            Error error = new Error();
            error.setStatus("403");
            error.setCode("FORBIDDEN_OPERATION");
            error.setTitle("Operazione non consentita");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error deleting PDC with codiceUfficiale: " + codiceUfficiale + ", codiceClasse: " + codiceClasse + ", codiceFamiglia: " + codiceFamiglia, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getPdcList(@QueryParam("sort") @Valid String sort, @QueryParam("limit") @Valid Integer limit, @QueryParam("offset") @Valid Integer offset, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("Ricevuta richiesta getPdcList con parametri - sort: {}, limit: {}, offset: {}", sort, limit, offset);
            // Anno bilancio letto automaticamente da BilancioContextHolder (dal JWT)
            PagedPdc pagedPdc = pdcService.getPdcList(sort, limit, offset);
            return Response.ok().entity(pagedPdc).build();
        } catch (Exception e) {
            LOG.error("Error retrieving PDC list", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response searchPdc(@Valid PdcFilter pdcFilter, @QueryParam("sort") @Valid String sort, @QueryParam("limit") @Valid Integer limit, @QueryParam("offset") @Valid Integer offset, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("Ricevuta richiesta searchPdc con parametri - sort: {}, limit: {}, offset: {}", sort, limit, offset);

            if (pdcFilter != null) {
                LOG.info("PdcFilter ricevuto: famiglia={}, codiceEvento={}, pdcClasseCodice={}, codiceUfficiale={}, pdcStatoCodice={}, descrizione={}, livello={}, modificabile={}, pdcTipoCodice={}",
                    pdcFilter.getFamiglia(), pdcFilter.getCodiceEvento(), pdcFilter.getPdcClasseCodice(),
                    pdcFilter.getCodiceUfficiale(), pdcFilter.getPdcStatoCodice(), pdcFilter.getDescrizione(),
                    pdcFilter.getLivello(), pdcFilter.isModificabile(),
                    pdcFilter.getPdcTipoCodice());
            } else {
                LOG.warn("PdcFilter ricevuto è null - questo potrebbe indicare un problema di parsing del corpo della richiesta");
            }

            // Verifichiamo se ci sono errori di validazione
            if (pdcFilter == null) {
                LOG.error("PdcFilter è null, impossibile procedere con la ricerca");
                Error error = new Error();
                error.setStatus("400");
                error.setCode("BAD_REQUEST");
                error.setTitle("Richiesta non valida");
                error.setDetail("Il corpo della richiesta non è stato correttamente elaborato");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            // Validazione manuale dei campi obbligatori (pdc_classe_codice e codice_ufficiale)
            // Le annotazioni @NotNull non vengono sempre validate correttamente da Jersey
            if (pdcFilter.getPdcClasseCodice() == null || pdcFilter.getPdcClasseCodice().trim().isEmpty()) {
                LOG.warn("Validazione fallita: pdc_classe_codice è obbligatorio");
                Error error = new Error();
                error.setStatus("400");
                error.setCode("VALIDATION_ERROR");
                error.setTitle("Errore di validazione");
                error.setDetail("WEB_ERR_0002 - Dato obbligatorio non valorizzato: Classe");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }
            if (pdcFilter.getCodiceUfficiale() == null || pdcFilter.getCodiceUfficiale().trim().isEmpty()) {
                LOG.warn("Validazione fallita: codice_ufficiale è obbligatorio");
                Error error = new Error();
                error.setStatus("400");
                error.setCode("VALIDATION_ERROR");
                error.setTitle("Errore di validazione");
                error.setDetail("WEB_ERR_0002 - Dato obbligatorio non valorizzato: Codice");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            // Anno bilancio letto automaticamente da BilancioContextHolder (dal JWT)
            PagedPdc pagedPdc = pdcService.searchPdc(pdcFilter, sort, limit, offset);
            LOG.info("Risposta searchPdc generata con successo, numero di elementi: {}", pagedPdc.getData() != null ? pagedPdc.getData().size() : 0);
            return Response.ok().entity(pagedPdc).build();
        } catch (Exception e) {
            LOG.error("Error searching PDC list - sort: {}, limit: {}, offset: {}", sort, limit, offset, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }



    @GET
    @Path("/{codiceUfficiale}/{codiceClasse}/{codiceFamiglia}")
    public Response getPdcByCodiceUfficialeClasseFamiglia(@PathParam("codiceUfficiale") @Valid String codiceUfficiale,
                                                         @PathParam("codiceClasse") @Valid String codiceClasse,
                                                         @PathParam("codiceFamiglia") @Valid String codiceFamiglia,
                                                         @QueryParam("fields") @Valid String fields,
                                                         @Context SecurityContext securityContext,
                                                         @Context HttpHeaders httpHeaders,
                                                         @Context HttpServletRequest httpRequest) {
        LOG.info("Richiesta getPdcByCodiceUfficialeClasseFamiglia ricevuta per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                 codiceUfficiale, codiceClasse, codiceFamiglia);

        try {
            // Anno bilancio letto automaticamente da BilancioContextHolder (dal JWT)
            Pdc pdc = pdcService.getPdcByCodiceUfficialeClasseFamiglia(codiceUfficiale, codiceClasse, codiceFamiglia);
            if (pdc != null) {
                LOG.info("getPdcByCodiceUfficialeClasseFamiglia completato con successo per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                         codiceUfficiale, codiceClasse, codiceFamiglia);
                return Response.ok().entity(pdc).build();
            } else {
                LOG.warn("getPdcByCodiceUfficialeClasseFamiglia restituito null per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                         codiceUfficiale, codiceClasse, codiceFamiglia);
                Error error = new Error();
                error.setStatus("404");
                error.setCode("NOT_FOUND");
                error.setTitle("PDC non trovato");
                error.setDetail("PDC con codice ufficiale " + codiceUfficiale + ", classe " + codiceClasse + ", famiglia " + codiceFamiglia + " non trovato");
                return Response.status(Response.Status.NOT_FOUND).entity(error).build();
            }
        } catch (NotFoundException e) {
            LOG.warn("NotFoundException in getPdcByCodiceUfficialeClasseFamiglia per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                     codiceUfficiale, codiceClasse, codiceFamiglia, e);
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("PDC non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error retrieving PDC by codice ufficiale, classe, famiglia: " + codiceUfficiale + ", " + codiceClasse + ", " + codiceFamiglia, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }


    @PUT
    @Path("/{codiceUfficiale}/{codiceClasse}/{codiceFamiglia}")
    @Override
    public Response updatePdcByTripleKey(@Valid Pdc body,
                             @Valid @PathParam("codiceUfficiale") String codiceUfficiale,
                             @Valid @PathParam("codiceClasse") String codiceClasse,
                             @Valid @PathParam("codiceFamiglia") String codiceFamiglia,
                             @Context SecurityContext securityContext,
                             @Context HttpHeaders httpHeaders,
                             @Context HttpServletRequest httpRequest) {
        LOG.info("Richiesta updatePdcByTripleKey ricevuta per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                 codiceUfficiale, codiceClasse, codiceFamiglia);

        try {
            Pdc updatedPdc = pdcService.updatePdcByTripleKey(codiceUfficiale, codiceClasse, codiceFamiglia, body);
            if (updatedPdc != null) {
                LOG.info("UpdatePdcByTripleKey completato con successo per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                         codiceUfficiale, codiceClasse, codiceFamiglia);
                return Response.ok().entity(updatedPdc).build();
            } else {
                LOG.warn("UpdatePdcByTripleKey restituito null per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                         codiceUfficiale, codiceClasse, codiceFamiglia);
                Error error = new Error();
                error.setStatus("404");
                error.setCode("NOT_FOUND");
                error.setTitle("PDC non trovato");
                error.setDetail("PDC con codice ufficiale " + codiceUfficiale + ", classe " + codiceClasse + ", famiglia " + codiceFamiglia + " non trovato");
                return Response.status(Response.Status.NOT_FOUND).entity(error).build();
            }
        } catch (NotFoundException e) {
            LOG.warn("NotFoundException in updatePdcByTripleKey per codiceUfficiale: {}, codiceClasse: {}, codiceFamiglia: {}",
                     codiceUfficiale, codiceClasse, codiceFamiglia, e);
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("PDC non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (ValidationException e) {
            LOG.warn("Validation error updating PDC", e);
            Error error = new Error();
            error.setStatus("400");
            error.setCode("VALIDATION_ERROR");
            error.setTitle("Errore di validazione");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (DuplicateRecordException e) {
            LOG.warn("Duplicate record error updating PDC", e);
            Error error = new Error();
            error.setStatus("409");
            error.setCode("DUPLICATE_RECORD");
            error.setTitle("Record duplicato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        } catch (ForbiddenOperationException e) {
            LOG.warn("Forbidden operation error updating PDC", e);
            Error error = new Error();
            error.setStatus("403");
            error.setCode("FORBIDDEN_OPERATION");
            error.setTitle("Operazione non consentita");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(error).build();
        } catch (OptimisticLockException ole) {
            LOG.error("Conflitto di concorrenza rilevato per conto: {}", codiceUfficiale, ole);
            
            // Crea oggetto Error strutturato
            Error error = new Error();
            error.setCode("OPTIMISTIC_LOCK");
            error.setTitle("Conflitto di concorrenza");
            error.setDetail("Il conto è stato modificato da un altro utente. Ricaricare la pagina e riprovare.");
            
            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            LOG.error("Error updating PDC with codice ufficiale: " + codiceUfficiale + ", codiceClasse: " + codiceClasse + ", codiceFamiglia: " + codiceFamiglia, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getPdcClassi(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("Ricevuta richiesta getPdcClassi");

            // Anno bilancio letto automaticamente da BilancioContextHolder (dal JWT)
            List<PdcClasse> pdcClassi = pdcService.getPdcClassiValid();
            return Response.ok().entity(pdcClassi).build();
        } catch (Exception e) {
            LOG.error("Error retrieving PDC classes", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getPdcTipi(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            List<PdcTipo> pdcTipi = pdcService.getPdcTipi();
            return Response.ok().entity(pdcTipi).build();
        } catch (Exception e) {
            LOG.error("Error retrieving PDC types", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getPdcStati(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            List<PdcStato> pdcStati = pdcService.getPdcStati();
            return Response.ok().entity(pdcStati).build();
        } catch (Exception e) {
            LOG.error("Error retrieving PDC states", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getPdcFamiglie(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            List<PdcFamiglia> pdcFamiglie = pdcService.getPdcFamiglie();
            return Response.ok().entity(pdcFamiglie).build();
        } catch (Exception e) {
            LOG.error("Error retrieving PDC families", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }


    @Override
    public Response getPdcRelazioniByKey(@PathParam("codiceUfficiale") @Valid String codiceUfficiale,
                                       @PathParam("codiceClasse") @Valid String codiceClasse,
                                       @PathParam("codiceFamiglia") @Valid String codiceFamiglia,
                                       @QueryParam("tipo") String tipo,
                                       @Context SecurityContext securityContext,
                                       @Context HttpHeaders httpHeaders,
                                       @Context HttpServletRequest httpRequest) {
        try {
            List<PdcContoRelazione> pdcRelazioni = pdcService.getPdcRelazioni(codiceUfficiale, codiceClasse, codiceFamiglia, tipo);
            return Response.ok().entity(pdcRelazioni).build();
        } catch (NotFoundException e) {
            Error error = new Error();
            error.setStatus("404");
            error.setCode("NOT_FOUND");
            error.setTitle("PDC non trovato");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        } catch (Exception e) {
            LOG.error("Error retrieving PDC relations for codiceUfficiale: " + codiceUfficiale + ", codiceClasse: " + codiceClasse + ", codiceFamiglia: " + codiceFamiglia, e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response searchPdcTree(@Valid it.csi.accrual.accrualsbsrv.dto.PdcTreeFilter pdcTreeFilter, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("Ricevuta richiesta searchPdcTree");

            if (pdcTreeFilter != null) {
                LOG.debug("PdcTreeFilter ricevuto: codice={}, descrizione={}, famiglia={}, codiceEvento={}, pdcClasseCodice={}, pdcStatoCodice={}, livello={}",
                    pdcTreeFilter.getCodice(), pdcTreeFilter.getDescrizione(), pdcTreeFilter.getFamiglia(),
                    pdcTreeFilter.getCodiceEvento(), pdcTreeFilter.getPdcClasseCodice(),
                    pdcTreeFilter.getPdcStatoCodice(), pdcTreeFilter.getLivello());
            } else {
                LOG.warn("PdcTreeFilter ricevuto è null");
            }

            // Verifichiamo se ci sono errori di validazione
            if (pdcTreeFilter == null) {
                LOG.error("PdcTreeFilter è null, impossibile procedere con la ricerca");
                Error error = new Error();
                error.setStatus("400");
                error.setCode("BAD_REQUEST");
                error.setTitle("Richiesta non valida");
                error.setDetail("Il corpo della richiesta non è stato correttamente elaborato");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            it.csi.accrual.accrualsbsrv.dto.PdcTreeResult treeResult = pdcService.searchPdcTree(pdcTreeFilter);
            LOG.info("Risposta searchPdcTree generata con successo, numero di nodi radice: {}",
                     treeResult.getNodes() != null ? treeResult.getNodes().size() : 0);
            return Response.ok().entity(treeResult).build();
        } catch (Exception e) {
            LOG.error("Error searching PDC tree", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response exportPdcList(@Valid PdcFilter pdcFilter, @QueryParam("sort") @Valid String sort, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("Richiesta export PDC - filter: {}, sort: {}", pdcFilter, sort);

            // Anno bilancio letto automaticamente da BilancioContextHolder (dal JWT)
            List<Pdc> pdcList = pdcService.exportPdcList(pdcFilter, sort);

            // Genera CSV
            String csv = generateCsv(pdcList);

            // Prepara headers per download
            String filename = "pdc_export_" + LocalDate.now() + ".csv";

            LOG.info("Export PDC completato - {} elementi, file: {}", pdcList.size(), filename);

            return Response.ok()
                    .entity(csv.getBytes(StandardCharsets.UTF_8))
                    .type("text/csv;charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();

        } catch (Exception e) {
            LOG.error("Errore durante l'export CSV: {}", e.getMessage(), e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("INTERNAL_ERROR");
            error.setTitle("Errore interno");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    /**
     * Genera CSV dalla lista di PDC
     */
    private String generateCsv(List<Pdc> pdcList) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Codice Ufficiale;Codice;Descrizione;Classe;Tipo;Stato;Livello;Inizio Validità;Fine Validità\n");

        // Dati
        for (Pdc pdc : pdcList) {
            sb.append(escapeCsv(pdc.getCodiceUfficiale())).append(";");
            sb.append(escapeCsv(pdc.getCodice())).append(";");
            sb.append(escapeCsv(pdc.getDescrizione())).append(";");
            sb.append(escapeCsv(pdc.getPdcClasse() != null ? pdc.getPdcClasse().getDescrizione() : null)).append(";");
            sb.append(escapeCsv(pdc.getPdcTipo() != null ? pdc.getPdcTipo().getDescrizione() : null)).append(";");
            sb.append(escapeCsv(pdc.getPdcStato() != null ? pdc.getPdcStato().getDescrizione() : null)).append(";");
            sb.append(pdc.getLivello() != null ? pdc.getLivello() : "").append(";");
            sb.append(pdc.getInizioValidita() != null ? pdc.getInizioValidita().toString() : "").append(";");
            sb.append(pdc.getFineValidita() != null ? pdc.getFineValidita().toString() : "").append("\n");
        }

        return sb.toString();
    }

    /**
     * Escape caratteri speciali per CSV
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Se contiene ; o " o newline, racchiudi tra virgolette
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public Response validaContoDaInserire(@Valid it.csi.accrual.accrualsbsrv.dto.ValidaContoRequest body, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            LOG.info("Ricevuta richiesta validaContoDaInserire per codice_conto: {}", body.getCodiceConto());

            if (body == null || body.getCodiceConto() == null || body.getCodiceConto().isEmpty()) {
                Error error = new Error();
                error.setStatus("400");
                error.setCode("WEB_ERR_0002");
                error.setTitle("Dato obbligatorio non valorizzato");
                error.setDetail("codice_conto è obbligatorio");
                return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
            }

            pdcService.validaContoDaInserire(body.getCodiceConto());

            LOG.info("Conto {} verificato con successo", body.getCodiceConto());
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            LOG.warn("Validazione conto fallita: {}", msg);

            String errorCode = "WEB_ERR_0001";
            String title = "Errore imprevisto";
            String status = "500";

            if (msg != null && msg.startsWith("WEB_ERR_")) {
                int idx = msg.indexOf(" - ");
                if (idx > 0) {
                    errorCode = msg.substring(0, idx);
                    title = msg.substring(idx + 3);
                } else {
                    errorCode = msg;
                    title = msg;
                }

                if ("WEB_ERR_021".equals(errorCode) || "WEB_ERR_022".equals(errorCode) || "WEB_ERR_028".equals(errorCode)) {
                    status = "400";
                }
            }

            Error error = new Error();
            error.setStatus(status);
            error.setCode(errorCode);
            error.setTitle(title);
            error.setDetail(msg);
            return Response.status(status.equals("400") ? Response.Status.BAD_REQUEST : Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (Exception e) {
            LOG.error("Errore imprevisto nella validazione conto", e);
            Error error = new Error();
            error.setStatus("500");
            error.setCode("WEB_ERR_0001");
            error.setTitle("Errore imprevisto");
            error.setDetail(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}