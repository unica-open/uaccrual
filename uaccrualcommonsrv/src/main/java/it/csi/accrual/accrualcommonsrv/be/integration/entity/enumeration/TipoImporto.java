/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualBaseEnum;

public enum TipoImporto implements AccrualBaseEnum {
    LORDO("LORDO"),
    IMPONIBILE("IMPONIBILE"),
    IMPOSTA("IMPOSTA");

    private String code;

    TipoImporto(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public static TipoImporto getByCode(String code) {
        for (TipoImporto e : TipoImporto.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}