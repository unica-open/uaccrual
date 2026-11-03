/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.exception;

/**
 * Eccezione lanciata quando un utente tenta di accedere a una risorsa
 * per la quale non ha i permessi necessari.
 */
public class ForbiddenOperationException extends Exception {

    public ForbiddenOperationException(String message) {
        super(message);
    }

    public ForbiddenOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
