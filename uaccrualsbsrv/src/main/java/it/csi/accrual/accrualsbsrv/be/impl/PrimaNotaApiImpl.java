/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.csi.accrual.accrualsbsrv.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import it.csi.accrual.accrualsbsrv.be.exception.ConfirmationRequiredException;
import it.csi.accrual.accrualsbsrv.be.exception.NotFoundException;
import it.csi.accrual.accrualsbsrv.be.exception.mapper.ConfirmationRequiredExceptionMapper;
import it.csi.accrual.accrualsbsrv.be.service.PrimaNotaService;
import it.csi.accrual.accrualsbsrv.be.PrimanotaApi;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;


@Component
public class PrimaNotaApiImpl implements PrimanotaApi {

    private static final Logger log = LoggerFactory.getLogger(PrimaNotaApiImpl.class);

    @Autowired
    private PrimaNotaService primaNotaService;

    @Autowired
    private SecurityContextHelper securityContextHelper;

    @Override
    public Response exportPrimaNotaList(PrimaNotaSearchFilterDTO body, @QueryParam("sort") String sort, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            log.info("Richiesta export PrimaNota - filter: {}, sort: {}", body, sort);

            List<PrimaNotaSearchResultDTO> list = primaNotaService.exportPrimaNotaList(body, sort);

            // Genera CSV (riutilizzando la logica di escape/generazione simile a Pdc)
            StringBuilder sb = new StringBuilder();
            sb.append("Numero Provvisorio;Data Reg. Provvisoria;Numero Definitivo;Data Reg. Definitiva;Tipo;Fonte;Causale;Stato;Dare;Avere;Descrizione\n");

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (PrimaNotaSearchResultDTO dto : list) {
                sb.append(escapeCsv(dto.getNumeroProvvisorio() != null ? dto.getNumeroProvvisorio().toString() : "")).append(";");

                String dataRegProvvisoria = "";
                if (dto.getDataRegProvvisoria() != null) {
                    dataRegProvvisoria = dto.getDataRegProvvisoria().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .format(formatter);
                }
                sb.append(escapeCsv(dataRegProvvisoria)).append(";");

                sb.append(escapeCsv(dto.getNumeroDefinitivo() != null ? dto.getNumeroDefinitivo().toString() : "")).append(";");

                String dataRegDefinitiva = "";
                if (dto.getDataRegDefinitiva() != null) {
                    dataRegDefinitiva = dto.getDataRegDefinitiva().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(formatter);
                }
                sb.append(escapeCsv(dataRegDefinitiva)).append(";");

                sb.append(escapeCsv(dto.getTipo())).append(";");
                sb.append(escapeCsv(dto.getFonte())).append(";");
                sb.append(escapeCsv(dto.getCausale())).append(";");
                sb.append(escapeCsv(dto.getStato())).append(";");
                sb.append(escapeCsv(dto.getTotaleDare() != null ? String.format("%.2f", dto.getTotaleDare()) : "0,00")).append(";");
                sb.append(escapeCsv(dto.getTotaleAvere() != null ? String.format("%.2f", dto.getTotaleAvere()) : "0,00")).append(";");
                sb.append(escapeCsv(dto.getDescrizione())).append("\n");
            }

            String filename = "primanota_export_" + java.time.LocalDate.now() + ".csv";

            return Response.ok()
                    .entity(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    .type("text/csv;charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();

        } catch (Exception e) {
            log.error("Errore durante l'export CSV Prima Nota: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createError("WEB_ERR_0001", "Errore durante l'esportazione: " + e.getMessage()))
                .build();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public Response checkIncompletePrimeNote(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        boolean hasIncomplete = primaNotaService.checkIncompletePrimeNote();
        return Response.ok().entity(java.util.Map.of("hasIncomplete", hasIncomplete)).build();
    }

    @Override
    public Response getIncompletePrimeNote(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            List<PrimaNotaSearchResultDTO> result = primaNotaService.getIncompletePrimeNote();
            return Response.ok().entity(result).build();
        } catch (Exception e) {
            log.error("Errore durante il recupero delle prime note non quadrate", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(java.util.Map.of("codice", "WEB_ERR_0001", "messaggio", "Si è verificato un errore imprevisto: " + e.getMessage()))
                .build();
        }
    }

    @Override
    public Response getCausaliPrimaNota(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        List<it.csi.accrual.accrualsbsrv.dto.CausalePrimaNota> causali = primaNotaService.getCausaliPrimaNota();
        return Response.ok().entity(causali).build();
    }

    @Override
    public Response getFontiPrimaNota(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        List<it.csi.accrual.accrualsbsrv.dto.FontePrimaNota> fonti = primaNotaService.getFontiPrimaNota();
        return Response.ok().entity(fonti).build();
    }

    @Override
    public Response getPrimaNotaByAnnoAndNumero(@jakarta.ws.rs.PathParam("anno") Integer anno, @jakarta.ws.rs.PathParam("numero") Integer numero, String fields, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        PrimaNotaDetailDTO result = primaNotaService.getPrimaNotaByAnnoAndNumero(anno, numero);
        return Response.ok().entity(result).build();
    }

    @Override
    public Response getStatiPrimaNota(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        List<it.csi.accrual.accrualsbsrv.dto.StatoPrimaNota> stati = primaNotaService.getStatiPrimaNota();
        return Response.ok().entity(stati).build();
    }

    @Override
    public Response getTipiPrimaNota(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        List<it.csi.accrual.accrualsbsrv.dto.TipoPrimaNota> tipi = primaNotaService.getTipiPrimaNota();
        return Response.ok().entity(tipi).build();
    }

    @Override
    public Response searchPrimeNote(PrimaNotaSearchFilterDTO body, String sort, Integer limit, Integer offset, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        PagePrimaNotaSearchResultDTO result = primaNotaService.searchPrimeNote(body, limit, offset, sort);
        return Response.ok().entity(result).build();
    }

    @Override
    public Response getContiPrimaNota(@jakarta.ws.rs.PathParam("anno") Integer anno, @jakarta.ws.rs.PathParam("numero") Integer numero, Integer limit, Integer offset, @jakarta.ws.rs.QueryParam("codiceConto") String codiceConto, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        PagedContoCollegatoDTO result = primaNotaService.getContiPrimaNota(anno, numero, limit, offset, codiceConto);
        return Response.ok().entity(result).build();
    }

    @Override
    public Response getTotaliContiPrimaNota(@jakarta.ws.rs.PathParam("anno") Integer anno, @jakarta.ws.rs.PathParam("numero") Integer numero, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        ContiTotaliDTO result = primaNotaService.getTotaliContiPrimaNota(anno, numero);
        return Response.ok().entity(result).build();
    }

    @Override
    public Response getAttributiPrimaNota(@jakarta.ws.rs.PathParam("anno") Integer anno, @jakarta.ws.rs.PathParam("numero") Integer numero, Integer limit, Integer offset, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        PagedAttributoCollegatoDTO result = primaNotaService.getAttributiPrimaNota(anno, numero, limit, offset);
        return Response.ok().entity(result).build();
    }

    @Override
    public Response getPrimeNoteCollegate(@jakarta.ws.rs.PathParam("anno") Integer anno, @jakarta.ws.rs.PathParam("numero") Integer numero, Integer limit, Integer offset, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        PagedPrimaNotaCollegataDTO result = primaNotaService.getPrimeNoteCollegate(anno, numero, limit, offset);
        return Response.ok().entity(result).build();
    }

    @Override
    public Response annullaPrimaNota(@jakarta.ws.rs.PathParam("anno") Integer anno, @jakarta.ws.rs.PathParam("numero") Integer numero, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            // Recupera l'utente corrente dagli header o dal contesto di sicurezza
            String utenteCorrente = getUtenteCorrente(securityContext);

            // Esegue l'annullamento
            primaNotaService.annullaPrimaNota(anno, numero, utenteCorrente);

            // Restituisce messaggio di successo
            return Response.ok()
                .entity(java.util.Map.of(
                    "messaggio",
                    "WEB_OK_0004"
                ))
                .build();
        } catch (it.csi.accrual.accrualsbsrv.be.exception.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(createError("WEB_ERR_0001", e.getMessage()))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(createError("WEB_ERR_0002", e.getMessage()))
                .build();
        } catch (Exception e) {
            log.error("Errore durante l'annullamento della prima nota {}/{}", anno, numero, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createError("WEB_ERR_0001", "Si è verificato un errore imprevisto: " + e.getMessage()))
                .build();
        }
    }

    @Override
    public Response riapriPrimaNota(@jakarta.ws.rs.PathParam("anno") Integer anno, @jakarta.ws.rs.PathParam("numero") Integer numero, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            // Recupera l'utente corrente dagli header o dal contesto di sicurezza
            String utenteCorrente = getUtenteCorrente(securityContext);

            // Esegue la riapertura
            primaNotaService.riapriPrimaNota(anno, numero, utenteCorrente);

            // Restituisce messaggio di successo
            return Response.ok()
                .entity(java.util.Map.of(
                    "messaggio",
                    "WEB_OK_0004"
                ))
                .build();
        } catch (it.csi.accrual.accrualsbsrv.be.exception.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(createError("WEB_ERR_0001", e.getMessage()))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(createError("WEB_ERR_0002", e.getMessage()))
                .build();
        } catch (Exception e) {
            log.error("Errore durante la riapertura della prima nota {}/{}", anno, numero, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createError("WEB_ERR_0001", "Si è verificato un errore imprevisto: " + e.getMessage()))
                .build();
        }
    }

    // Metodo helper per ottenere l'utente corrente
    private String getUtenteCorrente(SecurityContext securityContext) {
        // Usa solo l'utente del security context (popolato da JwtAuthenticationFilter o DevAuthFilter)
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        // Se l'utente non è autenticato, lancia eccezione
        // Questo non dovrebbe mai accadere perché i filter bloccano le richieste non autenticate
        throw new IllegalStateException("Utente non autenticato. Richiesta non autorizzata.");
    }

    // Metodo helper per creare un oggetto Error
    private it.csi.accrual.accrualsbsrv.dto.Error createError(String codice, String dettaglio) {
        it.csi.accrual.accrualsbsrv.dto.Error error = new it.csi.accrual.accrualsbsrv.dto.Error();
        error.setCode(codice);
        error.setTitle("Errore");
        error.setDetail(dettaglio);
        return error;
    }

    @Override
    public Response inserisciPrimaNota(PrimaNotaCreateUpdateDTO dto, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            String utenteCorrente = getUtenteCorrente(securityContext);
            PrimaNotaDetailDTO result = primaNotaService.inserisciPrimaNota(dto, utenteCorrente);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (IllegalArgumentException e) {
            log.error("Errore di validazione inserimento prima nota", e);
            String messaggio = e.getMessage();
            String codice = messaggio.startsWith("WEB_ERR_") ? messaggio.split(" - ")[0] : "WEB_ERR_0001";
            return Response.status(Response.Status.BAD_REQUEST).entity(createError(codice, messaggio)).build();
        } catch (NotFoundException e) {
            log.error("Entità non trovata durante inserimento prima nota", e);
            return Response.status(Response.Status.NOT_FOUND).entity(createError("WEB_ERR_0004", e.getMessage())).build();
        } catch (Exception e) {
            log.error("Errore imprevisto durante inserimento prima nota", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createError("WEB_ERR_0001", "Si è verificato un errore imprevisto: " + e.getMessage())).build();
        }
    }

    @Override
    public Response aggiornaPrimaNota(PrimaNotaCreateUpdateDTO dto, Integer anno, Integer numero, @Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
        try {
            String utenteCorrente = getUtenteCorrente(securityContext);
            PrimaNotaDetailDTO result = primaNotaService.aggiornaPrimaNota(anno, numero, dto, utenteCorrente);
            return Response.ok().entity(result).build();
        } catch (IllegalArgumentException e) {
            log.error("Errore di validazione aggiornamento prima nota", e);
            String messaggio = e.getMessage();
            String codice = messaggio.startsWith("WEB_ERR_") ? messaggio.split(" - ")[0] : "WEB_ERR_0001";
            return Response.status(Response.Status.BAD_REQUEST).entity(createError(codice, messaggio)).build();
        } catch (NotFoundException e) {
            log.error("Prima nota non trovata durante aggiornamento", e);
            return Response.status(Response.Status.NOT_FOUND).entity(createError("WEB_ERR_0004", e.getMessage())).build();
        } catch (Exception e) {
            log.error("Errore imprevisto durante aggiornamento prima nota", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createError("WEB_ERR_0001", "Si è verificato un errore imprevisto: " + e.getMessage())).build();
        }
    }

    @Override
    public Response validaPrimaNota(@jakarta.ws.rs.PathParam("anno") Integer anno,
                                    @jakarta.ws.rs.PathParam("numero") Integer numero,
                                    @QueryParam("mantieniNumeroDefinitivo") Boolean mantieniNumeroDefinitivo,
                                    @QueryParam("confermato") Boolean confermato,
                                    @Context SecurityContext securityContext,
                                    @Context HttpHeaders httpHeaders,
                                    @Context HttpServletRequest httpRequest) {
        try {
            String utenteCorrente = getUtenteCorrente(securityContext);

            // Chiama il servizio con la scelta dell'utente (null se prima chiamata)
            Map<String, String> result = primaNotaService.validaPrimaNota(anno, numero, utenteCorrente, mantieniNumeroDefinitivo, confermato);

            return Response.ok()
                .entity(java.util.Map.of(
                    "messaggio", result.get("messaggio"),
                    "dettaglio", result.get("dettaglio")
                ))
                .build();
        } catch (NotFoundException e) {
            log.error("Prima nota {}/{} non trovata durante validazione", anno, numero, e);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(createError("WEB_ERR_0001", e.getMessage()))
                .build();
        } catch (IllegalArgumentException e) {
            log.error("Prima nota {}/{} non può essere validata: {}", anno, numero, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(createError("WEB_ERR_0002", e.getMessage()))
                .build();
        } catch (ConfirmationRequiredException e) {
            // Delega al mapper per costruire la risposta 409 Conflict
            log.warn("Richiesta conferma per validazione prima nota {}/{}: {}", anno, numero, e.getConfirmationMessage());
            ConfirmationRequiredExceptionMapper mapper = new ConfirmationRequiredExceptionMapper();
            return mapper.toResponse(e);
        } catch (Exception e) {
            log.error("Errore durante la validazione della prima nota {}/{}", anno, numero, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createError("WEB_ERR_0001", "Si è verificato un errore imprevisto: " + e.getMessage()))
                .build();
        }
    }

    @Override
    public Response validaPrimeNoteMassiva(List<PrimaNotaIdDTO> body,
                                           @QueryParam("mantieniNumeri") Boolean mantieniNumeri,
                                           @QueryParam("confermato") Boolean confermato,
                                           @Context SecurityContext securityContext,
                                           @Context HttpHeaders httpHeaders,
                                           @Context HttpServletRequest httpRequest) {
        try {
            String utenteCorrente = getUtenteCorrente(securityContext);

            Map<String, String> result = primaNotaService.validaPrimeNoteMassiva(body, utenteCorrente, mantieniNumeri, confermato);

            return Response.ok()
                .entity(java.util.Map.of(
                    "messaggio", result.get("messaggio"),
                    "dettaglio", result.get("dettaglio"),
                    "errors", result.getOrDefault("errors", "")
                ))
                .build();
        } catch (IllegalArgumentException e) {
            log.error("Errore di validazione durante validazione massiva", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(createError("WEB_ERR_0002", e.getMessage()))
                .build();
        } catch (ConfirmationRequiredException e) {
            // Delega al mapper per costruire la risposta 409 Conflict
            log.warn("Richiesta conferma per validazione massiva prime note: {}", e.getConfirmationMessage());
            ConfirmationRequiredExceptionMapper mapper = new ConfirmationRequiredExceptionMapper();
            return mapper.toResponse(e);
        } catch (Exception e) {
            log.error("Errore durante la validazione massiva delle prime note", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(createError("WEB_ERR_0001", "Si è verificato un errore imprevisto: " + e.getMessage()))
                .build();
        }
    }
}
