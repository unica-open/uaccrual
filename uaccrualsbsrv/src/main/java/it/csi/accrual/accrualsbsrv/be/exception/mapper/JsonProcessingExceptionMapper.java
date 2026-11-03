/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.exception.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.csi.accrual.accrualsbsrv.dto.Error;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override
    public Response toResponse(JsonProcessingException exception) {
        LOG.error("Eccezione durante il parsing JSON: {}", exception.getMessage(), exception);
        
        // Logga informazioni aggiuntive sull'eccezione
        Throwable cause = exception.getCause();
        if (cause != null) {
            LOG.error("Causa dell'eccezione: {}", cause.getMessage(), cause);
        }
        
        // Logga la posizione dell'errore se disponibile
        if (exception.getLocation() != null) {
            LOG.error("Posizione dell'errore: line {}, column {}, character code {}", 
                     exception.getLocation().getLineNr(), 
                     exception.getLocation().getColumnNr(),
                     exception.getLocation().getCharOffset());
        }
        
        Error error = new Error();
        error.setStatus("400");
        error.setCode("INVALID_JSON");
        error.setTitle("Richiesta JSON non valida");
        error.setDetail("Errore durante l'elaborazione del corpo della richiesta JSON: " + exception.getMessage());
        
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }
}