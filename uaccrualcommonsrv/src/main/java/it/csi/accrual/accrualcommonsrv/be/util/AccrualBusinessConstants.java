/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.util;

/**
 * Classe centralizzata per le costanti di business del sistema Accrual.
 * Raccoglie codici di stato, tipi di file, nomi di parametri e altre stringhe
 * precedentemente hardcoded per migliorare la manutenibilità.
 */
public final class AccrualBusinessConstants {

    private AccrualBusinessConstants() {
        // Costruttore privato per impedire l'istanziazione
    }

    // Stati File (Tabella accrual_d_file_stato)
    public static final String STATO_FILE_DA_ELABORARE = "DA_ELABORARE";
    public static final String STATO_FILE_IN_ELABORAZIONE = "IN_ELABORAZIONE";
    public static final String STATO_FILE_ELABORATO = "ELABORATO";
    public static final String STATO_FILE_COMPLETATO_CON_ERRORI = "ELABORATO_CON_ERRORI";
    public static final String STATO_FILE_ERRORE = "ELABORATO_CON_ERRORI";
    public static final String STATO_FILE_IN_ATTESA_CONFERMA = "IN_ATTESA_CONFERMA_UTENTE";

    // Tipi File (Tabella accrual_d_file_tipo)
    public static final String TIPO_FILE_SALDI_PDC_118 = "SALDI_PDC_118";
    public static final String TIPO_FILE_VARIAZIONI_INVENTARIALI = "VARIAZIONI_INVENTARIALI";

    // Tipi Prima Nota (Tabella accrual_d_prima_nota_tipo)
    public static final String TIPO_PRIMA_NOTA_AUTOMATICA = "A";

    // Stati Prima Nota (Tabella accrual_d_prima_nota_stato)
    public static final String STATO_PRIMA_NOTA_DEFINITIVA = "D";
    public static final String STATO_PRIMA_NOTA_PROVVISORIA = "P";
    public static final String STATO_PRIMA_NOTA_ANNULLATO = "A";

    // Tipi Relazione PdC (Tabella accrual_d_pdc_tipo_relazione)
    public static final String TIPO_RELAZIONE_CORRELAZIONE = "CORRELAZIONE";

    // Conti Speciali
    public static final String CODICE_CONTO_FONDO_DOTAZIONE = "3.1.1.0.0";

    // Codici Eventi di Business (Tabella accrual_t_evento)
    public static final String EVENTO_APERTURA_ATTIVO = "APE_ATT";
    public static final String EVENTO_APERTURA_PASSIVO = "APE_PASS";
    public static final String EVENTO_APERTURA_COSTI = "APE_CE";
    public static final String EVENTO_APERTURA_RICAVI = "APE_RE";
    public static final String EVENTO_RETTIFICA_INVENTARIO = "RETTIFICA_INVENTARIO";

    // Codici Classi PdC (Tabella accrual_d_pdc_classe)
    public static final String CLASSE_ATTIVO = "AP";
    public static final String CLASSE_PASSIVO = "PP";
    public static final String CLASSE_COSTI = "CE";
    public static final String CLASSE_RICAVI = "RE";
    public static final String CLASSE_EPILOGATIVI = "EP";
}