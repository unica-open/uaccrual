/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;
import it.csi.accrual.accrualcommonsrv.be.util.AccrualBusinessConstants;
import it.csi.accrual.accrualcommonsrv.security.SecurityContextHelper;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import it.csi.accrual.accrualsbapi.be.exception.ConfirmationRequiredException; // AGGIUNTO
import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualsbapi.dto.ElaboraFlussoResponseDTO;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTFileRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFileStato;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDFileStatoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Servizio per la gestione asincrona dell'elaborazione dei flussi
 * Separato dal servizio principale per consentire elaborazioni in background
 */
@Service
@Transactional
public class AsyncElaboraFlussoService {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncElaboraFlussoService.class);

    @Autowired
    private FlussoServiceImpl flussoService;

    @Autowired
    private AccrualTFileRepository accrualTFileRepository;

    @Autowired
    private AccrualTEnteRepository accrualTEnteRepository;

    @Autowired
    private AccrualDFileStatoRepository accrualDFileStatoRepository;

    @Autowired
    private Pdc118Processor pdc118Processor;

    @Autowired
    private VariazioniInventarialiProcessor variazioniInventarialiProcessor;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificaService notificaService;

    /**
     * Avvia l'elaborazione asincrona di un flusso
     *
     * <p>In caso di errore il Future si completa con {@code null}, non in modo eccezionale: da
     * Spring 6.1 un metodo {@code @Transactional} che restituisce un Future già fallito va in
     * rollback, e con lui lo stato di errore impostato da {@link #terminaFlussoInErrore}. L'esito
     * resta consultabile dallo stato del file, dal log del flusso e dalla notifica.</p>
     *
     * @param id ID del flusso da elaborare
     * @param confirmedOverride
     * @return Future contenente la risposta dell'elaborazione, {@code null} in caso di errore
     */
    @Async("elaborazioneExecutor")
    public Future<ElaboraFlussoResponseDTO> elaboraFlussoAsync(String id, boolean confirmedOverride) throws NotFoundException {
        String codiceEnte = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.info("Avvio elaborazione asincrona del flusso ID: {} per ente: {}", id, codiceEnte);

        try {
            // Recupera l'Ente
            AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(codiceEnte)
                    .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + codiceEnte + " non trovato."));

            // Recupera il file dal repository
            AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(id))
                    .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

            // Verifica che il file appartenga all'ente specificato
            if (!file.getEnte().getId().equals(ente.getId())) {
                throw new NotFoundException("Flusso con ID " + id + " non trovato per l'ente specificato.");
            }

            // Recupera lo stato "IN_ELABORAZIONE"
            AccrualDFileStato statoInElaborazione = accrualDFileStatoRepository.findByCodice(AccrualBusinessConstants.STATO_FILE_IN_ELABORAZIONE)
                    .orElseGet(() -> {
                        LOG.warn("Stato '" + AccrualBusinessConstants.STATO_FILE_IN_ELABORAZIONE + "' non trovato, si tenta con '" + AccrualBusinessConstants.STATO_FILE_DA_ELABORARE + "'.");
                        return accrualDFileStatoRepository.findByCodice(AccrualBusinessConstants.STATO_FILE_DA_ELABORARE)
                                .orElseThrow(() -> new NotFoundException("Stati per i file non configurati nel sistema ('IN_ELABORAZIONE' o 'DA_ELABORARE')."));
                    });

            // Aggiorna lo stato del file a "IN_ELABORAZIONE" usando il nuovo metodo
            String utenteCorrente = "SYSTEM"; // Valore di fallback
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                utenteCorrente = authentication.getName(); // Recupera il nome utente autenticato
            }
            file.setCurrentStato(statoInElaborazione, utenteCorrente, ente);
            file.setDataAggiornamento(LocalDateTime.now()); // Aggiorna anche la data di aggiornamento dell'entità principale
            file.setUtenteAggiornamento(utenteCorrente);

            // Salva le modifiche con stato "IN_ELABORAZIONE"
            accrualTFileRepository.save(file);

            // Identifica il tipo di flusso e delega l'elaborazione al processor appropriato
            String tipoFileCodice = file.getFileTipo().getCodice();
            ElaboraFlussoResponseDTO response;

            if (AccrualBusinessConstants.TIPO_FILE_SALDI_PDC_118.equalsIgnoreCase(tipoFileCodice)) {
                // Delega l'elaborazione specifica per CDU-008
                ElaboraFlussoResponseDTO elaboraResponse = new ElaboraFlussoResponseDTO();
                elaboraResponse.setFlussoElaboraResponse(pdc118Processor.processaSaldiPdc118(id, confirmedOverride));
                response = elaboraResponse;
            } else if (AccrualBusinessConstants.TIPO_FILE_VARIAZIONI_INVENTARIALI.equalsIgnoreCase(tipoFileCodice)) {
                // Delega l'elaborazione specifica per CDU-009
                ElaboraFlussoResponseDTO elaboraResponse = new ElaboraFlussoResponseDTO();
                elaboraResponse.setFlussoElaboraResponse(variazioniInventarialiProcessor.processaVariazioniInventariali(id, confirmedOverride));
                response = elaboraResponse;
            } else {
                // Per altri tipi di file, usa la logica esistente
                response = flussoService.executeGenericProcessing(file, ente);
            }

            // Aggiorna lo stato del file a ELABORATO dopo il completamento con successo
            AccrualDFileStato statoElaborato = accrualDFileStatoRepository.findByCodice(AccrualBusinessConstants.STATO_FILE_ELABORATO).orElse(null);
            if (statoElaborato != null) {
                file.setCurrentStato(statoElaborato, utenteCorrente, ente);
                file.setDataAggiornamento(LocalDateTime.now());
                file.setUtenteAggiornamento(utenteCorrente);
                accrualTFileRepository.save(file);
            } else {
                LOG.warn("Stato ELABORATO non trovato — lo stato del file non verrà aggiornato.");
            }

            // Come per l'errore: l'esito positivo di un'elaborazione in background deve restare
            // consultabile anche se nel frattempo l'utente ha cambiato sessione.
            notificaService.crea(ente, null, NotificaService.TIPO_INFO, null, "Elaborazione completata",
                    "Il flusso " + file.getCodice() + " è stato elaborato con successo.", file, null);

            LOG.info("Fine elaborazione asincrona del flusso ID: {}", id);
            return CompletableFuture.completedFuture(response);

        } catch (ConfirmationRequiredException e) {
            LOG.warn("Conferma richiesta per flusso ID: {} - {}", id, e.getMessage());
            // Aggiorna lo stato del file a IN_ATTESA_CONFERMA_UTENTE
            try {
                AccrualTFile fileConf = accrualTFileRepository.findById(Long.parseLong(id)).orElse(null);
                AccrualDFileStato statoAttesa = accrualDFileStatoRepository.findByCodice(AccrualBusinessConstants.STATO_FILE_IN_ATTESA_CONFERMA).orElse(null);
                if (fileConf != null && statoAttesa != null) {
                    AccrualTEnte enteConf = fileConf.getEnte();
                    fileConf.setCurrentStato(statoAttesa, "SYSTEM", enteConf);
                    fileConf.setDataAggiornamento(LocalDateTime.now());
                    fileConf.setUtenteAggiornamento("SYSTEM");
                    accrualTFileRepository.save(fileConf);
                }
            } catch (Exception ex) {
                LOG.error("Impossibile aggiornare lo stato IN_ATTESA_CONFERMA per il flusso " + id, ex);
            }
            return CompletableFuture.completedFuture(null);

        } catch (NotFoundException e) {
            LOG.error("Errore di configurazione o dati mancanti durante l'elaborazione del flusso ID: {}: {}", id, e.getMessage());
            terminaFlussoInErrore(id, e);
            return CompletableFuture.completedFuture(null);

        } catch (IllegalArgumentException e) {
            LOG.error("Errore di validazione durante l'elaborazione del flusso ID: {}: {}", id, e.getMessage());
            terminaFlussoInErrore(id, e);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            String errorMsg = "Errore imprevisto durante l'elaborazione asincrona del flusso ID: " + id + " - " + e.getMessage();
            LOG.error(errorMsg, e);
            terminaFlussoInErrore(id, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Chiude un'elaborazione fallita: registra il testo dell'errore nel log del flusso e porta il
     * file allo stato di errore.
     *
     * <p>Senza questo passaggio il file resta su IN_ELABORAZIONE e il polling del frontend non
     * riceve mai uno stato terminale: la barra di avanzamento si ferma e il flusso non è più
     * rilanciabile.</p>
     *
     * <p>Ogni eccezione sollevata qui viene assorbita, per non mascherare l'errore originale.</p>
     *
     * @param id ID del flusso
     * @param causa Eccezione che ha interrotto l'elaborazione
     */
    private void terminaFlussoInErrore(String id, Exception causa) {
        try {
            AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(id)).orElse(null);
            if (file == null) {
                LOG.error("Flusso {} non trovato: impossibile registrarne lo stato di errore.", id);
                return;
            }

            // LogService scrive in una transazione propria (REQUIRES_NEW): il testo dell'errore
            // sopravvive al rollback dell'elaborazione e resta consultabile dall'utente.
            String dettaglio = causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName();
            logService.registraErrore(file, "Elaborazione interrotta", dettaglio);

            // L'elaborazione gira in background: senza una notifica persistente l'utente che ha
            // chiuso la pagina non saprebbe mai com'è finita.
            notificaService.crea(file.getEnte(), null, NotificaService.TIPO_ERROR, null,
                    "Elaborazione non riuscita",
                    "Il flusso " + file.getCodice() + " non è stato elaborato: " + dettaglio, file, null);

            AccrualDFileStato statoErrore = accrualDFileStatoRepository.findByCodice(AccrualBusinessConstants.STATO_FILE_ERRORE).orElse(null);
            if (statoErrore == null) {
                LOG.error("Stato '{}' non configurato: il flusso {} resterebbe in elaborazione.",
                        AccrualBusinessConstants.STATO_FILE_ERRORE, id);
                return;
            }
            file.setCurrentStato(statoErrore, "SYSTEM", file.getEnte());
            file.setDataAggiornamento(LocalDateTime.now());
            file.setUtenteAggiornamento("SYSTEM");
            accrualTFileRepository.save(file);
            LOG.info("Flusso {} portato allo stato {}.", id, AccrualBusinessConstants.STATO_FILE_ERRORE);
        } catch (Exception ex) {
            LOG.error("Impossibile aggiornare lo stato di errore per il flusso " + id, ex);
        }
    }

    /**
     * Metodo per ottenere lo stato di un'elaborazione asincrona
     *
     * @param id ID del flusso
     * @return Lo stato corrente del flusso
     * @throws NotFoundException se il flusso non è trovato
     */
    public String getStatoElaborazione(String id) throws NotFoundException {
        String codiceEnte = SecurityContextHelper.getEnteCodiceUnivocoStatic();
        LOG.debug("Richiesta stato elaborazione per flusso ID: {} e ente: {}", id, codiceEnte);

        AccrualTFile file = accrualTFileRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new NotFoundException("Flusso con ID " + id + " non trovato."));

        AccrualTEnte ente = accrualTEnteRepository.findByCodiceUnivoco(codiceEnte)
                .orElseThrow(() -> new NotFoundException("Ente con codice fiscale " + codiceEnte + " non trovato."));

        // Verifica che il file appartenga all'ente specificato
        if (!file.getEnte().getId().equals(ente.getId())) {
            throw new NotFoundException("Flusso con ID " + id + " non trovato per l'ente specificato.");
        }

        String statoCorrente = file.getCurrentStato() != null ? file.getCurrentStato().getCodice() : "Sconosciuto";
        LOG.debug("Stato corrente per flusso ID {}: {}", id, statoCorrente);

        return statoCorrente;
    }
}
