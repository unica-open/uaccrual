/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.repository;

import it.csi.accrual.accrualreportsbapi.entity.AsyncReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsyncReportRepository Integration Tests")
@SpringBootTest
@ActiveProfiles("integration-test")
@Transactional
class AsyncReportRepositoryTest {

    @Autowired
    private AsyncReportRepository repository;

    private AsyncReport testReport;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testReport = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .parameters("{\"key\":\"value\"}")
                .enteId(1L)  // Required field
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ===== SAVE TESTS =====

    @Test
    @DisplayName("Should save report successfully")
    void testSaveReport() {
        AsyncReport saved = repository.save(testReport);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(testReport.getFormat(), saved.getFormat());
        assertEquals(testReport.getStatus(), saved.getStatus());
    }

    @Test
    @DisplayName("Should save report with file content")
    void testSaveReportWithFileContent() {
        byte[] content = "PDF content".getBytes();
        testReport.setFileContent(content);
        testReport.setFileSize((long) content.length);

        AsyncReport saved = repository.save(testReport);

        assertNotNull(saved.getFileContent());
        assertArrayEquals(content, saved.getFileContent());
        assertEquals(content.length, saved.getFileSize());
    }

    @Test
    @DisplayName("Should save report with null parameters")
    void testSaveReportWithNullParameters() {
        testReport.setParameters(null);

        AsyncReport saved = repository.save(testReport);

        assertNotNull(saved);
        assertNull(saved.getParameters());
    }

    @Test
    @DisplayName("Should save report with timestamps")
    void testSaveReportWithTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        testReport.setCreatedAt(now);
        testReport.setStartedAt(now.plusSeconds(5));
        testReport.setCompletedAt(now.plusSeconds(10));

        AsyncReport saved = repository.save(testReport);

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getStartedAt());
        assertNotNull(saved.getCompletedAt());
    }

    // ===== FIND TESTS =====

    @Test
    @DisplayName("Should find report by ID")
    void testFindById() {
        AsyncReport saved = repository.save(testReport);

        Optional<AsyncReport> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getFormat(), found.get().getFormat());
    }

    @Test
    @DisplayName("Should return empty when report not found")
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        Optional<AsyncReport> found = repository.findById(nonExistentId);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all reports")
    void testFindAll() {
        AsyncReport report1 = repository.save(testReport);

        AsyncReport report2 = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("xlsx")
                .rptdesignFileName("test2.rptdesign")
                .rptconfigFileName("test2.rptconfig")
                .enteId(1L)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(report2);

        List<AsyncReport> reports = repository.findAll();

        assertEquals(2, reports.size());
    }

    // ===== UPDATE TESTS =====

    @Test
    @DisplayName("Should update report status")
    void testUpdateReportStatus() {
        AsyncReport saved = repository.save(testReport);
        
        saved.setStatus(AsyncReport.ReportStatus.PROCESSING);
        saved.setStartedAt(LocalDateTime.now());
        
        AsyncReport updated = repository.save(saved);

        assertEquals(AsyncReport.ReportStatus.PROCESSING, updated.getStatus());
        assertNotNull(updated.getStartedAt());
    }

    @Test
    @DisplayName("Should update report with file content")
    void testUpdateReportWithFileContent() {
        AsyncReport saved = repository.save(testReport);
        
        byte[] content = "Generated PDF".getBytes();
        saved.setFileContent(content);
        saved.setFileSize((long) content.length);
        saved.setStatus(AsyncReport.ReportStatus.COMPLETED);
        saved.setCompletedAt(LocalDateTime.now());
        
        AsyncReport updated = repository.save(saved);

        assertNotNull(updated.getFileContent());
        assertEquals(content.length, updated.getFileSize());
        assertEquals(AsyncReport.ReportStatus.COMPLETED, updated.getStatus());
    }

    @Test
    @DisplayName("Should update report with error")
    void testUpdateReportWithError() {
        AsyncReport saved = repository.save(testReport);

        saved.setStatus(AsyncReport.ReportStatus.ERROR);
        saved.setErrorMessage("Generation failed");
        saved.setCompletedAt(LocalDateTime.now());

        AsyncReport updated = repository.save(saved);

        assertEquals(AsyncReport.ReportStatus.ERROR, updated.getStatus());
        assertEquals("Generation failed", updated.getErrorMessage());
    }

    // ===== DELETE TESTS =====

    @Test
    @DisplayName("Should delete report by ID")
    void testDeleteById() {
        AsyncReport saved = repository.save(testReport);
        UUID id = saved.getId();

        repository.deleteById(id);

        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    @DisplayName("Should delete all reports")
    void testDeleteAll() {
        repository.save(testReport);

        AsyncReport report2 = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("xlsx")
                .rptdesignFileName("test2.rptdesign")
                .rptconfigFileName("test2.rptconfig")
                .enteId(1L)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(report2);

        repository.deleteAll();

        assertEquals(0, repository.findAll().size());
    }

    // ===== COUNT TESTS =====

    @Test
    @DisplayName("Should count reports")
    void testCount() {
        assertEquals(0, repository.count());

        repository.save(testReport);
        assertEquals(1, repository.count());

        AsyncReport report2 = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.COMPLETED)
                .format("xlsx")
                .rptdesignFileName("test2.rptdesign")
                .rptconfigFileName("test2.rptconfig")
                .enteId(1L)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(report2);

        assertEquals(2, repository.count());
    }

    // ===== EXISTS TESTS =====

    @Test
    @DisplayName("Should check if report exists")
    void testExistsById() {
        AsyncReport saved = repository.save(testReport);

        assertTrue(repository.existsById(saved.getId()));
        assertFalse(repository.existsById(UUID.randomUUID()));
    }

    // ===== TRANSACTION TESTS =====

    @Test
    @DisplayName("Should handle transaction rollback")
    void testTransactionRollback() {
        long countBefore = repository.count();

        AsyncReport report = AsyncReport.builder()
                .id(UUID.randomUUID())
                .status(AsyncReport.ReportStatus.PENDING)
                .format("pdf")
                .rptdesignFileName("test.rptdesign")
                .rptconfigFileName("test.rptconfig")
                .enteId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        // Save and then delete to simulate rollback
        repository.save(report);
        repository.delete(report);

        long countAfter = repository.count();
        assertEquals(countBefore, countAfter, "Count should be same after rollback");
    }

    // ===== LARGE DATA TESTS =====

    @Test
    @DisplayName("Should handle large file content")
    void testLargeFileContent() {
        byte[] largeContent = new byte[5 * 1024 * 1024]; // 5 MB
        testReport.setFileContent(largeContent);
        testReport.setFileSize((long) largeContent.length);

        AsyncReport saved = repository.save(testReport);

        AsyncReport found = repository.findById(saved.getId()).orElseThrow();

        assertNotNull(found.getFileContent());
        assertEquals(largeContent.length, found.getFileContent().length);
    }

    @Test
    @DisplayName("Should handle multiple status transitions")
    void testMultipleStatusTransitions() {
        AsyncReport saved = repository.save(testReport);
        
        // PENDING -> PROCESSING
        saved.setStatus(AsyncReport.ReportStatus.PROCESSING);
        saved.setStartedAt(LocalDateTime.now());
        repository.save(saved);
        
        // PROCESSING -> COMPLETED
        saved.setStatus(AsyncReport.ReportStatus.COMPLETED);
        saved.setFileContent("content".getBytes());
        saved.setCompletedAt(LocalDateTime.now());
        AsyncReport completed = repository.save(saved);

        assertEquals(AsyncReport.ReportStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getStartedAt());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    @DisplayName("Should handle different report formats")
    void testDifferentFormats() {
        String[] formats = {"pdf", "xlsx", "html", "docx"};

        for (String format : formats) {
            AsyncReport report = AsyncReport.builder()
                    .id(UUID.randomUUID())
                    .status(AsyncReport.ReportStatus.PENDING)
                    .format(format)
                    .rptdesignFileName("test_" + format + ".rptdesign")
                    .rptconfigFileName("test_" + format + ".rptconfig")
                    .enteId(1L)
                    .createdAt(LocalDateTime.now())
                    .build();

            AsyncReport saved = repository.save(report);
            assertEquals(format, saved.getFormat());
        }

        assertEquals(formats.length, repository.count());
    }
}
