/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualBaseEnum;

/**
 * Enumerazione degli stati possibili per una Prima Nota.
 * 
 * <p>Gli stati definiscono il ciclo di vita di una prima nota nel sistema contabile:</p>
 * <ul>
 *   <li>{@code PROVVISORIA} - Prima nota inserita ma non ancora validata definitivamente</li>
 *   <li>{@code DEFINITIVA} - Prima nota validata e registrata con numero definitivo</li>
 *   <li>{@code ANNULLATA} - Prima nota annullata (non più modificabile)</li>
 * </ul>
 * 
 * @author Accrual Team
 * @since 1.0.0
 */
public enum StatoPrimaNota implements AccrualBaseEnum {
    
    /**
     * Stato provvisorio - prima nota inserita ma non ancora validata
     */
    PROVVISORIA("P"),
    
    /**
     * Stato definitivo - prima nota validata e registrata
     */
    DEFINITIVA("D"),
    
    /**
     * Stato annullato - prima nota annullata
     */
    ANNULLATA("A");

    private final String code;

    StatoPrimaNota(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    /**
     * Ottiene l'enumerazione corrispondente al codice fornito.
     * 
     * @param code il codice dello stato (es. "P", "D", "A")
     * @return l'enumerazione corrispondente, o {@code null} se il codice non è valido
     */
    public static StatoPrimaNota getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (StatoPrimaNota stato : StatoPrimaNota.values()) {
            if (stato.getCode().equalsIgnoreCase(code)) {
                return stato;
            }
        }
        return null;
    }

    /**
     * Verifica se lo stato corrente è uno stato finale (non modificabile).
     * 
     * @return {@code true} se lo stato è {@link #ANNULLATA}, {@code false} altrimenti
     */
    public boolean isStatoFinale() {
        return this == ANNULLATA;
    }

    /**
     * Verifica se lo stato corrente permette modifiche.
     * 
     * @return {@code true} se lo stato è {@link #PROVVISORIA}, {@code false} altrimenti
     */
    public boolean isModificabile() {
        return this == PROVVISORIA;
    }

    /**
     * Verifica se lo stato corrente permette la validazione.
     * 
     * @return {@code true} se lo stato è {@link #PROVVISORIA}, {@code false} altrimenti
     */
    public boolean isValidabile() {
        return this == PROVVISORIA;
    }

    /**
     * Verifica se lo stato corrente permette la riapertura.
     * 
     * @return {@code true} se lo stato è {@link #DEFINITIVA}, {@code false} altrimenti
     */
    public boolean isRiapribile() {
        return this == DEFINITIVA;
    }

    /**
     * Verifica se lo stato corrente permette l'annullamento.
     * 
     * @return {@code true} se lo stato è {@link #PROVVISORIA} o {@link #DEFINITIVA}, {@code false} se è {@link #ANNULLATA}
     */
    public boolean isAnnullabile() {
        return this != ANNULLATA;
    }
}
