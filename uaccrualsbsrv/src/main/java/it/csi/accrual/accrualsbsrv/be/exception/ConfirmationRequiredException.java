/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.exception;

/**
 * Eccezione runtime lanciata quando un'operazione richiede una conferma da parte dell'utente.
 * Viene gestita dal ConfirmationRequiredExceptionMapper che restituisce una risposta HTTP 409 Conflict.
 */
public class ConfirmationRequiredException extends RuntimeException {

    private String confirmationCode;
    private String confirmationMessage;

    public ConfirmationRequiredException(String confirmationCode, String confirmationMessage) {
        super(confirmationMessage);
        this.confirmationCode = confirmationCode;
        this.confirmationMessage = confirmationMessage;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public String getConfirmationMessage() {
        return confirmationMessage;
    }
}
