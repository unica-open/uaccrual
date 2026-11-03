/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.service;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTEnte;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTEnteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Servizio per l'accesso ai dati degli enti.
 * Fornisce metodi per la ricerca e verifica degli enti nel database.
 */
@Service
public class EnteAccessService {

    @Autowired
    private AccrualTEnteRepository enteRepository;

    /**
     * Cerca un ente per codice univoco.
     * @param codiceUnivoco Codice univoco dell'ente
     * @return Optional contenente l'ente se trovato
     */
    public Optional<AccrualTEnte> findEnteByCodiceUnivoco(String codiceUnivoco) {
        return enteRepository.findByCodiceUnivoco(codiceUnivoco);
    }

    /**
     * Verifica se un utente ha accesso a un ente.
     * In modalità senza autenticazione, tutti gli utenti hanno accesso a tutti gli enti.
     * @param codiceFiscale Codice fiscale dell'utente
     * @param enteId ID dell'ente
     * @return true se l'utente ha accesso, false altrimenti
     */
    public boolean verifyUserAccessToEnte(String codiceFiscale, Long enteId) {
        // In modalità senza autenticazione, tutti gli utenti hanno accesso a tutti gli enti
        return true;
    }
}
