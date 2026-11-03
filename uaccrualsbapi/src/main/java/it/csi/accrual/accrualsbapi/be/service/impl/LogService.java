/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTElaborazioneLog;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTElaborazioneLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servizio per la registrazione strutturata degli eventi di elaborazione,
 * inclusi scarti, errori e messaggi di conferma/interazione utente
 */
@Service
public class LogService {

    private static final Logger LOG = LoggerFactory.getLogger(LogService.class);

    @Autowired
    private AccrualTElaborazioneLogRepository logElaborazioneRepository;

    @Autowired
    private AccrualTElaborazioneRepository elaborazioneRepository;

    @Autowired
    private AccrualDElaborazioneStatoRepository elaborazioneStatoRepository;

    /**
     * Registra un evento di elaborazione
     *
     * @param flusso Entità del flusso associato
     * @param tipoEvento Tipo di evento (INFO, WARN, ERROR, CONFIRM)
     * @param messaggio Messaggio descrittivo dell'evento
     * @param dettagli Eventuali dettagli aggiuntivi
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registraEvento(AccrualTFile flusso, String tipoEvento, String messaggio, String dettagli) {
        LOG.info("Registrazione evento per flusso ID: {} - Tipo: {} - Messaggio: {}",
                 flusso.getId(), tipoEvento, messaggio);

        // Cerchiamo un'elaborazione esistente collegata a questo file che sia ancora valida
        AccrualTElaborazione elaborazione = elaborazioneRepository.findTopByFileIdAndEnteIdAndFineValiditaIsNullOrderByIdDesc(flusso.getId(), flusso.getEnte().getId())
                .orElseGet(() -> {
                    LOG.info("Nessuna elaborazione attiva trovata per file ID: {}. Creazione nuova elaborazione.", flusso.getId());
                    AccrualTElaborazione newElab = new AccrualTElaborazione();
                    newElab.setFile(flusso);
                    newElab.setEnte(flusso.getEnte());
                    newElab.setData(java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    newElab.setInizioValidita(java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    newElab.setNote("Elaborazione automatica avviata da LogService (API)");
                    
                    // Recupero dello stato IN_ELABORAZIONE (codice 'I')
                    AccrualDElaborazioneStato stato = elaborazioneStatoRepository.findByCodice("I")
                        .orElseThrow(() -> new RuntimeException("Stato elaborazione 'I' (In corso) non trovato"));
                    newElab.setStato(stato);
                    
                    return elaborazioneRepository.save(newElab);
                });

        AccrualTElaborazioneLog logEntry = new AccrualTElaborazioneLog();
        
        // Impostiamo l'ente dal flusso
        logEntry.setEnte(flusso.getEnte());
        logEntry.setElaborazione(elaborazione);

        // TODO: Recuperare l'utente autenticato in modo dinamico
        String utente = "system"; // Placeholder temporaneo

        // Formatta la data/ora corrente
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Costruisci la stringa per il campo 'operazione'
        StringBuilder operazioneBuilder = new StringBuilder();
        operazioneBuilder.append("[").append(timestamp).append("] ");
        operazioneBuilder.append(tipoEvento).append(": ").append(messaggio);
        if (dettagli != null && !dettagli.isEmpty()) {
            operazioneBuilder.append(" - ").append(dettagli);
        }
        String operazione = operazioneBuilder.toString();
        if (operazione.length() > 2500) {
            operazione = operazione.substring(0, 2497) + "...";
        }
        logEntry.setOperazione(operazione);

        logElaborazioneRepository.save(logEntry);

        LOG.debug("Evento registrato con successo per flusso ID: {}", flusso.getId());
    }

    /**
     * Registra un messaggio di errore
     *
     * @param flusso Entità del flusso associato
     * @param messaggio Messaggio di errore
     * @param dettagli Eventuali dettagli aggiuntivi
     */
    public void registraErrore(AccrualTFile flusso, String messaggio, String dettagli) {
        registraEvento(flusso, "ERROR", messaggio, dettagli);
    }

    /**
     * Registra un messaggio di warning
     *
     * @param flusso Entità del flusso associato
     * @param messaggio Messaggio di warning
     * @param dettagli Eventuali dettagli aggiuntivi
     */
    public void registraWarning(AccrualTFile flusso, String messaggio, String dettagli) {
        registraEvento(flusso, "WARN", messaggio, dettagli);
    }

    /**
     * Registra un messaggio informativo
     *
     * @param flusso Entità del flusso associato
     * @param messaggio Messaggio informativo
     * @param dettagli Eventuali dettagli aggiuntivi
     */
    public void registraInfo(AccrualTFile flusso, String messaggio, String dettagli) {
        registraEvento(flusso, "INFO", messaggio, dettagli);
    }

    /**
     * Registra un messaggio di conferma che richiede interazione utente
     *
     * @param flusso Entità del flusso associato
     * @param messaggio Messaggio di conferma
     * @param dettagli Eventuali dettagli aggiuntivi
     */
    public void registraConferma(AccrualTFile flusso, String messaggio, String dettagli) {
        registraEvento(flusso, "CONFIRM", messaggio, dettagli);
    }

    /**
     * Recupera i log di elaborazione per un flusso specifico
     *
     * @param flussoId ID del flusso
     * @return Lista di log di elaborazione
     */
    public List<AccrualTElaborazioneLog> getLogPerFlusso(String flussoId) {
        LOG.debug("Recupero log di elaborazione per flusso ID: {}", flussoId);
        return logElaborazioneRepository.findByElaborazioneFileIdOrderByDataInserimentoDesc(Long.parseLong(flussoId));
    }

    /**
     * Controlla se ci sono errori registrati per un flusso specifico
     *
     * @param flussoId ID del flusso
     * @return true se ci sono errori, false altrimenti
     */
    public boolean hasErrors(Long flussoId) {
        LOG.debug("Controllo errori per flusso ID: {}", flussoId);
        List<AccrualTElaborazioneLog> logs = logElaborazioneRepository.findByElaborazioneFileIdOrderByDataInserimentoDesc(flussoId);
        return logs.stream().anyMatch(log -> log.getOperazione().contains("Tipo: ERROR"));
    }
}
