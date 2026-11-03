/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.exception;

import jakarta.ws.rs.core.Response;

public class IllegalArgumentException extends ApiException {
    private static final long serialVersionUID = 1L;

    public IllegalArgumentException(String message) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), message);
    }
}