/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.exception.mapper;

import it.csi.accrual.accrualsbsrv.dto.Error;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintViolationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        LOG.error("Eccezione durante la validazione: {}", exception.getMessage(), exception);
        
        // Logga i dettagli delle violazioni
        String violations = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        LOG.error("Violazioni di vincolo: {}", violations);
        
        Error error = new Error();
        error.setStatus("400");
        error.setCode("VALIDATION_ERROR");
        error.setTitle("Errore di validazione");
        error.setDetail("Errore durante la validazione dei parametri: " + violations);
        
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }
}