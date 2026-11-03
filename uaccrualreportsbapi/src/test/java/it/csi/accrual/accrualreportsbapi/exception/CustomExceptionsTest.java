/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Custom Exceptions Tests")
class CustomExceptionsTest {

    // ===== BirtException Tests =====

    @Test
    @DisplayName("BirtException - Should create with message")
    void testBirtExceptionWithMessage() {
        String message = "BIRT engine initialization failed";

        BirtException exception = new BirtException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("BirtException - Should create with code and message")
    void testBirtExceptionWithCodeAndMessage() {
        String code = "CUSTOM_CODE";
        String message = "BIRT engine error";

        BirtException exception = new BirtException(code, message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
    }

    @Test
    @DisplayName("BirtException - Should create with code, message and cause")
    void testBirtExceptionWithCodeMessageAndCause() {
        String code = "CUSTOM_CODE";
        String message = "BIRT engine error";
        Throwable cause = new RuntimeException("Underlying cause");

        BirtException exception = new BirtException(code, message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(code, exception.getCode());
        assertEquals(cause, exception.getCause());
    }

    // ===== InvalidFileException Tests =====

    @Test
    @DisplayName("InvalidFileException - Should create with message")
    void testInvalidFileExceptionWithMessage() {
        String message = "Invalid file format";

        InvalidFileException exception = new InvalidFileException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    // ===== QueueFullException Tests =====

    @Test
    @DisplayName("QueueFullException - Should create with message")
    void testQueueFullExceptionWithMessage() {
        String message = "Report queue is full";

        QueueFullException exception = new QueueFullException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    // ===== ReportGenerationException Tests =====

    @Test
    @DisplayName("ReportGenerationException - Should create with message")
    void testReportGenerationExceptionWithMessage() {
        String message = "Report generation failed";

        ReportGenerationException exception = new ReportGenerationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    // ===== ReportNotFoundException Tests =====

    @Test
    @DisplayName("ReportNotFoundException - Should create with message")
    void testReportNotFoundExceptionWithMessage() {
        String message = "Report not found with ID: 12345";

        ReportNotFoundException exception = new ReportNotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    // ===== ReportTimeoutException Tests =====

    @Test
    @DisplayName("ReportTimeoutException - Should create with message")
    void testReportTimeoutExceptionWithMessage() {
        String message = "Report generation timeout after 180 seconds";

        ReportTimeoutException exception = new ReportTimeoutException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    // ===== Exception Inheritance Tests =====

    @Test
    @DisplayName("All exceptions should extend RuntimeException")
    void testExceptionInheritance() {
        assertTrue(new BirtException("test") instanceof RuntimeException);
        assertTrue(new InvalidFileException("test") instanceof RuntimeException);
        assertTrue(new QueueFullException("test") instanceof RuntimeException);
        assertTrue(new ReportGenerationException("test") instanceof RuntimeException);
        assertTrue(new ReportNotFoundException("test") instanceof RuntimeException);
        assertTrue(new ReportTimeoutException("test") instanceof RuntimeException);
    }

    @Test
    @DisplayName("Exceptions should be throwable")
    void testExceptionsAreThrowable() {
        assertThrows(BirtException.class, () -> {
            throw new BirtException("test");
        });

        assertThrows(InvalidFileException.class, () -> {
            throw new InvalidFileException("test");
        });

        assertThrows(QueueFullException.class, () -> {
            throw new QueueFullException("test");
        });

        assertThrows(ReportGenerationException.class, () -> {
            throw new ReportGenerationException("test");
        });

        assertThrows(ReportNotFoundException.class, () -> {
            throw new ReportNotFoundException("test");
        });

        assertThrows(ReportTimeoutException.class, () -> {
            throw new ReportTimeoutException("test");
        });
    }

    @Test
    @DisplayName("Exceptions should preserve stack trace")
    void testExceptionStackTrace() {
        try {
            throw new ReportGenerationException("Test exception");
        } catch (ReportGenerationException e) {
            assertNotNull(e.getStackTrace());
            assertTrue(e.getStackTrace().length > 0);
        }
    }

    @Test
    @DisplayName("BirtException should support chaining")
    void testExceptionChaining() {
        Throwable rootCause = new IllegalStateException("Root cause");
        String code = "CUSTOM_CODE";
        String message = "Top level";
        BirtException topException = new BirtException(code, message, rootCause);

        assertEquals(rootCause, topException.getCause());
        assertEquals(code, topException.getCode());
    }
}
