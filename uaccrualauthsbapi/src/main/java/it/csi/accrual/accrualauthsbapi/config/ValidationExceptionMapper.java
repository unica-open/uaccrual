/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper per le eccezioni di validazione Bean.
 * Logga i dettagli della validazione fallita per debugging.
 */
@Provider
@Slf4j
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String details = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));

        log.error(">>> VALIDAZIONE FALLITA: {}", details);
        for (ConstraintViolation<?> v : exception.getConstraintViolations()) {
            log.error("  - Campo: {}, Valore: {}, Messaggio: {}",
                    v.getPropertyPath(), v.getInvalidValue(), v.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Validazione fallita: " + details)
                .build();
    }
}
