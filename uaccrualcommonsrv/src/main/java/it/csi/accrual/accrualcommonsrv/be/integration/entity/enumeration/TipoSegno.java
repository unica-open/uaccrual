/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualBaseEnum;

public enum TipoSegno implements AccrualBaseEnum {
    DARE("DARE"),
    AVERE("AVERE");

    private String code;

    TipoSegno(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public static TipoSegno getByCode(String code) {
        for (TipoSegno e : TipoSegno.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}