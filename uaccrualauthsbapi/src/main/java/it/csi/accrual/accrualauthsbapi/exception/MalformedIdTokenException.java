/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.exception;

import jakarta.ws.rs.core.Response;

public class MalformedIdTokenException extends ApiException {
    private static final long serialVersionUID = 1L;

    public MalformedIdTokenException(String message) {
        super(Response.Status.UNAUTHORIZED.getStatusCode(), message);
    }
}