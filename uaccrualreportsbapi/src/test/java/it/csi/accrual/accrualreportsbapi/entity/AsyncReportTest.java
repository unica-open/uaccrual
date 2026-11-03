/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsyncReport Entity Tests")
class AsyncReportTest {

    @Test
    @DisplayName("Should create AsyncReport with builder")
    void testBuilderCreation() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        byte[] fileContent = "PDF content".getBytes();

        AsyncReport report = AsyncReport.builder()
                .id(id)
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .parameters("{\"param\":\"value\"}")
                .fileContent(fileContent)
                .fileSize(1024L)
                .errorMessage(null)
                .createdAt(now)
                .startedAt(now.plusSeconds(5))
                .completedAt(now.plusSeconds(10))
                .build();

        assertNotNull(report);
        assertEquals(id, report.getId());
        assertEquals(AsyncReport.ReportStatus.COMPLETED, report.getStatus());
        assertEquals("pdf", report.getFormat());
        assertEquals("test.rptdesign", report.getRptdesignFileName());
        assertEquals("test.rptconfig", report.getRptconfigFileName());
        assertEquals("{\"param\":\"value\"}", report.getParameters());
        assertArrayEquals(fileContent, report.getFileContent());
        assertEquals(1024L, report.getFileSize());
        assertEquals(now, report.getCreatedAt());
    }

    @Test
    @DisplayName("Should create AsyncReport with setters")
    void testSetterCreation() {
        AsyncReport report = new AsyncReport();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        report.setId(id);
        report.setStatus(AsyncReport.ReportStatus.PENDING);
        report.setFormat("xlsx");
        report.setRptdesignFileName("report.rptdesign");
        report.setRptconfigFileName("config.rptconfig");
        report.setParameters("{\"key\":\"value\"}");
        report.setFileContent("Excel content".getBytes());
        report.setFileSize(2048L);
        report.setErrorMessage("Error occurred");
        report.setCreatedAt(now);
        report.setStartedAt(now.plusSeconds(1));
        report.setCompletedAt(now.plusSeconds(5));

        assertEquals(id, report.getId());
        assertEquals(AsyncReport.ReportStatus.PENDING, report.getStatus());
        assertEquals("xlsx", report.getFormat());
        assertEquals("report.rptdesign", report.getRptdesignFileName());
        assertEquals("config.rptconfig", report.getRptconfigFileName());
        assertEquals("{\"key\":\"value\"}", report.getParameters());
        assertNotNull(report.getFileContent());
        assertEquals(2048L, report.getFileSize());
        assertEquals("Error occurred", report.getErrorMessage());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        AsyncReport report = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .parameters(null)
                .fileContent(null)
                .fileSize(null)
                .errorMessage(null)
                .createdAt(LocalDateTime.now())
                .build();

        assertNotNull(report);
        assertNull(report.getParameters());
        assertNull(report.getFileContent());
        assertNull(report.getFileSize());
        assertNull(report.getErrorMessage());
    }

    @Test
    @DisplayName("Should test ReportStatus enum values")
    void testReportStatusEnum() {
        assertEquals(4, AsyncReport.ReportStatus.values().length);

        assertTrue(AsyncReport.ReportStatus.valueOf("PENDING") == AsyncReport.ReportStatus.PENDING);
        assertTrue(AsyncReport.ReportStatus.valueOf("PROCESSING") == AsyncReport.ReportStatus.PROCESSING);
        assertTrue(AsyncReport.ReportStatus.valueOf("COMPLETED") == AsyncReport.ReportStatus.COMPLETED);
        assertTrue(AsyncReport.ReportStatus.valueOf("ERROR") == AsyncReport.ReportStatus.ERROR);
    }

    @Test
    @DisplayName("Should test ReportStatus enum ordering")
    void testReportStatusOrdering() {
        AsyncReport.ReportStatus[] statuses = AsyncReport.ReportStatus.values();

        assertEquals(AsyncReport.ReportStatus.PENDING, statuses[0]);
        assertEquals(AsyncReport.ReportStatus.PROCESSING, statuses[1]);
        assertEquals(AsyncReport.ReportStatus.COMPLETED, statuses[2]);
        assertEquals(AsyncReport.ReportStatus.ERROR, statuses[3]);
    }

    @Test
    @DisplayName("Should handle empty string parameters")
    void testEmptyParameters() {
        AsyncReport report = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .parameters("")
                .createdAt(LocalDateTime.now())
                .build();

        assertNotNull(report);
        assertEquals("", report.getParameters());
    }

    @Test
    @DisplayName("Should handle large file content")
    void testLargeFileContent() {
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10 MB
        
        AsyncReport report = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("pdf")
                .rptdesignFileName("large.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .fileContent(largeContent)
                .fileSize((long) largeContent.length)
                .createdAt(LocalDateTime.now())
                .build();

        assertNotNull(report);
        assertNotNull(report.getFileContent());
        assertEquals(10 * 1024 * 1024, report.getFileContent().length);
        assertEquals(10 * 1024 * 1024L, report.getFileSize());
    }

    @Test
    @DisplayName("Should handle multiple format types")
    void testMultipleFormats() {
        String[] formats = {"pdf", "xlsx", "html", "docx"};

        for (String format : formats) {
            AsyncReport report = AsyncReport.builder()
                    .id(UUID.randomUUID())
                    .status(AsyncReport.ReportStatus.PENDING)
                    .format(format)
                    .rptdesignFileName("test.rptdesign")
                    .rptconfigFileName("test.rptconfig")
                    .createdAt(LocalDateTime.now())
                    .build();

            assertEquals(format, report.getFormat());
        }
    }

    @Test
    @DisplayName("Should track lifecycle timestamps")
    void testLifecycleTimestamps() {
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime started = created.plusSeconds(5);
        LocalDateTime completed = started.plusSeconds(30);

        AsyncReport report = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .createdAt(created)
                .startedAt(started)
                .completedAt(completed)
                .build();

        assertEquals(created, report.getCreatedAt());
        assertEquals(started, report.getStartedAt());
        assertEquals(completed, report.getCompletedAt());
        assertTrue(report.getStartedAt().isAfter(report.getCreatedAt()));
        assertTrue(report.getCompletedAt().isAfter(report.getStartedAt()));
    }

    @Test
    @DisplayName("Should handle different error messages")
    void testErrorMessages() {
        String[] errorMessages = {
            "Database connection failed",
            "BIRT engine initialization error",
            "Invalid report template",
            "Timeout after 180 seconds"
        };

        for (String errorMsg : errorMessages) {
            AsyncReport report = AsyncReport.builder()
                    .id(UUID.randomUUID())
                    .status(AsyncReport.ReportStatus.ERROR)
                    .format("pdf")
                    .rptdesignFileName("test.rptdesign")
                    .rptconfigFileName("test.rptconfig")
                    .errorMessage(errorMsg)
                    .createdAt(LocalDateTime.now())
                    .build();

            assertEquals(errorMsg, report.getErrorMessage());
        }
    }

    @Test
    @DisplayName("Should handle UUID uniqueness")
    void testUuidUniqueness() {
        AsyncReport report1 = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test1.rptdesign")
                .rptconfigFileName("test1.rptconfig")
                .createdAt(LocalDateTime.now())
                .build();

        AsyncReport report2 = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test2.rptdesign")
                .rptconfigFileName("test2.rptconfig")
                .createdAt(LocalDateTime.now())
                .build();

        assertNotEquals(report1.getId(), report2.getId());
    }

    @Test
    @DisplayName("Should handle special characters in filenames")
    void testSpecialCharactersInFilenames() {
        AsyncReport report = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("report_2025-01-15_v1.0.rptdesign")
                .rptconfigFileName("config_prod_2025.rptconfig")
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals("report_2025-01-15_v1.0.rptdesign", report.getRptdesignFileName());
        assertEquals("config_prod_2025.rptconfig", report.getRptconfigFileName());
    }

    @Test
    @DisplayName("Should handle JSON parameters")
    void testJsonParameters() {
        String jsonParams = "{\"startDate\":\"2025-01-01\",\"endDate\":\"2025-01-31\",\"userId\":123}";

        AsyncReport report = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .parameters(jsonParams)
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals(jsonParams, report.getParameters());
    }
}
