/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Evento di progresso generato dal monitor BIRT.
 * Contiene il tipo di evento e il valore associato.
 */
@Getter
@RequiredArgsConstructor
public class ProgressEvent {
    
    /**
     * Tipo di evento di progresso.
     */
    public enum Type {
        /** Task avviato */
        START_TASK,
        /** Task completato */
        END_TASK,
        /** Pagina iniziata */
        START_PAGE,
        /** Pagina completata */
        END_PAGE,
        /** Query avviata */
        START_QUERY,
        /** Query completata */
        END_QUERY,
        /** Righe lette dal database */
        FETCH_ROW
    }

    private final Type type;
    private final int value;
}
