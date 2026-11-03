/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.exception;

public class BirtException extends RuntimeException {
    private String code;
    private String requestId;

    public BirtException(String message) {
        super(message);
        this.code = "INTERNAL_ERROR";
    }

    public BirtException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BirtException(String code, String message, String requestId) {
        super(message);
        this.code = code;
        this.requestId = requestId;
    }

    public BirtException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
