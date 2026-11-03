/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualBaseEnum;

/**
 * Enumerazione per i tipi di relazione tra conti del Piano dei Conti.
 * Mappa i codici presenti nella tabella accrual_d_pdc_tipo_relazione.
 */
public enum PdcRelazioneTipoEnum implements AccrualBaseEnum {
    CORRELAZIONE("CORRELAZIONE"),
    EPILOGO("EPILOGO"),
    CHIUSURA("CHIUSURA");

    private final String codice;

    PdcRelazioneTipoEnum(String codice) {
        this.codice = codice;
    }

    @Override
    public String getCode() {
        return codice;
    }

    public static PdcRelazioneTipoEnum fromCodice(String codice) {
        for (PdcRelazioneTipoEnum tipo : PdcRelazioneTipoEnum.values()) {
            if (tipo.codice.equals(codice)) {
                return tipo;
            }
        }
        return null;
    }
}