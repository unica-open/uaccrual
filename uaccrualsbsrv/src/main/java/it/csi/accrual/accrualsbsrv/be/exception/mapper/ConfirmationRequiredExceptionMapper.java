/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.exception.mapper;

import it.csi.accrual.accrualsbsrv.be.exception.ConfirmationRequiredException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper per l'eccezione ConfirmationRequiredException.
 * Restituisce una risposta HTTP 409 (Conflict) con il codice di conferma
 * che il frontend dovrà gestire mostrando un dialog di conferma.
 */
@Provider
public class ConfirmationRequiredExceptionMapper implements ExceptionMapper<ConfirmationRequiredException> {

    @Override
    public Response toResponse(ConfirmationRequiredException exception) {
        Map<String, Object> entity = new HashMap<>();
        entity.put("confirmationRequired", true);
        entity.put("confirmationCode", exception.getConfirmationCode());
        entity.put("confirmationMessage", exception.getConfirmationMessage());

        return Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(entity)
            .build();
    }
}
