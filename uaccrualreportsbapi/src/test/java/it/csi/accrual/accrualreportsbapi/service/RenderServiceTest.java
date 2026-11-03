/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.service;

import it.csi.accrual.accrualreportsbapi.exception.ReportGenerationException;
import org.eclipse.birt.report.engine.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RenderService Tests")
@ExtendWith(MockitoExtension.class)
class RenderServiceTest {

    @Mock
    private ReportEngine reportEngine;

    @Mock
    private IReportRunnable reportRunnable;

    @Mock
    private IRunAndRenderTask runAndRenderTask;

    @InjectMocks
    private RenderService renderService;

    @TempDir
    Path tempDir;

    private String testRptdesignPath;
    private String testRptconfigPath;

    @BeforeEach
    void setUp() throws IOException {
        // Set up temp directory
        ReflectionTestUtils.setField(renderService, "tempDir", tempDir.toString());
        ReflectionTestUtils.setField(renderService, "timeoutSeconds", 180);

        // Create test files
        testRptdesignPath = tempDir.resolve("test.rptdesign").toString();
        testRptconfigPath = tempDir.resolve("test.rptconfig").toString();
        
        Files.write(Paths.get(testRptdesignPath), "<?xml version=\"1.0\"?><report></report>".getBytes());
        Files.write(Paths.get(testRptconfigPath), "<?xml version=\"1.0\"?><config></config>".getBytes());
    }

    // ===== GENERATE REPORT TESTS =====

    @Test
    @DisplayName("Should generate PDF report successfully")
    void testGeneratePdfReportSuccess() throws Exception {
        byte[] expectedContent = "PDF content".getBytes();
        
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);
        
        doAnswer(invocation -> {
            RenderOption option = invocation.getArgument(0);
            ByteArrayOutputStream out = (ByteArrayOutputStream) option.getOutputStream();
            out.write(expectedContent);
            return null;
        }).when(runAndRenderTask).setRenderOption(any(RenderOption.class));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("testParam", "value");

        byte[] result = renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "pdf",
            parameters
        );

        assertNotNull(result);
        assertEquals(expectedContent.length, result.length);
        verify(reportEngine).openReportDesign(testRptdesignPath);
        verify(reportEngine).createRunAndRenderTask(reportRunnable);
        verify(runAndRenderTask).setParameterValue("testParam", "value");
        verify(runAndRenderTask).run();
        verify(runAndRenderTask).close();
    }

    @Test
    @DisplayName("Should generate XLSX report successfully")
    void testGenerateXlsxReportSuccess() throws Exception {
        byte[] expectedContent = "XLSX content".getBytes();
        
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);
        
        doAnswer(invocation -> {
            RenderOption option = invocation.getArgument(0);
            ByteArrayOutputStream out = (ByteArrayOutputStream) option.getOutputStream();
            out.write(expectedContent);
            return null;
        }).when(runAndRenderTask).setRenderOption(any(RenderOption.class));

        byte[] result = renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "xlsx",
            null
        );

        assertNotNull(result);
        assertEquals(expectedContent.length, result.length);
    }

    @Test
    @DisplayName("Should generate HTML report successfully")
    void testGenerateHtmlReportSuccess() throws Exception {
        byte[] expectedContent = "<html>Report</html>".getBytes();
        
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);
        
        doAnswer(invocation -> {
            RenderOption option = invocation.getArgument(0);
            ByteArrayOutputStream out = (ByteArrayOutputStream) option.getOutputStream();
            out.write(expectedContent);
            return null;
        }).when(runAndRenderTask).setRenderOption(any(RenderOption.class));

        byte[] result = renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "html",
            null
        );

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    @DisplayName("Should throw exception when rptdesign file not found")
    void testGenerateReportRptdesignNotFound() {
        String nonExistentPath = "/path/to/nonexistent.rptdesign";

        assertThrows(ReportGenerationException.class, () ->
            renderService.generateReport(
                nonExistentPath,
                testRptconfigPath,
                "pdf",
                null
            )
        );
    }

    @Test
    @DisplayName("Should throw exception when report design cannot be opened")
    void testGenerateReportDesignOpenFailure() throws Exception {
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(null);

        assertThrows(ReportGenerationException.class, () ->
            renderService.generateReport(
                testRptdesignPath,
                testRptconfigPath,
                "pdf",
                null
            )
        );
    }

    @Test
    @DisplayName("Should handle parameters correctly")
    void testGenerateReportWithMultipleParameters() throws Exception {
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", "2025-01-01");
        parameters.put("endDate", "2025-01-31");
        parameters.put("userId", 12345);

        renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "pdf",
            parameters
        );

        verify(runAndRenderTask).setParameterValue("startDate", "2025-01-01");
        verify(runAndRenderTask).setParameterValue("endDate", "2025-01-31");
        verify(runAndRenderTask).setParameterValue("userId", 12345);
    }

    @Test
    @DisplayName("Should handle null parameters")
    void testGenerateReportWithNullParameters() throws Exception {
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);

        renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "pdf",
            null
        );

        verify(runAndRenderTask, never()).setParameterValue(anyString(), any());
    }

    @Test
    @DisplayName("Should handle empty parameters map")
    void testGenerateReportWithEmptyParameters() throws Exception {
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);

        renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "pdf",
            new HashMap<>()
        );

        verify(runAndRenderTask, never()).setParameterValue(anyString(), any());
    }

    @Test
    @DisplayName("Should fallback to PDF for DOCX format")
    void testGenerateDocxReportFallbackToPdf() throws Exception {
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);

        renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "docx",
            null
        );

        verify(runAndRenderTask).run();
    }

    @Test
    @DisplayName("Should handle unknown format with default PDF")
    void testGenerateReportUnknownFormat() throws Exception {
        when(reportEngine.openReportDesign(testRptdesignPath)).thenReturn(reportRunnable);
        when(reportEngine.createRunAndRenderTask(reportRunnable)).thenReturn(runAndRenderTask);

        renderService.generateReport(
            testRptdesignPath,
            testRptconfigPath,
            "unknown",
            null
        );

        verify(runAndRenderTask).run();
    }

    // ===== MIME TYPE TESTS =====

    @Test
    @DisplayName("Should return correct MIME type for PDF")
    void testGetMimeTypePdf() {
        assertEquals("application/pdf", renderService.getMimeType("pdf"));
        assertEquals("application/pdf", renderService.getMimeType("PDF"));
    }

    @Test
    @DisplayName("Should return correct MIME type for XLSX")
    void testGetMimeTypeXlsx() {
        String expected = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        assertEquals(expected, renderService.getMimeType("xlsx"));
        assertEquals(expected, renderService.getMimeType("xls"));
    }

    @Test
    @DisplayName("Should return correct MIME type for HTML")
    void testGetMimeTypeHtml() {
        assertEquals("text/html", renderService.getMimeType("html"));
    }

    @Test
    @DisplayName("Should return correct MIME type for DOCX")
    void testGetMimeTypeDocx() {
        String expected = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        assertEquals(expected, renderService.getMimeType("docx"));
    }

    @Test
    @DisplayName("Should return default MIME type for unknown format")
    void testGetMimeTypeUnknown() {
        assertEquals("application/octet-stream", renderService.getMimeType("unknown"));
        assertEquals("application/octet-stream", renderService.getMimeType(null));
    }

    // ===== FILE EXTENSION TESTS =====

    @Test
    @DisplayName("Should return correct file extension for PDF")
    void testGetFileExtensionPdf() {
        assertEquals(".pdf", renderService.getFileExtension("pdf"));
    }

    @Test
    @DisplayName("Should return correct file extension for XLSX")
    void testGetFileExtensionXlsx() {
        assertEquals(".xlsx", renderService.getFileExtension("xlsx"));
        assertEquals(".xls", renderService.getFileExtension("xls"));
    }

    @Test
    @DisplayName("Should return correct file extension for HTML")
    void testGetFileExtensionHtml() {
        assertEquals(".html", renderService.getFileExtension("html"));
    }

    @Test
    @DisplayName("Should return correct file extension for DOCX")
    void testGetFileExtensionDocx() {
        assertEquals(".docx", renderService.getFileExtension("docx"));
    }

    @Test
    @DisplayName("Should return default extension for unknown format")
    void testGetFileExtensionUnknown() {
        assertEquals(".pdf", renderService.getFileExtension("unknown"));
        assertEquals(".pdf", renderService.getFileExtension(null));
    }

    // ===== TEMP DIR TESTS =====

    @Test
    @DisplayName("Should create temp directory if not exists")
    void testEnsureTempDirExists() throws Exception {
        Path newTempDir = tempDir.resolve("new_temp");
        ReflectionTestUtils.setField(renderService, "tempDir", newTempDir.toString());

        assertFalse(Files.exists(newTempDir));
        
        renderService.ensureTempDirExists();
        
        assertTrue(Files.exists(newTempDir));
    }

    @Test
    @DisplayName("Should not fail if temp directory already exists")
    void testEnsureTempDirExistsAlreadyExists() throws Exception {
        assertTrue(Files.exists(tempDir));
        
        assertDoesNotThrow(() -> renderService.ensureTempDirExists());
    }

    // ===== SAVE TEMP FILE TESTS =====

    @Test
    @DisplayName("Should save temp file successfully")
    void testSaveTempFileSuccess() throws Exception {
        byte[] content = "Test content".getBytes();
        String filename = "test_file.txt";

        String savedPath = renderService.saveTempFile(content, filename);

        assertNotNull(savedPath);
        assertTrue(Files.exists(Paths.get(savedPath)));
        assertArrayEquals(content, Files.readAllBytes(Paths.get(savedPath)));
    }

    @Test
    @DisplayName("Should create temp directory when saving file")
    void testSaveTempFileCreatesDirectory() throws Exception {
        Path newTempDir = tempDir.resolve("new_temp_2");
        ReflectionTestUtils.setField(renderService, "tempDir", newTempDir.toString());

        byte[] content = "Test content".getBytes();
        String filename = "test_file.txt";

        String savedPath = renderService.saveTempFile(content, filename);

        assertNotNull(savedPath);
        assertTrue(Files.exists(Paths.get(savedPath)));
    }

    // ===== CLEANUP TESTS =====

    @Test
    @DisplayName("Should cleanup temp files successfully")
    void testCleanupTempFilesSuccess() throws Exception {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        
        Files.write(file1, "content1".getBytes());
        Files.write(file2, "content2".getBytes());

        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));

        renderService.cleanupTempFiles(file1.toString(), file2.toString());

        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
    }

    @Test
    @DisplayName("Should not fail when cleaning up non-existent files")
    void testCleanupNonExistentFiles() {
        assertDoesNotThrow(() -> 
            renderService.cleanupTempFiles("/path/to/nonexistent1.txt", "/path/to/nonexistent2.txt")
        );
    }

    @Test
    @DisplayName("Should handle mixed existing and non-existing files")
    void testCleanupMixedFiles() throws Exception {
        Path existingFile = tempDir.resolve("existing.txt");
        Files.write(existingFile, "content".getBytes());

        assertTrue(Files.exists(existingFile));

        renderService.cleanupTempFiles(
            existingFile.toString(), 
            "/path/to/nonexistent.txt"
        );

        assertFalse(Files.exists(existingFile));
    }

    @Test
    @DisplayName("Should handle empty cleanup array")
    void testCleanupEmptyArray() {
        assertDoesNotThrow(() -> renderService.cleanupTempFiles());
    }
}
