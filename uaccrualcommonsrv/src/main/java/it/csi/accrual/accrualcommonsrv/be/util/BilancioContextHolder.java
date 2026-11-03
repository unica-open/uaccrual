/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.util;

/**
 * ThreadLocal per memorizzare l'anno di bilancio corrente.
 * 
 * Permette di accedere all'anno di bilancio senza passare header HTTP espliciti,
 * analogamente a quanto fatto con EnteContextHolder per il codice ente.
 */
public class BilancioContextHolder {

    private static final ThreadLocal<Integer> CURRENT_ANNO_BILANCIO = new ThreadLocal<>();

    private BilancioContextHolder() {
        // Utility class, non istanziabile
    }

    /**
     * Imposta l'anno di bilancio per il thread corrente.
     * 
     * @param anno anno di bilancio
     */
    public static void setCurrentAnnoBilancio(Integer anno) {
        CURRENT_ANNO_BILANCIO.set(anno);
    }

    /**
     * Ottiene l'anno di bilancio per il thread corrente.
     * 
     * @return anno di bilancio, o null se non impostato
     */
    public static Integer getCurrentAnnoBilancio() {
        return CURRENT_ANNO_BILANCIO.get();
    }

    /**
     * Rimuove l'anno di bilancio dal thread corrente.
     * Da chiamare a fine richiesta per evitare memory leak.
     */
    public static void clear() {
        CURRENT_ANNO_BILANCIO.remove();
    }
}
