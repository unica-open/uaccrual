/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.exception;

public class ReportGenerationException extends BirtException {
    public ReportGenerationException(String message) {
        super("REPORT_GENERATION_ERROR", message);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super("REPORT_GENERATION_ERROR", message, cause);
    }
}