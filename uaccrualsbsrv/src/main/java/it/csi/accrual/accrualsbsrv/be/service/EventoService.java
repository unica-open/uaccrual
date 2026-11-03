/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service;

import java.util.List;

import it.csi.accrual.accrualsbsrv.dto.Evento;

public interface EventoService {

    /**
     * Ottiene tutti gli eventi
     *
     * @return lista di eventi
     */
    List<Evento> getEventi();

    /**
     * Ottiene gli eventi filtrati per ente
     *
     * @param enteCodice codice dell'ente
     * @return lista di eventi filtrati per ente
     */
    List<Evento> getEventiByEnte(String enteCodice);

    /**
     * Ottiene gli eventi filtrati per fonte e ente
     *
     * @param codiceFonte codice della fonte
     * @param enteCodice codice dell'ente
     * @return lista di eventi filtrati per fonte e ente
     */
    List<Evento> getEventiByFonteAndEnte(String codiceFonte, String enteCodice);
}