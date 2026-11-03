/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualreportsbapi.validator;

import it.csi.accrual.accrualreportsbapi.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ReportFileValidator {

    @Value("${birt.file.max-size:52428800}")  // 50 MB default
    private long maxFileSize;

    private static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024;  // 50 MB

    // XML Magic bytes: <?xml
    private static final byte[] XML_MAGIC_BYTES = {0x3C, 0x3F, 0x78, 0x6D, 0x6C};

    // Valid BIRT file extensions
    private static final String[] VALID_EXTENSIONS = {".rptdesign", ".rptconfig"};

    /**
     * Valida un file BIRT (rptdesign o rptconfig)
     *
     * @param file Il file da validare
     * @param expectedExtension Estensione attesa (.rptdesign o .rptconfig)
     * @throws InvalidFileException se il file non è valido
     */
    public void validateReportFile(MultipartFile file, String expectedExtension) throws InvalidFileException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or null");
        }

        // 1. Validare dimensione
        validateFileSize(file);

        // 2. Validare estensione
        validateFileExtension(file, expectedExtension);

        // 3. Validare magic bytes (XML)
        validateMagicBytes(file);

        log.debug("File validation passed for: {}", file.getOriginalFilename());
    }

    /**
     * Valida la dimensione del file
     */
    private void validateFileSize(MultipartFile file) throws InvalidFileException {
        long fileSize = file.getSize();
        long maxSize = maxFileSize > 0 ? maxFileSize : DEFAULT_MAX_SIZE;

        if (fileSize > maxSize) {
            throw new InvalidFileException(
                String.format(
                    "File size exceeds maximum allowed (%d MB). Actual size: %d MB",
                    maxSize / (1024 * 1024),
                    fileSize / (1024 * 1024)
                )
            );
        }

        if (fileSize == 0) {
            throw new InvalidFileException("File size is 0 bytes");
        }

        log.debug("File size validation passed: {} bytes", fileSize);
    }

    /**
     * Valida l'estensione del file
     */
    private void validateFileExtension(MultipartFile file, String expectedExtension) throws InvalidFileException {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new InvalidFileException("Filename is null or blank");
        }

        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        String fullExtension = "." + extension;

        // Verificare che l'estensione sia valida
        boolean isValidExtension = false;
        for (String valid : VALID_EXTENSIONS) {
            if (valid.equalsIgnoreCase(fullExtension)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            throw new InvalidFileException(
                String.format(
                    "Invalid file extension: %s. Expected: .rptdesign or .rptconfig",
                    fullExtension
                )
            );
        }

        // Se specificata un'estensione attesa, verificarla
        if (expectedExtension != null && !expectedExtension.isBlank()) {
            String expected = expectedExtension.toLowerCase();
            if (!expected.startsWith(".")) {
                expected = "." + expected;
            }
            if (!fullExtension.equalsIgnoreCase(expected)) {
                throw new InvalidFileException(
                    String.format(
                        "File extension mismatch. Expected: %s, but got: %s",
                        expected,
                        fullExtension
                    )
                );
            }
        }

        log.debug("File extension validation passed: {}", fullExtension);
    }

    /**
     * Valida i magic bytes del file (deve essere XML)
     */
    private void validateMagicBytes(MultipartFile file) throws InvalidFileException {
        try {
            byte[] fileBytes = file.getBytes();

            // Verificare che il file abbia almeno 5 byte (lunghezza di "<?xml")
            if (fileBytes.length < XML_MAGIC_BYTES.length) {
                throw new InvalidFileException(
                    "File is too small to be a valid XML file"
                );
            }

            // Verificare i magic bytes
            for (int i = 0; i < XML_MAGIC_BYTES.length; i++) {
                if (fileBytes[i] != XML_MAGIC_BYTES[i]) {
                    throw new InvalidFileException(
                        "Invalid file format. File does not start with XML declaration (<?xml)"
                    );
                }
            }

            // Validazione aggiuntiva: tentare di leggere come XML
            // (opzionale, ma utile per catch corrupted XML)
            String content = new String(fileBytes, 0, Math.min(100, fileBytes.length), StandardCharsets.UTF_8);
            if (!content.contains("<?xml")) {
                throw new InvalidFileException(
                    "File does not contain valid XML declaration"
                );
            }

            log.debug("Magic bytes validation passed for file: {}", file.getOriginalFilename());

        } catch (IOException e) {
            throw new InvalidFileException(
                "Failed to read file content for validation",
                e
            );
        }
    }

    /**
     * Valida entrambi i file (rptdesign e rptconfig)
     * rptconfig è opzionale
     */
    public void validateReportFiles(MultipartFile rptdesign, MultipartFile rptconfig) throws InvalidFileException {
        if (rptdesign == null || rptdesign.isEmpty()) {
            throw new InvalidFileException("rptdesign file is required");
        }

        validateReportFile(rptdesign, "rptdesign");
        
        // rptconfig è opzionale, se presente lo validiamo
        if (rptconfig != null && !rptconfig.isEmpty()) {
            validateReportFile(rptconfig, "rptconfig");
        }

        log.debug("Report files validation passed (rptconfig is optional)");
    }

    /**
     * Valida il formato richiesto
     */
    public void validateFormat(String format) throws InvalidFileException {
        if (format == null || format.isBlank()) {
            throw new InvalidFileException("Format parameter is required");
        }

        String lowerFormat = format.toLowerCase();
        switch (lowerFormat) {
            case "pdf":
            case "xlsx":
            case "html":
            case "docx":
                log.debug("Format validation passed: {}", lowerFormat);
                return;
            default:
                throw new InvalidFileException(
                    String.format(
                        "Invalid format: %s. Allowed formats: pdf, xlsx, html, docx",
                        format
                    )
                );
        }
    }
}
