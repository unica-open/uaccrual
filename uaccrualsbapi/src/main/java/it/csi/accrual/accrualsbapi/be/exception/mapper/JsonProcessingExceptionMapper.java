/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.exception.mapper;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.MediaType;
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
        LOG.error("JsonProcessingException caught: {}", exception.getMessage());

        // Crea un oggetto di errore personalizzato
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        errorResponse.setCode("JSON_PROCESSING_ERROR");
        errorResponse.setTitle("Errore di elaborazione JSON");
        errorResponse.setDetail("Si è verificato un errore durante l'elaborazione del contenuto JSON: " + exception.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Classe interna per la rappresentazione dell'errore
    public static class ErrorResponse {
        private int status;
        private String code;
        private String title;
        private String detail;

        // Costruttori, getter e setter
        public ErrorResponse() {}

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }
}
