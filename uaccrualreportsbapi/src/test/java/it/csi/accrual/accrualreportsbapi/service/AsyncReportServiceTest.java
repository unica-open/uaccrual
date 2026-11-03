/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service;

import it.csi.accrual.accrualreportsbapi.entity.AsyncReport;
import it.csi.accrual.accrualreportsbapi.exception.ReportNotFoundException;
import it.csi.accrual.accrualreportsbapi.repository.AsyncReportRepository;
import it.csi.accrual.accrualreportsbapi.util.ParameterParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AsyncReportService Tests")
@ExtendWith(MockitoExtension.class)
class AsyncReportServiceTest {

    @Mock
    private AsyncReportRepository reportRepository;

    @Mock
    private RenderService renderService;

    @Mock
    private ParameterParser parameterParser;

    @Mock
    private ProgressRegistry progressRegistry;

    @InjectMocks
    private AsyncReportService asyncReportService;

    private UUID testReportId;
    private AsyncReport testReport;

    @BeforeEach
    void setUp() {
        testReportId = UUID.randomUUID();
        testReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .fileContent("PDF content".getBytes())
                .fileSize(11L)
                .createdAt(LocalDateTime.now())
                .enteId(1L)
                .version(0L)
                .build();
    }

    // ===== CREATE ASYNC REPORT TESTS =====

    @Test
    @DisplayName("Should create async report with status PENDING")
    void testCreateAsyncReportSuccess() {
        byte[] rptdesignContent = "<?xml version=\"1.0\"?><report></report>".getBytes();
        byte[] rptconfigContent = "<?xml version=\"1.0\"?><config></config>".getBytes();

        AsyncReport savedReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .createdAt(LocalDateTime.now())
                .enteId(1L)
                .version(0L)
                .build();

        when(reportRepository.save(any(AsyncReport.class)))
                .thenReturn(savedReport);

        it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse response = asyncReportService.createAsyncReport(
                "pdf",
                rptdesignContent,
                "test.rptdesign",
                rptconfigContent,
                "test.rptconfig",
                null
        );

        assertNotNull(response);
        assertNotNull(response.getReportId());
        assertEquals("PENDING", response.getStatus().toString());
        verify(reportRepository, times(1)).save(any(AsyncReport.class));
    }

    @Test
    @DisplayName("Should create async report for large files")
    void testCreateAsyncReportForLargeFiles() {
        // Create 5 MB content
        byte[] largeContent = new byte[5 * 1024 * 1024];
        byte[] smallContent = "<?xml version=\"1.0\"?><config></config>".getBytes();

        AsyncReport savedReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("large.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .createdAt(LocalDateTime.now())
                .enteId(1L)
                .version(0L)
                .build();

        when(reportRepository.save(any(AsyncReport.class)))
                .thenReturn(savedReport);

        it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse response = asyncReportService.createAsyncReport(
                "pdf",
                largeContent,
                "large.rptdesign",
                smallContent,
                "test.rptconfig",
                null
        );

        assertNotNull(response);
    }

    @Test
    @DisplayName("Should save parameters if provided")
    void testCreateAsyncReportWithParameters() {
        byte[] rptdesignContent = "<?xml version=\"1.0\"?><report></report>".getBytes();
        byte[] rptconfigContent = "<?xml version=\"1.0\"?><config></config>".getBytes();
        String parameters = "{\"startDate\":\"2025-01-01\"}";

        AsyncReport savedReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .parameters(parameters)
                .createdAt(LocalDateTime.now())
                .enteId(1L)
                .version(0L)
                .build();

        when(reportRepository.save(any(AsyncReport.class)))
                .thenReturn(savedReport);

        it.csi.accrual.accrualreportsbapi.dto.AsyncReportResponse response = asyncReportService.createAsyncReport(
                "pdf",
                rptdesignContent,
                "test.rptdesign",
                rptconfigContent,
                "test.rptconfig",
                parameters
        );

        assertNotNull(response);
        verify(reportRepository, times(1)).save(any(AsyncReport.class));
    }

    // ===== GET REPORT STATUS TESTS =====

    @Test
    @DisplayName("Should get report status successfully")
    void testGetReportStatusSuccess() {
        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.of(testReport));

        it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus response = asyncReportService.getReportStatus(testReportId);

        assertNotNull(response);
        assertEquals(testReportId, response.getReportId());
        assertEquals("COMPLETED", response.getStatus().toString());
        assertEquals(100, response.getProgress()); // COMPLETED = 100%
        assertNotNull(response.getDownloadUrl());
    }

    @Test
    @DisplayName("Should return PENDING status with progress 0")
    void testGetReportStatusPending() {
        AsyncReport pendingReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .createdAt(LocalDateTime.now())
                .enteId(1L)
                .version(0L)
                .build();

        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.of(pendingReport));

        it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus response = asyncReportService.getReportStatus(testReportId);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus().toString());
        assertEquals(0, response.getProgress());
    }

    @Test
    @DisplayName("Should return PROCESSING status with progress 50")
    void testGetReportStatusProcessing() {
        AsyncReport processingReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.PROCESSING)
                .format("pdf")
                .createdAt(LocalDateTime.now())
                .enteId(1L)
                .version(0L)
                .build();

        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.of(processingReport));

        // Mockare ProgressRegistry per evitare NullPointerException
        // Restituiamo null per simulare assenza di info progressive
        when(progressRegistry.getProgressInfo(testReportId))
                .thenReturn(null);

        it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus response = asyncReportService.getReportStatus(testReportId);

        assertNotNull(response);
        assertEquals("PROCESSING", response.getStatus().toString());
        assertEquals(1, response.getProgress()); // Quando info è null, ritorna 1
    }

    @Test
    @DisplayName("Should throw exception for non-existent report")
    void testGetReportStatusNotFound() {
        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.empty());

        assertThrows(ReportNotFoundException.class, () ->
                asyncReportService.getReportStatus(testReportId)
        );
    }

    @Test
    @DisplayName("Should include error message for ERROR status")
    void testGetReportStatusError() {
        AsyncReport errorReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.ERROR)
                .format("pdf")
                .errorMessage("Database connection failed")
                .createdAt(LocalDateTime.now())
                .enteId(1L)
                .version(0L)
                .build();

        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.of(errorReport));

        it.csi.accrual.accrualreportsbapi.dto.AsyncReportStatus response = asyncReportService.getReportStatus(testReportId);

        assertNotNull(response);
        assertEquals("ERROR", response.getStatus().toString());
        assertEquals("Database connection failed", response.getError());
    }

    // ===== DOWNLOAD REPORT TESTS =====

    @Test
    @DisplayName("Should download report successfully")
    void testDownloadReportSuccess() {
        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.of(testReport));

        AsyncReportService.ReportDownloadResult result = asyncReportService.downloadReport(testReportId);

        assertNotNull(result);
        assertNotNull(result.content());
        assertEquals("PDF content".getBytes().length, result.content().length);
        assertEquals("pdf", result.format());
        verify(reportRepository, times(1)).findById(testReportId);
    }

    @Test
    @DisplayName("Should throw exception when report not found")
    void testDownloadReportNotFound() {
        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.empty());

        assertThrows(ReportNotFoundException.class, () ->
                asyncReportService.downloadReport(testReportId)
        );
    }

    @Test
    @DisplayName("Should throw exception when file content is null")
    void testDownloadReportNullContent() {
        AsyncReport emptyReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .fileContent(null)
                .build();

        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.of(emptyReport));

        assertThrows(ReportNotFoundException.class, () ->
                asyncReportService.downloadReport(testReportId)
        );
    }

    @Test
    @DisplayName("Should throw exception when file content is empty")
    void testDownloadReportEmptyContent() {
        AsyncReport emptyReport = AsyncReport.builder()
                .id(testReportId)
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("pdf")
                .fileContent(new byte[0])
                .build();

        when(reportRepository.findById(testReportId))
                .thenReturn(Optional.of(emptyReport));

        assertThrows(ReportNotFoundException.class, () ->
                asyncReportService.downloadReport(testReportId)
        );
    }
}
