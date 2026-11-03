/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import java.util.List;

import it.csi.accrual.accrualsbsrv.dto.Causale; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.CausaleTipo; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.CausaleStato; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.Evento; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.PagedCausale; // DTO generato da Swagger
import it.csi.accrual.accrualsbsrv.dto.CausaleRequest; // Nuovo import
import it.csi.accrual.accrualsbsrv.dto.ContoCollegato; // Nuovo import

public interface CausaleService {

    PagedCausale getPagedCausali(String filter, Integer limit, Integer offset);

    /**
     * Trova una causale specifica per ID.
     *
     * @param id ID della causale.
     * @return Causale trovata.
     */
    Causale getCausaleById(Long id);

    /**
     * Ottiene tutti i tipi di causale.
     *
     * @return Lista di CausaleTipo.
     */
    List<CausaleTipo> getTipiCausale();


    /**
     * Ottiene tutti gli stati di causale.
     *
     * @return Lista di CausaleStato.
     */
    List<CausaleStato> getCausaleStati();

    /**
     * Ottiene i conti collegati a una causale specifica.
     *
     * @param codiceCausale Codice della causale.
     * @return Lista di ContoCollegato.
     */
    List<it.csi.accrual.accrualsbsrv.dto.ContoCollegato> getContiCollegati(Long id);

    /**
     * Crea una nuova causale.
     * @param causaleRequest DTO per la creazione della causale.
     * @return La causale creata.
     */
    Causale createCausale(CausaleRequest causaleRequest);

    /**
     * Aggiorna una causale esistente.
     * @param id ID della causale da aggiornare.
     * @param causaleRequest DTO per l'aggiornamento della causale.
     * @return La causale aggiornata.
     */
    Causale updateCausale(Long id, CausaleRequest causaleRequest);

    /**
     * Valida una causale.
     * @param id ID della causale da validare.
     */
    void validaCausale(Long id);

    /**
     * Elimina (logicamente) una causale dal sistema.
     * @param id ID della causale da eliminare.
     */
    void deleteCausale(Long id);

    /**
     * Elenca le causali associate a un evento.
     *
     * @param eventoCodice codice dell'evento.
     * @param statoCodice codice dello stato corrente su cui filtrare le causali; se null o vuoto non si filtra.
     * @return lista delle causali, vuota se nessuna corrisponde.
     */
    List<Causale> getEventiCausale(String eventoCodice, String statoCodice);

    /**
     * Ottiene gli eventi associati a una causale specifica.
     *
     * @param idCausale ID della causale.
     * @return Lista di Evento.
     */
    List<Evento> findEventiByCausaleId(Long idCausale);

    // Metodi per CUD (Create, Update, Delete) sarebbero aggiunti qui, se richiesti.
    // Causale createCausale(Causale causale);
    // Causale updateCausale(String codice, Causale causale);
    // void deleteCausale(String codice);

    List<Evento> getEventiCausale();
}
