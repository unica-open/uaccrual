/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.exception.mapper;

import it.csi.accrual.accrualsbapi.be.exception.ConfirmationRequiredException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ConfirmationRequiredExceptionMapper implements ExceptionMapper<ConfirmationRequiredException> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmationRequiredExceptionMapper.class);

    @Override
    public Response toResponse(ConfirmationRequiredException exception) {
        LOG.warn("ConfirmationRequiredException caught: {} - {}", exception.getConfirmationCode(), exception.getConfirmationMessage());

        ConfirmationResponse confirmationResponse = new ConfirmationResponse();
        confirmationResponse.setStatus(Response.Status.CONFLICT.getStatusCode());
        confirmationResponse.setCode(exception.getConfirmationCode());
        confirmationResponse.setTitle("Conferma richiesta");
        confirmationResponse.setDetail(exception.getConfirmationMessage());

        return Response.status(Response.Status.CONFLICT)
                .entity(confirmationResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public static class ConfirmationResponse {
        private int status;
        private String code;
        private String title;
        private String detail;

        public ConfirmationResponse() {}

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
