/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualsbapi.be.exception.NotFoundException;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualDFileStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualRFileStato;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualDFileStatoRepository;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // AGGIUNTO

import java.time.LocalDateTime; // AGGIUNTO
import java.util.Optional; // AGGIUNTO

/**
 * Servizio per la gestione delle interazioni con l'utente durante l'elaborazione.
 * Questa classe è responsabile di gestire la logica di richiesta/ricezione di conferma
 * da parte del frontend, aggiornando lo stato del flusso e registrando i messaggi.
 */
@Service
@Transactional // AGGIUNTO
public class InterazioneUtenteService {

    private static final Logger LOG = LoggerFactory.getLogger(InterazioneUtenteService.class);

    @Autowired
    private AccrualTFileRepository accrualTFileRepository;
    @Autowired
    private AccrualDFileStatoRepository accrualDFileStatoRepository;
    @Autowired
    private LogService logService;
    // TODO: Recuperare l'utente autenticato in modo dinamico
    private static final String SYSTEM_USER = "SYSTEM";

    /**
     * Registra un messaggio per l'utente (errore, warning, info, conferma).
     * Questo metodo non blocca l'esecuzione ma registra il messaggio nel log del flusso
     * e può, in futuro, renderlo disponibile per il polling del frontend.
     *
     * @param flusso Entità del flusso associato.
     * @param codiceMessaggio Codice del messaggio (es. WEB_ERR_0025, WEB_CON_0024).
     * @param testoMessaggio Testo del messaggio.
     * @param tipoMessaggio Tipo del messaggio ("ERROR", "WARNING", "INFO", "CONFIRM").
     */
    public void registraMessaggioUtente(AccrualTFile flusso, String codiceMessaggio, String testoMessaggio, String tipoMessaggio) {
        LOG.info("Registrazione messaggio utente per flusso ID: {} - Codice: {}, Tipo: {}",
                 flusso.getId(), codiceMessaggio, tipoMessaggio);

        switch (tipoMessaggio) {
            case "ERROR":
                logService.registraErrore(flusso, codiceMessaggio, testoMessaggio);
                break;
            case "WARNING":
                logService.registraWarning(flusso, codiceMessaggio, testoMessaggio);
                break;
            case "INFO":
                logService.registraInfo(flusso, codiceMessaggio, testoMessaggio);
                break;
            case "CONFIRM":
                logService.registraInfo(flusso, codiceMessaggio, testoMessaggio + " (Richiesta conferma)"); // Registra anche le richieste di conferma
                break;
            default:
                logService.registraInfo(flusso, codiceMessaggio, testoMessaggio);
                break;
        }
    }

    /**
     * Invia una richiesta di conferma al frontend. L'elaborazione del flusso viene sospesa.
     *
     * @param flusso Entità del flusso associato.
     * @param codiceMessaggio Codice del messaggio di conferma (es. WEB_CON_0024).
     * @param testoMessaggio Testo del messaggio da mostrare all'utente.
     * @return true se la richiesta è stata inviata con successo e il flusso è in attesa.
     * @throws NotFoundException se lo stato "IN_ATTESA_CONFERMA_UTENTE" non è configurato.
     */
    public boolean richiediConfermaUtente(AccrualTFile flusso, String codiceMessaggio, String testoMessaggio) throws NotFoundException {
        LOG.info("Richiesta conferma utente per flusso ID: {} - Codice: {}",
                 flusso.getId(), codiceMessaggio);

        // 1. Registra il messaggio di conferma nel log del flusso
        registraMessaggioUtente(flusso, codiceMessaggio, testoMessaggio, "CONFIRM");

        // 2. Aggiorna lo stato del flusso a "IN_ATTESA_CONFERMA_UTENTE"
        AccrualDFileStato statoAttesa = accrualDFileStatoRepository.findByCodice("IN_ATTESA_CONFERMA_UTENTE")
            .orElseThrow(() -> new NotFoundException("Stato 'IN_ATTESA_CONFERMA_UTENTE' per i file non configurato nel sistema."));

        flusso.setCurrentStato(statoAttesa, SYSTEM_USER, flusso.getEnte());
        flusso.setDataAggiornamento(LocalDateTime.now());
        flusso.setUtenteAggiornamento(SYSTEM_USER);
        accrualTFileRepository.save(flusso);

        LOG.info("Flusso ID: {} posto in stato 'IN_ATTESA_CONFERMA_UTENTE' con richiesta: {}", flusso.getId(), codiceMessaggio);
        return true; // Indica che l'elaborazione è stata sospesa
    }

    /**
     * Metodo di supporto per verificare se sono presenti dati esistenti (placeholder).
     * La logica reale di verifica dovrebbe essere implementata nei processori specifici.
     * Questo metodo può essere rimosso o modificato per recuperare i messaggi di conferma
     * pendenti dal flusso.
     *
     * @param flussoId ID del flusso.
     * @param tipoControllo Tipo di controllo da effettuare (non più usato direttamente per la logica di verifica, ma per logging).
     * @return Sempre false, in quanto la verifica effettiva avviene nel processore.
     * @deprecated La logica di verifica dati esistenti deve risiedere nei processori (Pdc118Processor, VariazioniInventarialiProcessor).
     *             Questo metodo rimane solo come scheletro per future evoluzioni o per coerenza con il codice esistente.
     */
    @Deprecated
    public boolean verificaDatiEsistenti(String flussoId, String tipoControllo) {
        LOG.warn("Chiamata a metodo deprecato verificaDatiEsistenti in InterazioneUtenteService. La logica di verifica deve essere nei processori.");
        return false;
    }

    /**
     * Gestisce la risposta dell'utente al messaggio di conferma.
     * Questo metodo dovrebbe essere chiamato da un endpoint del backend dopo che il frontend
     * ha ricevuto la risposta dall'utente.
     *
     * @param flussoId ID del flusso.
     * @param codiceMessaggio Codice del messaggio a cui si riferisce la risposta.
     * @param risposta Risposta dell'utente (es. "SI", "NO", "ANNULLA").
     * @return true se l'elaborazione deve continuare, false se deve essere annullata.
     * @throws NotFoundException se il flusso non è trovato o non è nello stato di attesa.
     * @throws IllegalStateException se il flusso non è nello stato "IN_ATTESA_CONFERMA_UTENTE".
     */
    public boolean gestisciRispostaUtente(String flussoId, String codiceMessaggio, String risposta) throws NotFoundException, IllegalStateException {
        LOG.info("Gestione risposta utente per flusso ID: {}, messaggio: {}, risposta: {}",
                 flussoId, codiceMessaggio, risposta);

        AccrualTFile flusso = accrualTFileRepository.findById(Long.parseLong(flussoId))
            .orElseThrow(() -> new NotFoundException("Flusso con ID " + flussoId + " non trovato."));

        if (!"IN_ATTESA_CONFERMA_UTENTE".equals(flusso.getCurrentStato().getCodice())) {
            throw new IllegalStateException("Il flusso " + flussoId + " non è in attesa di conferma utente.");
        }

        boolean continuaElaborazione = "SI".equalsIgnoreCase(risposta) || "CONFERMA".equalsIgnoreCase(risposta);

        if (continuaElaborazione) {
            registraMessaggioUtente(flusso, codiceMessaggio, "Conferma utente: SI. Riprendo elaborazione.", "INFO");
            // Imposta lo stato per riprendere l'elaborazione (es. "IN_ELABORAZIONE")
            AccrualDFileStato statoInElaborazione = accrualDFileStatoRepository.findByCodice("IN_ELABORAZIONE")
                .orElseThrow(() -> new NotFoundException("Stato 'IN_ELABORAZIONE' non configurato."));
            flusso.setCurrentStato(statoInElaborazione, SYSTEM_USER, flusso.getEnte());
            flusso.setDataAggiornamento(LocalDateTime.now());
            flusso.setUtenteAggiornamento(SYSTEM_USER);
            accrualTFileRepository.save(flusso);
        } else {
            registraMessaggioUtente(flusso, codiceMessaggio, "Conferma utente: NO. Elaborazione annullata.", "INFO");
            // Imposta lo stato per indicare l'annullamento
            AccrualDFileStato statoAnnullato = accrualDFileStatoRepository.findByCodice("ANNULLATO") // O ERRORE/ANNULLATO_DA_UTENTE
                .orElseThrow(() -> new NotFoundException("Stato 'ANNULLATO' non configurato."));
            flusso.setCurrentStato(statoAnnullato, SYSTEM_USER, flusso.getEnte());
            flusso.setDataAggiornamento(LocalDateTime.now());
            flusso.setUtenteAggiornamento(SYSTEM_USER);
            accrualTFileRepository.save(flusso);
        }

        return continuaElaborazione;
    }
}
