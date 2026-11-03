/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.exception;

public class InvalidFileException extends BirtException {
    public InvalidFileException(String message) {
        super("INVALID_FILE", message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super("INVALID_FILE", message, cause);
    }
}