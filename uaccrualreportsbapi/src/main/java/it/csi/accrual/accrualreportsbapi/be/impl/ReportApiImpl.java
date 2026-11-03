/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.be.impl;

import it.csi.accrual.accrualreportsbapi.be.ReportApi;
import it.csi.accrual.accrualreportsbapi.dto.*;
import it.csi.accrual.accrualreportsbapi.service.AsyncReportService;
import it.csi.accrual.accrualreportsbapi.service.RenderService;
import it.csi.accrual.accrualreportsbapi.service.ReportInfoService;
import it.csi.accrual.accrualreportsbapi.exception.*;
import it.csi.accrual.accrualreportsbapi.validator.ReportFileValidator;
import it.csi.accrual.accrualreportsbapi.util.ParameterParser;
import it.csi.accrual.accrualcommonsrv.be.util.EnteContextHolder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Path("/report")
@Slf4j
public class ReportApiImpl implements ReportApi {

    @Autowired
    private RenderService renderService;

    @Autowired
    private AsyncReportService asyncReportService;

    @Autowired
    private ReportInfoService reportInfoService;

    @Autowired
    private ReportFileValidator fileValidator;

    @Autowired
    private ParameterParser parameterParser;

    @Value("${birt.reports.directory:./reports}")
    private String reportsDirectory;

    @Override
    public Response getReportInfo(ReportInfoRequest request,
                                   SecurityContext securityContext,
                                   HttpHeaders httpHeaders,
                                   HttpServletRequest httpRequest) {
        log.info("========== RICHIESTA METADATA REPORT RICEVUTA ==========");
        log.info("Request: rptdesign={}", request.getRptdesign());

        try {
            // Chiama il service per estrarre i metadata
            ReportInfo info = reportInfoService.getReportInfo(request.getRptdesign());

            log.info("Metadata estratti con successo: {} parametri trovati",
                info.getParametri() != null ? info.getParametri().size() : 0);

            return Response.ok(info).build();

        } catch (IllegalArgumentException e) {
            log.error("Template non trovato: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Template non trovato: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Errore nell'estrazione dei metadata: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nell'estrazione dei metadata: " + e.getMessage())
                    .build();
        } finally {
            log.info("========== FINE RICHIESTA METADATA REPORT ==========");
        }
    }

    @Override
    public Response reportGeneratePost(ReportGenerateBody request,
                                       SecurityContext securityContext,
                                       HttpHeaders httpHeaders,
                                       HttpServletRequest httpRequest) {
        log.info("========== RICHIESTA GENERAZIONE SINCRONA RICEVUTA ==========");
        log.info("Headers: {}", httpHeaders.getRequestHeaders());
        log.info("Request: rptdesign={}, rptconfig={}, format={}",
            request.getRptdesign(), request.getRptconfig(), request.getFormat());
        log.info("Reports directory configurata: {}", reportsDirectory);

        try {
            // Costruisci i percorsi completi dei file usando Paths.get() per gestire correttamente i separatori
            String rptdesignPath = Paths.get(reportsDirectory, request.getRptdesign()).toString();
            String rptconfigPath = request.getRptconfig() != null
                ? Paths.get(reportsDirectory, request.getRptconfig()).toString()
                : null;

            log.info("rptdesignPath: {}", rptdesignPath);
            log.info("rptconfigPath: {}", rptconfigPath != null ? rptconfigPath : "NON SPECIFICATO (opzionale)");

            // Verifica che i file esistano
            File rptdesignFile = new File(rptdesignPath);
            File rptconfigFile = rptconfigPath != null ? new File(rptconfigPath) : null;

            if (!rptdesignFile.exists()) {
                log.error("File rptdesign non trovato: {}", request.getRptdesign());
                log.warn("Reports directory: {}", reportsDirectory);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Report design file not found: " + request.getRptdesign())
                        .build();
            }

            if (rptconfigPath != null && !rptconfigFile.exists()) {
                log.error("File rptconfig non trovato: {}", request.getRptconfig());
                log.warn("Reports directory: {}", reportsDirectory);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Report config file not found: " + request.getRptconfig())
                        .build();
            }

            // Valida il formato
            log.info("Validazione formato: {}", request.getFormat());
            fileValidator.validateFormat(request.getFormat());

            // Parsa i parametri se presenti
            log.info("Parsing parametri...");
            Map<String, Object> paramMap = parameterParser.parseParameters(
                request.getParameters() != null ?
                new ObjectMapper().writeValueAsString(request.getParameters()) : null
            );
            log.info("Parametri parsati: {}", paramMap);

            // Auto-inietta ente_proprietario dal contesto JWT (se non già presente)
            autoInjectEnteProprietario(paramMap);

            // Genera il report passando i percorsi dei file
            log.info("Chiamata a renderService.generateReport()...");
            byte[] reportContent = renderService.generateReport(
                rptdesignPath,
                rptconfigPath,
                request.getFormat(),
                paramMap
            );

            log.info("Report generato: {} bytes", reportContent.length);

            // Determina il content type in base al formato
            String contentType = getContentType(request.getFormat());
            log.info("Content-Type: {}", contentType);

            return Response.ok(reportContent)
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"report." + request.getFormat() + "\"")
                    .build();

        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nella serializzazione JSON: " + e.getMessage())
                    .build();
        } catch (InvalidFileException | ReportGenerationException e) {
            log.error("ReportGenerationException/InvalidFileException: {}", e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Exception imprevista: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore imprevisto: " + e.getMessage())
                    .build();
        } finally {
            log.info("========== FINE RICHIESTA GENERAZIONE SINCRONA ==========");
        }
    }

    @Override
    public Response reportGenerateAsyncPost(ReportGenerateasyncBody request,
                                            SecurityContext securityContext,
                                            HttpHeaders httpHeaders,
                                            HttpServletRequest httpRequest) {
        log.info("========== RICHIESTA GENERAZIONE ASINCRONA RICEVUTA ==========");
        log.info("Request: rptdesign={}, rptconfig={}, format={}",
            request.getRptdesign(), request.getRptconfig(), request.getFormat());
        log.info("Reports directory configurata: {}", reportsDirectory);

        try {
            // Costruisci i percorsi completi dei file usando Paths.get() per gestire correttamente i separatori
            String rptdesignPath = Paths.get(reportsDirectory, request.getRptdesign()).toString();
            String rptconfigPath = request.getRptconfig() != null
                ? Paths.get(reportsDirectory, request.getRptconfig()).toString()
                : null;

            log.info("rptdesignPath: {}", rptdesignPath);
            log.info("rptconfigPath: {}", rptconfigPath != null ? rptconfigPath : "NON SPECIFICATO (opzionale)");

            // Verifica che i file esistano
            File rptdesignFile = new File(rptdesignPath);
            File rptconfigFile = rptconfigPath != null ? new File(rptconfigPath) : null;

            if (!rptdesignFile.exists()) {
                log.error("File rptdesign non trovato: {}", request.getRptdesign());
                log.warn("Reports directory: {}", reportsDirectory);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Report design file not found: " + request.getRptdesign())
                        .build();
            }

            if (rptconfigPath != null && !rptconfigFile.exists()) {
                log.error("File rptconfig non trovato: {}", request.getRptconfig());
                log.warn("Reports directory: {}", reportsDirectory);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Report config file not found: " + request.getRptconfig())
                        .build();
            }

            // Valida il formato
            fileValidator.validateFormat(request.getFormat());

            // Leggi i contenuti dei file per passarli al servizio asincrono
            byte[] rptdesignContent = Files.readAllBytes(rptdesignFile.toPath());
            byte[] rptconfigContent = rptconfigPath != null ? Files.readAllBytes(rptconfigFile.toPath()) : null;

            // Parsa i parametri se presenti
            Map<String, Object> paramMap = parameterParser.parseParameters(
                request.getParameters() != null ?
                new ObjectMapper().writeValueAsString(request.getParameters()) : null
            );

            // Auto-inietta ente_proprietario dal contesto JWT (se non già presente)
            autoInjectEnteProprietario(paramMap);

            // Crea la richiesta asincrona
            // Se rptconfig non è specificato, passo null (ora il DB lo permette)
            AsyncReportResponse response = asyncReportService.createAsyncReport(
                request.getFormat(),
                rptdesignContent,
                request.getRptdesign(),
                rptconfigContent,
                request.getRptconfig(),
                serializeParameters(paramMap)
            );

            return Response.status(Response.Status.ACCEPTED)
                    .entity(response)
                    .build();

        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nella lettura dei file: " + e.getMessage())
                    .build();
        } catch (InvalidFileException e) {
            log.error("InvalidFileException: {}", e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (QueueFullException e) {
            log.error("QueueFullException: {}", e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Exception imprevista: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore imprevisto: " + e.getMessage())
                    .build();
        } finally {
            log.info("========== FINE RICHIESTA GENERAZIONE ASINCRONA ==========");
        }
    }

    @Override
    public Response reportGenerateAsyncReportIdGet(UUID reportId, SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        try {
            AsyncReportStatus status = asyncReportService.getReportStatus(reportId);
            return Response.ok(status).build();
        } catch (ReportNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore imprevisto: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response downloadAsyncReport(UUID reportId, SecurityContext securityContext, HttpHeaders httpHeaders, HttpServletRequest httpRequest) {
        try {
            it.csi.accrual.accrualreportsbapi.service.AsyncReportService.ReportDownloadResult result =
                asyncReportService.downloadReport(reportId);

            String format = result.format();
            String contentType = getContentType(format);

            return Response.ok(result.content())
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + reportId + "." + format + "\"")
                    .build();
        } catch (ReportNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore imprevisto: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response searchAsyncReports(AsyncReportSearchRequest request,
                                        SecurityContext securityContext,
                                        HttpHeaders httpHeaders,
                                        HttpServletRequest httpRequest) {
        log.info("========== RICHIESTA RICERCA REPORT ASINCRONI RICEVUTA ==========");
        log.info("Request filters: data_elaborazione_da={}, data_elaborazione_a={}, stato={}",
                request.getDataElaborazioneDa(), request.getDataElaborazioneA(), request.getStato());
        log.info("Pagination: pagina={}, dimensione_pagina={}, ordinamento={}, direzione={}",
                request.getPagina(), request.getDimensionePagina(), request.getOrdinamento(), request.getDirezioneOrdinamento());

        try {
            // Recuperare ente_id dal contesto (impostato da EnteContextFilter)
            Long enteId = EnteContextHolder.getCurrentEnte();
            if (enteId == null) {
                log.error("Ente ID non presente nel contesto");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Ente ID not found in context")
                        .build();
            }

            log.info("Ente ID: {}", enteId);

            // Chiamare il service per la ricerca
            AsyncReportSearchResponse response = asyncReportService.searchReports(request, enteId);

            log.info("Ricerca completata - Totale elementi: {}, Pagina: {}/{}",
                    response.getTotaleElementi(), response.getPaginaCorrente() + 1, response.getTotalePagine());

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            log.error("Parametri di ricerca non validi: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Parametri di ricerca non validi: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Errore nella ricerca dei report: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nella ricerca dei report: " + e.getMessage())
                    .build();
        } finally {
            log.info("========== FINE RICHIESTA RICERCA REPORT ASINCRONI ==========");
        }
    }

    @Override
    public Response getReportDetail(UUID reportId,
                                     SecurityContext securityContext,
                                     HttpHeaders httpHeaders,
                                     HttpServletRequest httpRequest) {
        log.info("========== RICHIESTA DETTAGLIO REPORT RICEVUTA ==========");
        log.info("ReportId: {}", reportId);

        try {
            // Recuperare ente_id dal contesto
            Long enteId = EnteContextHolder.getCurrentEnte();
            if (enteId == null) {
                log.error("Ente ID non presente nel contesto");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Ente ID not found in context")
                        .build();
            }

            log.info("Ente ID: {}", enteId);

            // Chiamare il service per ottenere il dettaglio
            AsyncReportDetail detail = asyncReportService.getReportDetail(reportId, enteId);

            log.info("Dettaglio report recuperato - Stato: {}, Formato: {}",
                    detail.getStato(), detail.getFormato());

            return Response.ok(detail).build();

        } catch (ReportNotFoundException e) {
            log.error("Report non trovato: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        } catch (ForbiddenOperationException e) {
            log.error("Accesso non autorizzato al report: {}", e.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Errore nel recupero del dettaglio report: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nel recupero del dettaglio report: " + e.getMessage())
                    .build();
        } finally {
            log.info("========== FINE RICHIESTA DETTAGLIO REPORT ==========");
        }
    }

    /**
     * Auto-inietta l'ente nei parametri del report.
     * Legge l'ente dal JWT tramite EnteContextHolder e lo imposta come parametro BIRT "ente".
     * Se il parametro è già presente nella richiesta, viene sovrascritto con il valore dal JWT
     * per garantire che l'utente non possa manipolarlo.
     *
     * @param paramMap Mappa dei parametri del report (modificata in-place)
     */
    private void autoInjectEnteProprietario(Map<String, Object> paramMap) {
        Long enteId = EnteContextHolder.getCurrentEnte();
        if (enteId != null) {
            Object existingValue = paramMap.get("ente");
            // BIRT si aspetta Integer, non Long
            Integer enteInt = enteId.intValue();
            if (existingValue != null) {
                log.warn("Parametro 'ente' già presente nei parametri (valore: {}), sovrascritto con ente_id dal JWT: {}",
                        existingValue, enteInt);
            } else {
                log.info("Auto-iniezione parametro 'ente': {}", enteInt);
            }
            paramMap.put("ente", enteInt);
        } else {
            log.error("Impossibile iniettare parametro 'ente': ente_id non presente nel contesto JWT");
        }
    }

    /**
     * Serializza i parametri in JSON convertendo java.sql.Date in stringhe ISO (yyyy-MM-dd)
     * per evitare che vengano salvati come timestamp numerici.
     */
    private String serializeParameters(Map<String, Object> paramMap) throws IOException {
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }
        Map<String, Object> serializable = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof java.sql.Date) {
                serializable.put(entry.getKey(), sdf.format((java.sql.Date) value));
            } else {
                serializable.put(entry.getKey(), value);
            }
        }
        return new ObjectMapper().writeValueAsString(serializable);
    }

    // Metodo per determinare il content type in base al formato
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "html":
                return "text/html";
            case "csv":
                return "text/csv";
            default:
                return "application/octet-stream";
        }
    }

    @GET
    @Path("/debug-config")
    public Response debugConfig() {
        String debugInfo = String.format(
            "Reports Directory: %s\nDatasource URL: %s\nDatasource User: %s",
            reportsDirectory,
            renderService.getDatasourceUrl(),
            renderService.getDatasourceUsername()
        );
        return Response.ok(debugInfo).build();
    }
}