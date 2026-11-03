/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.validator;

import it.csi.accrual.accrualreportsbapi.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportFileValidator Tests")
class ReportFileValidatorTest {

    private ReportFileValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ReportFileValidator();
    }

    // ===== FILE SIZE TESTS =====

    @Test
    @DisplayName("Should accept file with valid size")
    void testValidFileSize() {
        byte[] content = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptdesign", "application/xml", content
        );

        assertDoesNotThrow(() -> validator.validateReportFile(file, "rptdesign"));
    }

    @Test
    @DisplayName("Should reject empty file")
    void testEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptdesign", "application/xml", new byte[0]
        );

        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFile(file, "rptdesign")
        );
    }

    @Test
    @DisplayName("Should reject null file")
    void testNullFile() {
        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFile(null, "rptdesign")
        );
    }

    // ===== EXTENSION TESTS =====

    @Test
    @DisplayName("Should accept .rptdesign extension")
    void testValidRptdesignExtension() {
        byte[] content = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptdesign", "application/xml", content
        );

        assertDoesNotThrow(() -> validator.validateReportFile(file, "rptdesign"));
    }

    @Test
    @DisplayName("Should accept .rptconfig extension")
    void testValidRptconfigExtension() {
        byte[] content = "<?xml version=\"1.0\"?><config></config>".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptconfig", "application/xml", content
        );

        assertDoesNotThrow(() -> validator.validateReportFile(file, "rptconfig"));
    }

    @Test
    @DisplayName("Should reject invalid extension")
    void testInvalidExtension() {
        byte[] content = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", content
        );

        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFile(file, "rptdesign")
        );
    }

    @Test
    @DisplayName("Should reject extension mismatch")
    void testExtensionMismatch() {
        byte[] content = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptconfig", "application/xml", content
        );

        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFile(file, "rptdesign")
        );
    }

    // ===== MAGIC BYTES TESTS =====

    @Test
    @DisplayName("Should accept valid XML magic bytes")
    void testValidXmlMagicBytes() {
        byte[] xmlContent = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptdesign", "application/xml", xmlContent
        );

        assertDoesNotThrow(() -> validator.validateReportFile(file, "rptdesign"));
    }

    @Test
    @DisplayName("Should reject non-XML file (invalid magic bytes)")
    void testInvalidMagicBytes() {
        byte[] content = "This is not XML".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptdesign", "application/xml", content
        );

        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFile(file, "rptdesign")
        );
    }

    @Test
    @DisplayName("Should reject file with only 3 bytes")
    void testTooShortFile() {
        byte[] content = "<?x".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.rptdesign", "application/xml", content
        );

        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFile(file, "rptdesign")
        );
    }

    // ===== FORMAT VALIDATION TESTS =====

    @Test
    @DisplayName("Should accept pdf format")
    void testValidPdfFormat() {
        assertDoesNotThrow(() -> validator.validateFormat("pdf"));
    }

    @Test
    @DisplayName("Should accept xlsx format")
    void testValidXlsxFormat() {
        assertDoesNotThrow(() -> validator.validateFormat("xlsx"));
    }

    @Test
    @DisplayName("Should accept html format")
    void testValidHtmlFormat() {
        assertDoesNotThrow(() -> validator.validateFormat("html"));
    }

    @Test
    @DisplayName("Should accept docx format")
    void testValidDocxFormat() {
        assertDoesNotThrow(() -> validator.validateFormat("docx"));
    }

    @Test
    @DisplayName("Should accept format case-insensitive")
    void testFormatCaseInsensitive() {
        assertDoesNotThrow(() -> validator.validateFormat("PDF"));
        assertDoesNotThrow(() -> validator.validateFormat("XlSx"));
        assertDoesNotThrow(() -> validator.validateFormat("DOCX"));
    }

    @Test
    @DisplayName("Should reject invalid format")
    void testInvalidFormat() {
        assertThrows(InvalidFileException.class, () ->
                validator.validateFormat("doc")
        );
    }

    @Test
    @DisplayName("Should reject null format")
    void testNullFormat() {
        assertThrows(InvalidFileException.class, () ->
                validator.validateFormat(null)
        );
    }

    @Test
    @DisplayName("Should reject empty format")
    void testEmptyFormat() {
        assertThrows(InvalidFileException.class, () ->
                validator.validateFormat("")
        );
    }

    // ===== DUAL FILE VALIDATION =====

    @Test
    @DisplayName("Should accept both files valid")
    void testValidBothFiles() {
        byte[] content = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile rptdesign = new MockMultipartFile(
                "rptdesign", "test.rptdesign", "application/xml", content
        );
        MockMultipartFile rptconfig = new MockMultipartFile(
                "rptconfig", "test.rptconfig", "application/xml", content
        );

        assertDoesNotThrow(() -> validator.validateReportFiles(rptdesign, rptconfig));
    }

    @Test
    @DisplayName("Should reject null rptdesign")
    void testNullRptdesign() {
        byte[] content = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile rptconfig = new MockMultipartFile(
                "rptconfig", "test.rptconfig", "application/xml", content
        );

        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFiles(null, rptconfig)
        );
    }

    @Test
    @DisplayName("Should reject null rptconfig")
    void testNullRptconfig() {
        byte[] content = "<?xml version=\"1.0\"?><report></report>".getBytes();
        MockMultipartFile rptdesign = new MockMultipartFile(
                "rptdesign", "test.rptdesign", "application/xml", content
        );

        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFiles(rptdesign, null)
        );
    }

    @Test
    @DisplayName("Should reject both files null")
    void testBothFilesNull() {
        assertThrows(InvalidFileException.class, () ->
                validator.validateReportFiles(null, null)
        );
    }
}
