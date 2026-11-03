/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.exception;

public class ReportNotFoundException extends BirtException {
    public ReportNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}