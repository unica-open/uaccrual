/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Registry in-memory del progresso di generazione dei report asincroni.
 *
 * Traccia multiple metriche per ogni job:
 * - Pagine completate (da END_PAGE)
 * - Righe lette dal DB (da FETCH_ROW)
 * - Stato del task (STARTED/COMPLETED)
 *
 * I valori vengono aggiornati dal BirtProgressMonitor e letti da
 * AsyncReportService.getReportStatus() per calcolare il progress.
 *
 * La entry viene rimossa automaticamente quando il report termina
 * (COMPLETED o ERROR) tramite remove().
 */
@Slf4j
@Component
public class ProgressRegistry {

    /**
     * Informazioni di progresso per un singolo report.
     * Thread-safe grazie all'uso di tipi atomici.
     */
    public static class ProgressInfo {
        private final AtomicInteger completedPages = new AtomicInteger(0);
        private final AtomicInteger fetchedRows = new AtomicInteger(0);
        private final AtomicBoolean taskStarted = new AtomicBoolean(false);
        private final AtomicBoolean taskCompleted = new AtomicBoolean(false);
        private final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());

        public AtomicInteger getCompletedPages() {
            return completedPages;
        }

        public AtomicInteger getFetchedRows() {
            return fetchedRows;
        }

        public AtomicBoolean isTaskStarted() {
            return taskStarted;
        }

        public AtomicBoolean isTaskCompleted() {
            return taskCompleted;
        }

        /**
         * Aggiorna il timestamp dell'ultimo aggiornamento.
         */
        public void touch() {
            lastUpdateTime.set(System.currentTimeMillis());
        }

        /**
         * Restituisce il tempo trascorso dall'inizio del processing in millisecondi.
         */
        public long getElapsedMillis() {
            return System.currentTimeMillis() - lastUpdateTime.get();
        }
    }

    private final ConcurrentHashMap<UUID, ProgressInfo> registry = new ConcurrentHashMap<>();

    /**
     * Registra un nuovo job con progresso iniziale a 0.
     */
    public void register(UUID reportId) {
        ProgressInfo info = new ProgressInfo();
        registry.put(reportId, info);
        log.debug("ProgressRegistry: registrato reportId={}", reportId);
    }

    /**
     * Segnala l'avvio del task di rendering.
     */
    public void markTaskStarted(UUID reportId) {
        ProgressInfo info = registry.get(reportId);
        if (info != null) {
            info.taskStarted.set(true);
            info.touch();
            log.debug("ProgressRegistry: reportId={} task STARTED", reportId);
        }
    }

    /**
     * Segnala il completamento del task di rendering.
     */
    public void markTaskCompleted(UUID reportId) {
        ProgressInfo info = registry.get(reportId);
        if (info != null) {
            info.taskCompleted.set(true);
            info.touch();
            log.debug("ProgressRegistry: reportId={} task COMPLETED", reportId);
        }
    }

    /**
     * Incrementa il contatore delle pagine completate per il job dato.
     * Chiamato dal monitor BIRT ad ogni END_PAGE.
     */
    public void incrementPage(UUID reportId, int pageNumber) {
        ProgressInfo info = registry.get(reportId);
        if (info != null) {
            info.completedPages.set(pageNumber);
            info.touch();
            log.debug("ProgressRegistry: reportId={} pagina {} completata", reportId, pageNumber);
        }
    }

    /**
     * Aggiorna il contatore delle righe lette dal database.
     * Chiamato dal monitor BIRT ad ogni FETCH_ROW.
     * Per performance, si consiglia di chiamare solo ogni N righe (es. 1000).
     */
    public void updateFetchedRows(UUID reportId, int rowCount) {
        ProgressInfo info = registry.get(reportId);
        if (info != null) {
            info.fetchedRows.set(rowCount);
            info.touch();
            // Log solo ogni 10000 righe per non inondare il log
            if (rowCount % 10000 == 0) {
                log.debug("ProgressRegistry: reportId={} righe lette={}", reportId, rowCount);
            }
        }
    }

    /**
     * Restituisce le informazioni di progresso per un report.
     * Restituisce null se il job non è registrato.
     */
    public ProgressInfo getProgressInfo(UUID reportId) {
        return registry.get(reportId);
    }

    /**
     * Restituisce il numero di pagine completate finora.
     * Restituisce -1 se il job non è registrato.
     * @deprecated usare getProgressInfo() per accesso completo
     */
    @Deprecated
    public int getCompletedPages(UUID reportId) {
        ProgressInfo info = registry.get(reportId);
        return info != null ? info.completedPages.get() : -1;
    }

    /**
     * Rimuove il job dal registry al termine della generazione.
     */
    public void remove(UUID reportId) {
        ProgressInfo removed = registry.remove(reportId);
        if (removed != null) {
            log.debug("ProgressRegistry: rimosso reportId={} (pagine={}, righe={})", 
                    reportId, 
                    removed.completedPages.get(), 
                    removed.fetchedRows.get());
        }
    }
}
