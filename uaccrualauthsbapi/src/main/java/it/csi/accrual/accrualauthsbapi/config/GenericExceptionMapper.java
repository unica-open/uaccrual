/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.config;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Mapper generico per eccezioni non gestite.
 * Logga stack trace completo per debugging.
 */
@Provider
@Slf4j
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        log.error(">>> ECCEZIONE NON GESTITA:", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Errore interno: " + exception.getMessage())
                .build();
    }
}
