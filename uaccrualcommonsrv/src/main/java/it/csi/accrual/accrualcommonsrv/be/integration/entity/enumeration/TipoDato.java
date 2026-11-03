/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualBaseEnum;

public enum TipoDato implements AccrualBaseEnum {
    NUMERICO("numerico"),
    BOOLEAN("boolean"),
    TESTO("testo"),
    PERCENTUALE("percentuale"),
    TABELLA("tabella"),
    BYTEA("bytea");

    private String code;

    TipoDato(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public static TipoDato getByCode(String code) {
        for (TipoDato e : TipoDato.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}