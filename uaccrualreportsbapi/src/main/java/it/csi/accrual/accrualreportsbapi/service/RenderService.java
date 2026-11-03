/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service;

import it.csi.accrual.accrualreportsbapi.exception.ReportGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.SlotHandle;

import org.eclipse.birt.report.engine.api.IProgressMonitor;
import org.mozilla.javascript.Context;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RenderService {

    @Value("${birt.render.temp.dir:${java.io.tmpdir}/birt-processing}")
    private String tempDir;

    @Value("${birt.render.timeout-seconds:180}")
    private int timeoutSeconds;

    @Value("${birt.reports.directory:./reports}")
    private String reportsDirectory;

    @Value("${birt.locale:it_IT}")
    private String birtLocale;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public String getDatasourceUsername() {
        return datasourceUsername;
    }

    public String getDatasourcePassword() {
        return datasourcePassword;
    }

    private final ReportEngine reportEngine;

    public RenderService(ReportEngine reportEngine) {
        this.reportEngine = reportEngine;
    }

    /**
     * Genera un report sincronamente
     *
     * @param rptdesignPath Path al file .rptdesign
     * @param rptconfigPath Path al file .rptconfig
     * @param format Formato output (pdf, xlsx, html, docx)
     * @param parameters Parametri report (opzionali)
     * @return Contenuto file generato come byte array
     * @throws ReportGenerationException se la generazione fallisce
     */
    public byte[] generateReport(
            String rptdesignPath,
            String rptconfigPath,
            String format,
            Map<String, Object> parameters
    ) throws ReportGenerationException {
        return generateReport(rptdesignPath, rptconfigPath, format, parameters, null);
    }

    /**
     * Genera un report sincronamente con supporto al progress monitoring.
     *
     * @param progressCallback callback opzionale chiamato per ogni evento di progresso.
     *                         Passare null per disabilitare il monitoring.
     */
    public byte[] generateReport(
            String rptdesignPath,
            String rptconfigPath,
            String format,
            Map<String, Object> parameters,
            Consumer<ProgressEvent> progressCallback
    ) throws ReportGenerationException {

        long startTime = System.currentTimeMillis();
        String logFormat = format != null ? format.toLowerCase() : "unknown";

        log.info("========== GENERAZIONE REPORT INIZIATA ==========");
        log.info("Format: {}", logFormat);
        log.info("rptdesignPath: {}", rptdesignPath);
        log.info("rptconfigPath: {}", rptconfigPath);
        log.info("Parameters: {}", parameters);
        log.info("Datasource URL: {}", datasourceUrl);
        log.info("Datasource User: {}", datasourceUsername);
        System.out.println("========== GENERAZIONE REPORT INIZIATA ==========");
        System.out.println("Format: " + logFormat);
        System.out.println("rptdesignPath: " + rptdesignPath);
        System.out.println("Parameters: " + parameters);
        System.out.println("Datasource URL: " + datasourceUrl);
        System.out.println("Datasource User: " + datasourceUsername);

        try {
            log.debug("Starting report generation for format: {}", logFormat);

            // Verificare che i file esistano
            Path rptdesignFilePath = Paths.get(rptdesignPath);
            Path rptconfigFilePath = rptconfigPath != null ? Paths.get(rptconfigPath) : null;

            log.info("Verifica esistenza rptdesign: {} - Esiste: {}", rptdesignPath, Files.exists(rptdesignFilePath));
            log.info("Verifica esistenza rptconfig: {} - Esiste: {}", rptconfigPath, rptconfigFilePath != null && Files.exists(rptconfigFilePath));

            if (!Files.exists(rptdesignFilePath)) {
                log.error("File rptdesign NON TROVATO: {}", rptdesignPath);
                throw new ReportGenerationException(
                    String.format("rptdesign file not found: %s", rptdesignPath)
                );
            }

            // rptconfig è opzionale, se specificato verifichiamo che esista
            if (rptconfigPath != null && !Files.exists(rptconfigFilePath)) {
                log.error("File rptconfig NON TROVATO: {}", rptconfigPath);
                throw new ReportGenerationException(
                    String.format("rptconfig file not found: %s", rptconfigPath)
                );
            }

            log.info("Dimensione file rptdesign: {} bytes", Files.size(rptdesignFilePath));
            if (rptconfigPath != null) {
                log.info("Dimensione file rptconfig: {} bytes", Files.size(rptconfigFilePath));
            } else {
                log.info("rptconfig non specificato (opzionale)");
            }

            // Aprire il design di report
            log.info("Apertura report design: {}", rptdesignPath);
            org.eclipse.birt.report.engine.api.IReportRunnable design =
                reportEngine.openReportDesign(rptdesignPath);
            log.info("Report design aperto: {}", design != null ? "SUCCESSO" : "FALLITO");

            if (design == null) {
                log.error("Impossibile aprire il report design: {}", rptdesignPath);
                throw new ReportGenerationException(
                    "Failed to open report design: " + rptdesignPath
                );
            }

            // Modificare direttamente il ReportDesignHandle già caricato in memoria
            // dal motore BIRT, PRIMA di creare il task.
            // Questo bypassa completamente il meccanismo di decrypt delle password cifrate male.
            log.info("Configurazione datasource ODA con credenziali Spring...");
            configureOdaDataSourceOnDesign(design, datasourceUrl, datasourceUsername, datasourcePassword);
            log.info("Datasource ODA configurato con successo");

            log.debug("Report design loaded successfully");

            // Creare un task per eseguire il report
            log.info("Creazione run-and-render task...");
            IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);
            log.info("Task creato: {}", task != null ? "SUCCESSO" : "FALLITO");

            try {
                // Impostare i parametri se presenti
                if (parameters != null && !parameters.isEmpty()) {
                    log.info("Setting {} report parameters", parameters.size());
                    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                        String key = entry.getKey();
                        if (key.endsWith("__display")) {
                            continue;
                        }
                        String displayText = null;
                        Object dt = parameters.get(key + "__display");
                        if (dt instanceof String && !((String) dt).isEmpty()) {
                            displayText = (String) dt;
                        }
                        if (displayText != null) {
                            log.info("  Parameter: {} = {} (display: {})", key, entry.getValue(), displayText);
                            task.setParameter(key, entry.getValue(), displayText);
                        } else {
                            log.info("  Parameter: {} = {} (type: {})", key, entry.getValue(), entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
                            task.setParameterValue(key, entry.getValue());
                        }
                    }
                } else {
                    log.warn("NO PARAMETERS PROVIDED!");
                }

                // Impostare il locale
                java.util.Locale locale = parseLocale(birtLocale);
                log.info("Impostazione locale: {} (da configurazione: {})", locale, birtLocale);
                task.setLocale(locale);

                // Impostare l'app context per le variabili di connessione (per report che usano ${userconn})
                Map<String, Object> appContext = task.getAppContext();
                if (appContext == null) {
                    appContext = new HashMap<>();
                }
                log.info("App context prima dell'impostazione: {}", appContext.isEmpty() ? "VUOTO" : appContext);
                
                // Le credenziali sono già state impostate nel datasource tramite configureOdaDataSourceAtRuntime
                // Aggiungiamo anche le variabili di app context per compatibilità
                appContext.put("userconn", datasourceUrl);
                appContext.put("userdb", datasourceUsername);
                appContext.put("pwddb", datasourcePassword);
                
                task.setAppContext(appContext);
                log.info("App context impostato con variabili di connessione: url={}, user={}", datasourceUrl, datasourceUsername);

                // Creare l'output stream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                log.info("OutputStream creato: {}", outputStream);

                // Impostare le opzioni di rendering in base al formato
                log.info("Creazione render option per formato: {}", logFormat);
                RenderOption renderOption = createRenderOption(format, outputStream);
                log.info("Render option creata: {}", renderOption != null ? "SUCCESSO" : "FALLITO");
                
                task.setRenderOption(renderOption);
                log.info("Render option impostata sul task");

                // Registrare il progress monitor se il callback e' fornito.
                // Tutti gli eventi BIRT vengono tracciati per un monitoraggio completo:
                // - START_TASK/END_TASK: inizio/fine elaborazione
                // - START_PAGE/END_PAGE: pagine generate (se disponibile)
                // - FETCH_ROW: righe lette dal DB (sempre disponibile, usato come fallback)
                // - START_QUERY/END_QUERY: diagnostica query
                if (progressCallback != null) {
                    final int[] lastLoggedRows = {0};
                    task.setProgressMonitor((type, value) -> {
                        ProgressEvent.Type eventType = null;
                        
                        switch (type) {
                            case IProgressMonitor.START_TASK:
                                eventType = ProgressEvent.Type.START_TASK;
                                log.debug("BIRT: task avviato (tipo={})", value);
                                break;
                                
                            case IProgressMonitor.END_TASK:
                                eventType = ProgressEvent.Type.END_TASK;
                                log.info("BIRT: task completato");
                                break;
                                
                            case IProgressMonitor.START_PAGE:
                                eventType = ProgressEvent.Type.START_PAGE;
                                log.debug("BIRT: inizio generazione pagina {}", value);
                                break;
                                
                            case IProgressMonitor.END_PAGE:
                                eventType = ProgressEvent.Type.END_PAGE;
                                log.debug("BIRT: pagina {} completata", value);
                                break;
                                
                            case IProgressMonitor.START_QUERY:
                                eventType = ProgressEvent.Type.START_QUERY;
                                log.trace("BIRT: query avviata (elemento ID={})", value);
                                break;
                                
                            case IProgressMonitor.END_QUERY:
                                eventType = ProgressEvent.Type.END_QUERY;
                                log.trace("BIRT: query completata (elemento ID={})", value);
                                break;
                                
                            case IProgressMonitor.FETCH_ROW:
                                eventType = ProgressEvent.Type.FETCH_ROW;
                                // Log solo ogni 10000 righe per non inondare il log
                                if (value >= lastLoggedRows[0] + 10000) {
                                    log.debug("BIRT: lette {} righe dal DB", value);
                                    lastLoggedRows[0] = value;
                                }
                                break;
                                
                            default:
                                log.trace("BIRT: evento sconosciuto type={}, value={}", type, value);
                                break;
                        }
                        
                        // Notificare il callback se l'evento è significativo
                        if (eventType != null) {
                            progressCallback.accept(new ProgressEvent(eventType, value));
                        }
                    });
                    log.info("IProgressMonitor registrato sul task");
                }

                // Eseguire il rendering con timeout.
                // IMPORTANTE: task.run() e task.close() devono essere chiamati
                // nello STESSO thread. Se close() viene chiamato da un thread diverso,
                // BIRT lancia NullPointerException su CancelManager.deregister()
                // perche DataEngineSession non e inizializzato nel thread chiamante.
                final ByteArrayOutputStream finalOutputStream = outputStream;
                final IRunAndRenderTask finalTask = task;
                final RuntimeException[] renderError = {null};

                Thread renderThread = new Thread(() -> {
                    Context cx = Context.enter();
                    try {
                        cx.initStandardObjects();
                        log.info("Thread rendering: INIZIO task.run()");
                        finalTask.run();
                        log.info("Thread rendering: task.run() COMPLETATO - {} bytes", finalOutputStream.size());
                    } catch (Exception e) {
                        log.error("Thread rendering: ERRORE durante task.run()", e);
                        renderError[0] = new RuntimeException(e);
                    } catch (Error e) {
                        log.error("Thread rendering: ERROR durante task.run()", e);
                        renderError[0] = new RuntimeException(e);
                    } finally {
                        // Chiudiamo il task nello stesso thread in cui e stato eseguito
                        try {
                            finalTask.close();
                            log.info("Thread rendering: task chiuso con successo");
                        } catch (Exception e) {
                            log.warn("Thread rendering: errore chiusura task (ignorato): {}", e.getMessage());
                        }
                        Context.exit();
                    }
                });

                renderThread.start();
                log.info("Thread rendering avviato (timeout: {} secondi)", timeoutSeconds);
                renderThread.join(timeoutSeconds * 1000L);

                if (renderThread.isAlive()) {
                    renderThread.interrupt();
                    throw new ReportGenerationException(
                        String.format("Report generation timeout after %d seconds", timeoutSeconds)
                    );
                }

                if (renderError[0] != null) {
                    throw renderError[0];
                }

                byte[] result = outputStream.toByteArray();
                long duration = System.currentTimeMillis() - startTime;

                log.info("Dimensione risultato: {} bytes, durata: {} ms", result.length, duration);

                if (result.length == 0) {
                    throw new ReportGenerationException(
                        "Report generation produced empty output. Check report design and parameters."
                    );
                }

                log.info("Report generated successfully - Format: {}, Size: {} bytes, Duration: {} ms",
                    logFormat, result.length, duration);

                return result;

            } finally {
                // task.close() e gia chiamato dentro renderThread - niente da fare qui
            }

        } catch (ReportGenerationException e) {
            log.error("ReportGenerationException: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error generating report for format: {}", logFormat, e);
            throw new ReportGenerationException(
                String.format("Failed to generate report: %s", e.getMessage()),
                e
            );
        } finally {
            log.info("========== GENERAZIONE REPORT COMPLETATA ==========");
        }
    }

    /**
     * Crea le opzioni di rendering in base al formato
     */
    private RenderOption createRenderOption(String format, ByteArrayOutputStream outputStream) {
        String lowerFormat = format != null ? format.toLowerCase() : "pdf";

        switch (lowerFormat) {
            case "pdf":
                PDFRenderOption pdfOption = new PDFRenderOption();
                pdfOption.setOutputFormat("pdf");
                pdfOption.setOutputStream(outputStream);
                return pdfOption;

            case "xlsx":
            case "xls":
                EXCELRenderOption excelOption = new EXCELRenderOption();
                excelOption.setOutputFormat("xlsx");
                excelOption.setOutputStream(outputStream);
                return excelOption;

            case "html":
                HTMLRenderOption htmlOption = new HTMLRenderOption();
                htmlOption.setOutputFormat("html");
                htmlOption.setOutputStream(outputStream);
                htmlOption.setEnableInlineStyle(true);
                return htmlOption;

            case "docx":
                // BIRT support per Word tramite plugin/extension
                // Per ora restituiamo PDF come fallback
                log.warn("DOCX format requested but not fully supported, using PDF fallback");
                PDFRenderOption docxFallback = new PDFRenderOption();
                docxFallback.setOutputFormat("pdf");
                docxFallback.setOutputStream(outputStream);
                return docxFallback;

            default:
                // Default a PDF
                PDFRenderOption defaultOption = new PDFRenderOption();
                defaultOption.setOutputFormat("pdf");
                defaultOption.setOutputStream(outputStream);
                return defaultOption;
        }
    }

    /**
     * Ottiene il MIME type per un formato
     */
    public String getMimeType(String format) {
        if (format == null) {
            return "application/octet-stream";
        }
        String lowerFormat = format.toLowerCase();

        switch (lowerFormat) {
            case "pdf":
                return "application/pdf";
            case "xlsx":
            case "xls":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "html":
                return "text/html";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * Ottiene l'estensione file per un formato
     */
    public String getFileExtension(String format) {
        String lowerFormat = format != null ? format.toLowerCase() : "pdf";

        switch (lowerFormat) {
            case "pdf":
                return ".pdf";
            case "xlsx":
                return ".xlsx";
            case "xls":
                return ".xls";
            case "html":
                return ".html";
            case "docx":
                return ".docx";
            default:
                return ".pdf";
        }
    }

    /**
     * Crea la cartella temporanea per BIRT se non esiste
     */
    public void ensureTempDirExists() throws ReportGenerationException {
        try {
            Path tempPath = Paths.get(tempDir);
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
                log.info("Created BIRT temp directory: {}", tempDir);
            }
        } catch (IOException e) {
            throw new ReportGenerationException(
                "Failed to create temp directory: " + tempDir,
                e
            );
        }
    }

    /**
     * Pulisce i file temporanei
     */
    public void cleanupTempFiles(String... filePaths) {
        for (String filePath : filePaths) {
            if (filePath == null || filePath.trim().isEmpty()) {
                continue;
            }
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.debug("Deleted temp file: {}", filePath);
                }
            } catch (IOException e) {
                log.warn("Failed to delete temp file: {}", filePath, e);
            }
        }
    }

    /**
     * Salva un file nel temp directory
     * @return Il percorso del file salvato, o null se content è null
     */
    public String saveTempFile(byte[] content, String filename) throws ReportGenerationException {
        // Se il contenuto è null, restituisci null (file opzionale non specificato)
        if (content == null || content.length == 0) {
            log.debug("Skipping temp file creation for {} - content is null/empty", filename);
            return null;
        }
        
        try {
            ensureTempDirExists();
            Path filePath = Paths.get(tempDir, filename);
            Files.write(filePath, content);
            log.debug("Saved temp file: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new ReportGenerationException(
                "Failed to save temp file: " + filename,
                e
            );
        }
    }

    /**
     * Configura le proprietà del datasource ODA direttamente sul ReportDesignHandle
     * già caricato in memoria dal motore BIRT.
     *
     * Questo approccio bypassa completamente il meccanismo di decrypt di BIRT
     * (SimpleEncryptionHelper), che era la causa dell'errore "Illegal base64 character".
     * Le proprietà vengono scritte in chiaro sul modello in memoria, senza toccare
     * il file .rptdesign su disco e senza che BIRT tenti mai di decifrare la password
     * memorizzata nel file.
     *
     * @param design   Il design già aperto dal ReportEngine
     * @param url      URL JDBC del datasource
     * @param username Username del datasource
     * @param password Password del datasource
     */
    private void configureOdaDataSourceOnDesign(
            org.eclipse.birt.report.engine.api.IReportRunnable design,
            String url, String username, String password) {
        try {
            ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();
            if (designHandle == null) {
                log.warn("getDesignHandle() ha restituito null - impossibile configurare i datasource");
                return;
            }

            SlotHandle dataSources = designHandle.getDataSources();
            if (dataSources == null) {
                log.warn("Nessuno slot datasource trovato nel report design");
                return;
            }

            int count = dataSources.getCount();
            log.info("Trovati {} datasource nel report design", count);

            for (int i = 0; i < count; i++) {
                Object element = dataSources.get(i);

                if (element instanceof OdaDataSourceHandle) {
                    OdaDataSourceHandle dsHandle = (OdaDataSourceHandle) element;
                    String elementName = dsHandle.getName();
                    String extensionID = dsHandle.getExtensionID();

                    log.info("Datasource {}: name={}, extensionID={}", i, elementName, extensionID);

                    try {
                        // Ora che SimpleEncryptionHelper e' stato sostituito con un no-op
                        // in BirtEngineConfig, dsHandle.setProperty() non triggera piu'
                        // il Base64-decode della password esistente.
                        if (url != null && !url.isEmpty()) {
                            dsHandle.setProperty("odaURL", url);
                        }
                        if (username != null && !username.isEmpty()) {
                            dsHandle.setProperty("odaUser", username);
                        }
                        if (password != null && !password.isEmpty()) {
                            dsHandle.setProperty("odaPassword", password);
                        }
                        log.info("Datasource '{}' configurato: URL={}, user={}", elementName, url, username);
                    } catch (Exception e) {
                        log.warn("Impossibile impostare proprietà sul datasource '{}': {}", elementName, e.getMessage());
                    }
                } else {
                    log.debug("Datasource all'indice {} non è OdaDataSourceHandle, ignorato", i);
                }
            }

        } catch (Exception e) {
            log.warn("Errore durante la configurazione in-memory dei datasource ODA: {}", e.getMessage());
            log.debug("Stack trace:", e);
            // Non bloccare l'esecuzione: il report potrebbe comunque andare
        }
    }

    private java.util.Locale parseLocale(String localeStr) {
        if (localeStr == null || localeStr.isEmpty()) {
            return java.util.Locale.ITALY;
        }
        String[] parts = localeStr.split("_");
        if (parts.length == 1) {
            return new java.util.Locale(parts[0]);
        } else if (parts.length == 2) {
            return new java.util.Locale(parts[0], parts[1]);
        } else if (parts.length == 3) {
            return new java.util.Locale(parts[0], parts[1], parts[2]);
        }
        return java.util.Locale.ITALY;
    }
}
