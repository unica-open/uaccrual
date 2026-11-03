/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service;

import it.csi.accrual.accrualreportsbapi.dto.*;
import it.csi.accrual.accrualreportsbapi.entity.AsyncReport;
import it.csi.accrual.accrualreportsbapi.exception.ForbiddenOperationException;
import it.csi.accrual.accrualreportsbapi.exception.QueueFullException;
import it.csi.accrual.accrualreportsbapi.exception.ReportNotFoundException;
import it.csi.accrual.accrualreportsbapi.repository.AsyncReportRepository;
import it.csi.accrual.accrualreportsbapi.specification.AsyncReportSpecification;
import it.csi.accrual.accrualreportsbapi.util.ParameterParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsyncReportService {

    private final AsyncReportRepository reportRepository;
    private final RenderService renderService;
    private final ParameterParser parameterParser;
    private final ProgressRegistry progressRegistry;
    private final ReportInfoService reportInfoService;

    // Thresholds
    private static final long FIVE_MB = 5 * 1024 * 1024;

    public AsyncReportService(
            AsyncReportRepository reportRepository,
            RenderService renderService,
            ParameterParser parameterParser,
            ProgressRegistry progressRegistry,
            ReportInfoService reportInfoService
    ) {
        this.reportRepository = reportRepository;
        this.renderService = renderService;
        this.parameterParser = parameterParser;
        this.progressRegistry = progressRegistry;
        this.reportInfoService = reportInfoService;
    }

    /**
     * Crea un job di report asincrono
     *
     * @param format Formato output (pdf, xlsx, html, docx)
     * @param rptdesignContent Contenuto file rptdesign
     * @param rptdesignFileName Nome file rptdesign
     * @param rptconfigContent Contenuto file rptconfig
     * @param rptconfigFileName Nome file rptconfig
     * @param parameters Parametri report (opzionali)
     * @return Risposta con reportId e status PENDING
     * @throws QueueFullException se la coda è piena
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse createAsyncReport(
            String format,
            byte[] rptdesignContent,
            String rptdesignFileName,
            byte[] rptconfigContent,
            String rptconfigFileName,
            String parameters
    ) throws QueueFullException {

        log.debug("Creating async report - Format: {}, rptdesign: {}, rptconfig: {}",
                format, rptdesignFileName, rptconfigFileName);

        // Creare l'entità report
        AsyncReport asyncReport = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format(format)
                .rptdesignFileName(rptdesignFileName)
                .rptconfigFileName(rptconfigFileName)
                .parameters(parameters)
                .createdAt(LocalDateTime.now())
                .build();

        // Salvare nel database e flushare per commit immediato
        AsyncReport saved = reportRepository.save(asyncReport);
        reportRepository.flush();

        log.info("Async report created with ID: {} - Status: PENDING", saved.getId());

        // Restituire la risposta PRIMA di avviare l'elaborazione asincrona
        // Questo permette alla transazione di essere commitata immediatamente
        it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse response =
            it.csi.accrual.accrualreportsbapi.dto.ReportDtos.AsyncReportResponse.builder()
                .reportId(saved.getId())
                .status(saved.getStatus())
                .createdAt(Date.from(saved.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        // Determinare il lane (FAST o SLOW) in base alla dimensione di rptdesign
        // e avviare l'elaborazione asincrona DOPO aver restituito la risposta
        boolean useFastLane = rptdesignContent.length < FIVE_MB;

        // Usiamo un thread separato per avviare l'elaborazione asincrona
        // Questo evita che il thread HTTP rimanga bloccato
        new Thread(() -> {
            if (useFastLane) {
                log.debug("Submitting report {} to FAST lane", saved.getId());
                processReportFastAsync(saved, rptdesignContent, rptconfigContent);
            } else {
                log.debug("Submitting report {} to SLOW lane", saved.getId());
                processReportSlowAsync(saved, rptdesignContent, rptconfigContent);
            }
        }).start();

        return response;
    }

    /**
     * Elaborazione asincrona FAST lane (< 5 MB)
     * Executor: fastExecutor (5 thread, 100 queue, timeout 5 min)
     */
    @Async("fastExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processReportFastAsync(AsyncReport report, byte[] rptdesignContent, byte[] rptconfigContent) {
        processReportAsync(report, rptdesignContent, rptconfigContent, "FAST");
    }

    /**
     * Elaborazione asincrona SLOW lane (>= 5 MB)
     * Executor: slowExecutor (2 thread, 50 queue, timeout 1h)
     */
    @Async("slowExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processReportSlowAsync(AsyncReport report, byte[] rptdesignContent, byte[] rptconfigContent) {
        processReportAsync(report, rptdesignContent, rptconfigContent, "SLOW");
    }

    /**
     * Logica comune di elaborazione asincrona
     */
    private void processReportAsync(
            AsyncReport report,
            byte[] rptdesignContent,
            byte[] rptconfigContent,
            String lane
    ) {

        String reportId = report.getId().toString();
        long startTime = System.currentTimeMillis();
        String rptdesignPath = null;
        String rptconfigPath = null;

        try {
            log.info("Starting async report processing - Lane: {}, ReportId: {}", lane, reportId);

            // Aggiornare status a PROCESSING
            updateReportStatus(report.getId(), AsyncReport.ReportStatus.PROCESSING, null);

            // Registrare il job nel ProgressRegistry: da questo momento
            // getReportStatus() restituira' il numero di pagine reali invece di 50.
            progressRegistry.register(report.getId());

            // Salvare i file rptdesign e rptconfig nel temp directory
            // rptconfig è opzionale, se rptconfigContent è null il percorso sarà null
            rptdesignPath = renderService.saveTempFile(rptdesignContent, report.getRptdesignFileName());
            rptconfigPath = renderService.saveTempFile(rptconfigContent, report.getRptconfigFileName());

            log.debug("Temp files created - rptdesign: {}, rptconfig: {}", rptdesignPath, rptconfigPath != null ? rptconfigPath : "NON SPECIFICATO");

            // Parsare i parametri se presenti
            Map<String, Object> parameters = parameterParser.parseParameters(report.getParameters());

            log.debug("Report parameters parsed - Count: {}", parameters.size());

            // Generare il report passando il callback di progresso.
            // Tutti gli eventi BIRT vengono tracciati nel ProgressRegistry:
            // - START_TASK: segna l'inizio dell'elaborazione
            // - END_PAGE: aggiorna il numero di pagine completate (prioritario)
            // - FETCH_ROW: aggiorna il numero di righe lette (fallback se non ci sono pagine)
            // - END_TASK: segna il completamento
            byte[] reportContent = renderService.generateReport(
                    rptdesignPath,
                    rptconfigPath,
                    report.getFormat(),
                    parameters,
                    event -> {
                        switch (event.getType()) {
                            case START_TASK:
                                progressRegistry.markTaskStarted(report.getId());
                                break;
                            case END_PAGE:
                                progressRegistry.incrementPage(report.getId(), event.getValue());
                                break;
                            case FETCH_ROW:
                                // Aggiorniamo solo ogni 1000 righe per ridurre overhead
                                if (event.getValue() % 1000 == 0) {
                                    progressRegistry.updateFetchedRows(report.getId(), event.getValue());
                                }
                                break;
                            case END_TASK:
                                progressRegistry.markTaskCompleted(report.getId());
                                break;
                            default:
                                // Altri eventi non richiedono aggiornamento del registry
                                break;
                        }
                    }
            );

            log.info("Report generated successfully - Size: {} bytes, ReportId: {}",
                    reportContent.length, reportId);

            // Salvare il contenuto nel database e aggiornare status
            updateReportWithContent(
                    report.getId(),
                    reportContent,
                    AsyncReport.ReportStatus.COMPLETED
            );

            // Rimuovere dal registry: il progress sara' ora calcolato dallo status COMPLETED -> 100
            progressRegistry.remove(report.getId());

            long duration = System.currentTimeMillis() - startTime;
            log.info("Async report completed successfully - ReportId: {}, Duration: {} ms, Lane: {}",
                    reportId, duration, lane);

        } catch (Exception e) {
            log.error("Error processing async report - ReportId: {}, Lane: {}", reportId, lane, e);

            // Aggiornare status a ERROR
            updateReportStatus(
                    report.getId(),
                    AsyncReport.ReportStatus.ERROR,
                    e.getMessage()
            );

            // Rimuovere dal registry anche in caso di errore
            progressRegistry.remove(report.getId());

        } finally {
            // Pulire i file temporanei
            if (rptdesignPath != null || rptconfigPath != null) {
                renderService.cleanupTempFiles(rptdesignPath, rptconfigPath);
                log.debug("Temp files cleaned up for ReportId: {}", reportId);
            }
        }
    }

    /**
     * Ottiene lo status di un report asincrono
     */
    @Transactional(readOnly = true)
    public it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus getReportStatus(UUID reportId) throws ReportNotFoundException {
        AsyncReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(
                        "Report not found with ID: " + reportId
                ));

        return ReportDtos.AsyncReportStatus.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .progress(calculateProgress(report.getStatus(), report.getId()))
                .downloadUrl(report.getStatus() == AsyncReport.ReportStatus.COMPLETED ?
                    "/api/report/generate-async/" + reportId + "/download" : null)
                .error(report.getStatus() == AsyncReport.ReportStatus.ERROR ?
                    report.getErrorMessage() : null)
                .build();
    }

    /**
     * Scarica il file di un report completato
     */
    @Transactional(readOnly = true)
    public ReportDownloadResult downloadReport(UUID reportId) throws ReportNotFoundException {
        AsyncReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(
                        "Report not found with ID: " + reportId
                ));

        if (report.getFileContent() == null || report.getFileContent().length == 0) {
            throw new ReportNotFoundException(
                    "Report file not ready or not found for ID: " + reportId
            );
        }

        log.info("Downloading report - ReportId: {}, Size: {} bytes, Format: {}", 
                reportId, report.getFileContent().length, report.getFormat());

        return new ReportDownloadResult(report.getFileContent(), report.getFormat());
    }

    /**
     * Ricerca report asincroni con filtri
     *
     * @param request Request di ricerca con filtri
     * @param enteId ID dell'ente per multi-tenancy
     * @return Response paginata con i report trovati
     */
    @Transactional(readOnly = true)
    public AsyncReportSearchResponse searchReports(AsyncReportSearchRequest request, Long enteId) {
        log.info("Searching async reports - EnteId: {}, Filters: {}", enteId, request);

        try {
            // Validare i filtri di ricerca
            validateSearchFilters(request);

            // Convertire le date da Date a LocalDateTime (tipo nativo dell'entity)
            LocalDateTime completedAtDa = null;
            LocalDateTime completedAtA = null;

            if (request.getDataElaborazioneDa() != null) {
                completedAtDa = request.getDataElaborazioneDa()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            if (request.getDataElaborazioneA() != null) {
                // Includere tutto il giorno specificato
                LocalDate dateA = request.getDataElaborazioneA()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                completedAtA = dateA.atTime(LocalTime.MAX);
            }

            // Convertire lo stato da DTO a Entity
            AsyncReport.ReportStatus status = null;
            if (request.getStato() != null) {
                status = AsyncReport.ReportStatus.valueOf(request.getStato().name());
            }

            // Recuperare tipo_report (stringa) e formato (convertire enum in stringa)
            String tipoReport = request.getTipoReport();
            String formato = request.getFormato() != null ? request.getFormato().name() : null;

            // Creare Pageable per la paginazione
            Sort.Direction sortDirection = "ASC".equalsIgnoreCase(request.getDirezioneOrdinamento().name())
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(sortDirection, request.getOrdinamento());
            Pageable pageable = PageRequest.of(request.getPagina(), request.getDimensionePagina(), sort);

            // Creare la specifica con i filtri
            Specification<AsyncReport> spec = AsyncReportSpecification.withFilters(
                    enteId,
                    completedAtDa,
                    completedAtA,
                    status,
                    tipoReport,
                    formato
            );

            log.info("Applying filters - enteId: {}, formato: {}, tipoReport: {}, status: {}", 
                    enteId, formato, tipoReport, status);

            // Chiamare il repository usando JpaSpecificationExecutor
            Page<AsyncReport> reportPage = reportRepository.findAll(spec, pageable);

            log.info("Query results - Total elements: {}", reportPage.getTotalElements());

            // Mappare entity → DTO summary
            List<AsyncReportSummary> summaries = reportPage.getContent().stream()
                    .map(this::mapToSummary)
                    .collect(Collectors.toList());

            // Costruire la response
            AsyncReportSearchResponse response = new AsyncReportSearchResponse();
            response.setReport(summaries);
            response.setTotaleElementi((int) reportPage.getTotalElements());
            response.setPaginaCorrente(reportPage.getNumber());
            response.setDimensionePagina(reportPage.getSize());
            response.setTotalePagine(reportPage.getTotalPages());

            log.info("Search completed - Found {} reports, Page {}/{}",
                    reportPage.getTotalElements(), reportPage.getNumber() + 1, reportPage.getTotalPages());

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Invalid search filters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error searching reports - EnteId: {}", enteId, e);
            throw new RuntimeException("Error searching reports: " + e.getMessage(), e);
        }
    }

    /**
     * Ottiene il dettaglio completo di un report asincrono
     *
     * @param reportId ID del report
     * @param enteId ID dell'ente per multi-tenancy
     * @return Dettaglio completo del report
     * @throws ReportNotFoundException se il report non esiste
     * @throws ForbiddenOperationException se il report non appartiene all'ente
     */
    @Transactional(readOnly = true)
    public AsyncReportDetail getReportDetail(UUID reportId, Long enteId) throws ReportNotFoundException, ForbiddenOperationException {
        log.info("Getting report detail - ReportId: {}, EnteId: {}", reportId, enteId);

        // Verificare l'appartenenza del report all'ente
        verifyReportOwnership(reportId, enteId);

        // Recuperare il report dal database
        AsyncReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(
                        "Report not found with ID: " + reportId
                ));

        // Mappare entity → DTO detail
        AsyncReportDetail detail = mapToDetail(report);

        log.info("Report detail retrieved - ReportId: {}", reportId);

        return detail;
    }

    /**
     * Verifica che un report appartenga a un ente specifico
     *
     * @param reportId ID del report
     * @param enteId ID dell'ente
     * @throws ReportNotFoundException se il report non esiste
     * @throws ForbiddenOperationException se il report non appartiene all'ente
     */
    public void verifyReportOwnership(UUID reportId, Long enteId) throws ReportNotFoundException, ForbiddenOperationException {
        long count = reportRepository.countByIdAndEnteId(reportId, enteId);

        if (count == 0) {
            // Verificare se il report esiste per dare un messaggio di errore appropriato
            Optional<AsyncReport> report = reportRepository.findById(reportId);
            if (report.isPresent()) {
                throw new ForbiddenOperationException(
                        "Access denied to report " + reportId + " for ente " + enteId
                );
            } else {
                throw new ReportNotFoundException(
                        "Report not found with ID: " + reportId
                );
            }
        }
    }

    /**
     * Valida i filtri di ricerca
     *
     * @param request Request di ricerca
     * @throws IllegalArgumentException se i filtri non sono validi
     */
    private void validateSearchFilters(AsyncReportSearchRequest request) {
        if (request.getDataElaborazioneDa() != null && request.getDataElaborazioneA() != null) {
            if (request.getDataElaborazioneDa().after(request.getDataElaborazioneA())) {
                throw new IllegalArgumentException(
                        "data_elaborazione_da must be before or equal to data_elaborazione_a"
                );
            }
        }

        if (request.getPagina() != null && request.getPagina() < 0) {
            throw new IllegalArgumentException("pagina must be >= 0");
        }

        if (request.getDimensionePagina() != null && (request.getDimensionePagina() < 1 || request.getDimensionePagina() > 100)) {
            throw new IllegalArgumentException("dimensione_pagina must be between 1 and 100");
        }
    }

    /**
     * Mappa un'entity AsyncReport in un DTO AsyncReportSummary
     *
     * @param report Entity report
     * @return DTO summary
     */
    private AsyncReportSummary mapToSummary(AsyncReport report) {
        AsyncReportSummary summary = new AsyncReportSummary();
        summary.setReportId(report.getId());
        summary.setRptdesignFileName(report.getRptdesignFileName());
        summary.setFormato(report.getFormat());
        summary.setFileNome(generateFileName(report));
        summary.setStato(AsyncReportSummary.StatoEnum.valueOf(report.getStatus().name()));
        summary.setDataCreazione(Date.from(report.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        
        if (report.getCompletedAt() != null) {
            summary.setDataCompletamento(Date.from(report.getCompletedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        summary.setDimensioneFile(report.getFileSize() != null ? report.getFileSize().intValue() : null);
        summary.setProgressPercentage(report.getProgressPercentage());
        
        return summary;
    }

    /**
     * Mappa un'entity AsyncReport in un DTO AsyncReportDetail
     *
     * @param report Entity report
     * @return DTO detail
     */
    private AsyncReportDetail mapToDetail(AsyncReport report) {
        AsyncReportDetail detail = new AsyncReportDetail();
        
        // Copiare i campi da summary
        detail.setReportId(report.getId());
        detail.setFormato(report.getFormat());
        detail.setFileNome(generateFileName(report));
        detail.setStato(AsyncReportSummary.StatoEnum.valueOf(report.getStatus().name()));
        detail.setDataCreazione(Date.from(report.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        
        if (report.getCompletedAt() != null) {
            detail.setDataCompletamento(Date.from(report.getCompletedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        detail.setDimensioneFile(report.getFileSize() != null ? report.getFileSize().intValue() : null);
        detail.setProgressPercentage(report.getProgressPercentage());
        
        // Campi specifici del detail
        detail.setRptdesignFileName(report.getRptdesignFileName());
        detail.setRptconfigFileName(report.getRptconfigFileName());
        detail.setParametri(report.getParameters());
        detail.setMessaggioErrore(report.getErrorMessage());
        
        if (report.getStartedAt() != null) {
            detail.setDataAvvio(Date.from(report.getStartedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        detail.setVersione(report.getVersion() != null ? report.getVersion().intValue() : null);
        detail.setEnteId(report.getEnteId() != null ? report.getEnteId().intValue() : null);

        // Carica etichette parametri dal template BIRT (solo parametri visibili)
        if (report.getRptdesignFileName() != null) {
            try {
                Map<String, String> labels = reportInfoService.getParameterLabels(report.getRptdesignFileName());
                if (labels != null && !labels.isEmpty()) {
                    detail.setEtichetteParametri(labels);
                }
            } catch (Exception e) {
                log.warn("Impossibile caricare etichette parametri per {}: {}", report.getRptdesignFileName(), e.getMessage());
            }
        }

        return detail;
    }

    /**
     * Genera il nome file del report
     *
     * @param report Entity report
     * @return Nome file
     */
    private String generateFileName(AsyncReport report) {
        if (report.getCompletedAt() != null) {
            return "report_" + report.getCompletedAt().toString().replace("-", "").replace(":", "").replace(" ", "_") + "." + report.getFormat();
        }
        return "report_" + report.getId().toString() + "." + report.getFormat();
    }

    /**
     * Aggiorna lo status del report
     */
    @Transactional
    private void updateReportStatus(
            UUID reportId,
            AsyncReport.ReportStatus status,
            String errorMessage
    ) {
        try {
            Optional<AsyncReport> optional = reportRepository.findById(reportId);
            if (optional.isPresent()) {
                AsyncReport report = optional.get();
                report.setStatus(status);

                if (status == AsyncReport.ReportStatus.PROCESSING) {
                    report.setStartedAt(LocalDateTime.now());
                } else if (status == AsyncReport.ReportStatus.COMPLETED ||
                           status == AsyncReport.ReportStatus.ERROR) {
                    report.setCompletedAt(LocalDateTime.now());
                }

                if (errorMessage != null) {
                    report.setErrorMessage(errorMessage);
                }

                reportRepository.save(report);
                log.debug("Report status updated - ReportId: {}, Status: {}", reportId, status);
            }
        } catch (Exception e) {
            log.error("Failed to update report status - ReportId: {}", reportId, e);
        }
    }

    /**
     * Aggiorna il report con il contenuto generato
     */
    @Transactional
    private void updateReportWithContent(
            UUID reportId,
            byte[] fileContent,
            AsyncReport.ReportStatus status
    ) {
        try {
            Optional<AsyncReport> optional = reportRepository.findById(reportId);
            if (optional.isPresent()) {
                AsyncReport report = optional.get();
                report.setFileContent(fileContent);
                report.setFileSize((long) fileContent.length);
                report.setStatus(status);
                report.setCompletedAt(LocalDateTime.now());

                reportRepository.save(report);
                log.debug("Report content saved - ReportId: {}, FileSize: {}", reportId, fileContent.length);
            }
        } catch (Exception e) {
            log.error("Failed to save report content - ReportId: {}", reportId, e);
        }
    }

    /**
     * Calcola il progress del report usando una strategia multi-evento.
     *
     * Per i job PROCESSING, il progresso viene calcolato con questa priorita':
     *   1. END_PAGE (se disponibile) - piu' significativo per l'utente
     *   2. FETCH_ROW (fallback) - sempre disponibile, usato come proxy
     *   3. START_TASK (minimo) - almeno 1 per indicare attivita' in corso
     *
     * Il valore restituito e' un numero assoluto che cresce durante l'elaborazione.
     * Non e' una percentuale perche' BIRT non conosce il totale a priori.
     *
     * Stati fissi:
     *   PENDING     -> 0
     *   PROCESSING  -> pagine (se disp.) O righe/1000 (se disp.) O 1 (task avviato)
     *   COMPLETED   -> 100
     *   ERROR       -> 0
     */
    private Integer calculateProgress(AsyncReport.ReportStatus status, UUID reportId) {
        switch (status) {
            case PENDING:
                return 0;

            case PROCESSING:
                ProgressRegistry.ProgressInfo info = progressRegistry.getProgressInfo(reportId);

                // Se il registry non ha info (race condition), restituiamo 1
                if (info == null) {
                    return 1;
                }

                // Priorita' 1: Pagine completate (da END_PAGE)
                int pages = info.getCompletedPages().get();
                if (pages > 0) {
                    log.trace("Progress [{}]: usando pagine={}", reportId, pages);
                    return pages;
                }

                // Priorita' 2: Righe lette (da FETCH_ROW) - normalizzate in migliaia
                int rows = info.getFetchedRows().get();
                if (rows > 0) {
                    int progress = Math.max(1, rows / 1000);
                    log.trace("Progress [{}]: usando righe={} -> progress={}", reportId, rows, progress);
                    return progress;
                }

                // Priorita' 3: Task avviato (almeno 1 per indicare attivita' in corso)
                if (info.isTaskStarted().get()) {
                    return 1;
                }

                // Default: attivita' in corso ma nessun dato ancora
                return 1;

            case COMPLETED:
                return 100;

            case ERROR:
                return 0;

            default:
                return 0;
        }
    }

    /**
     * Record wrapper per il risultato del download report
     * Contiene il contenuto del file e il formato per impostare correttamente l'estensione
     */
    public record ReportDownloadResult(byte[] content, String format) {}
}
