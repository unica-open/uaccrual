/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTFileRepository;
import it.csi.accrual.accrualsbapi.be.service.impl.dto.CsvRowDto; // AGGIUNTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException; // AGGIUNTO

/**
 * Componente per la lettura e validazione dei file CSV
 * Configurabile per gestire diversi formati (CDU-008 e CDU-009)
 */
@Component
public class CsvReader {

    private static final Logger LOG = LoggerFactory.getLogger(CsvReader.class);
    private static final String DEFAULT_DELIMITER = ";"; // Punto e virgola come default primario

    @Value("${csv.encoding.default:UTF-8}")
    private String csvEncodingDefault;

    @Value("${csv.encoding.fallback:ISO-8859-1}")
    private String csvEncodingFallback;

    @Autowired
    private AccrualTFileRepository accrualTFileRepository;

    /**
     * Rileva il charset più appropriato per il contenuto del file.
     * Controlla la presenza di BOM e prova a determinare se l'encoding è UTF-8 o meno.
     * 
     * @param content Contenuto binario del file
     * @return Charset da utilizzare per la lettura
     */
    private Charset detectCharset(byte[] content) {
        // Controllo BOM (Byte Order Mark) per UTF-8
        if (content.length >= 3 && 
            (content[0] & 0xFF) == 0xEF && 
            (content[1] & 0xFF) == 0xBB && 
            (content[2] & 0xFF) == 0xBF) {
            LOG.info("Rilevato BOM UTF-8 - utilizzo UTF-8 per la lettura");
            return StandardCharsets.UTF_8;
        }
        
        // Prova a leggere come UTF-8 e verifica se ci sono caratteri non validi
        try {
            String test = new String(content, StandardCharsets.UTF_8);
            // Controllo se la stringa contiene caratteri di replacement o sequenze invalide
            if (!test.contains("")) {
                LOG.info("Encoding UTF-8 valido per il file");
                return StandardCharsets.UTF_8;
            }
        } catch (Exception e) {
            LOG.debug("UTF-8 non valido, fallback a {}", csvEncodingFallback);
        }
        
        // Fallback a ISO-8859-1 (compatibile con Windows-1252 per la maggior parte dei caratteri)
        LOG.info("Fallback a {} per la lettura del file", csvEncodingFallback);
        return Charset.forName(csvEncodingFallback);
    }

    /**
     * Metodo generico per leggere e mappare un file CSV a una lista di DTO specifici.
     * 
     * @param <T> Il tipo di DTO che implementa CsvRowDto.
     * @param flussoId ID del flusso.
     * @param targetDtoClass La classe del DTO target.
     * @param skipHeader Se true, salta la prima riga (intestazione).
     * @return Lista di DTO mappati dalle righe del file.
     * @throws Exception se si verifica un errore durante la lettura, il parsing o la validazione.
     */
    public <T extends CsvRowDto> List<T> readAndMapFile(String flussoId, Class<T> targetDtoClass, boolean skipHeader) throws Exception {
        LOG.info("Lettura e mappatura del file CSV per flusso ID: {} al DTO: {}", flussoId, targetDtoClass.getSimpleName());

        // Recupera il file dal repository
        AccrualTFile fileEntity = accrualTFileRepository.findById(Long.parseLong(flussoId))
                .orElseThrow(() -> new Exception("File non trovato per flusso ID: " + flussoId));

        byte[] fileContent = fileEntity.getFile();
        List<T> dtoList = new ArrayList<>();
        int lineNumber = 0;

        // Rileva automaticamente l'encoding del file (UTF-8 con BOM o fallback ISO-8859-1)
        Charset detectedCharset = detectCharset(fileContent);
        byte[] contentToRead = fileContent;
        if (detectedCharset == StandardCharsets.UTF_8 &&
                fileContent.length >= 3 &&
                (fileContent[0] & 0xFF) == 0xEF &&
                (fileContent[1] & 0xFF) == 0xBB &&
                (fileContent[2] & 0xFF) == 0xBF) {
            contentToRead = java.util.Arrays.copyOfRange(fileContent, 3, fileContent.length);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contentToRead), detectedCharset))) {
            String line;
            int nonEmptyLineCount = 0; // Contatore per le righe non vuote

            while ((line = reader.readLine()) != null) {
                lineNumber++; // 1-based line number for logging

                // Normalizza la riga: rimuovi CR (\r) residui per compatibilità Windows/Linux
                line = line.replace("\r", "").trim();

                // Salta righe vuote o di soli spazi
                if (line.isEmpty()) {
                    continue;
                }

                // Conta le righe non vuote
                nonEmptyLineCount++;

                // Salta l'intestazione se richiesto (prima riga non vuota)
                if (skipHeader && nonEmptyLineCount == 1) {
                    LOG.info("Salto riga di intestazione (riga fisica {})", lineNumber);
                    continue;
                }

                // Splitting CSV rispettando i campi quotati (es. "valore;con;punti")
                String[] columns = splitCsvLine(line, DEFAULT_DELIMITER.charAt(0));
                if (columns.length < 2) {
                    char alternateDelimiter = DEFAULT_DELIMITER.equals(";") ? ',' : ';';
                    String[] altColumns = splitCsvLine(line, alternateDelimiter);
                    if (altColumns.length > columns.length) {
                        columns = altColumns;
                    }
                }

                T dto = targetDtoClass.getDeclaredConstructor().newInstance();
                
                try {
                    dto.fromColumns(columns); // Popola il DTO dalla riga CSV
                    dto.validateSemantic();  // Valida la semantica del DTO
                    dtoList.add(dto);
                } catch (IllegalArgumentException e) {
                    LOG.error("Errore di parsing o validazione semantica alla riga {} del file {}: {}", lineNumber, fileEntity.getNome(), e.getMessage());
                    // Qui potresti voler loggare l'errore e continuare o interrompere l'elaborazione.
                    // Per ora, rilanciamo l'eccezione come errore bloccante.
                    throw new Exception("Errore alla riga " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            LOG.error("Errore durante la lettura del file CSV per flusso ID: {}", flussoId, e);
            throw new Exception("Errore durante la lettura del file CSV", e);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOG.error("Errore durante la creazione del DTO di riga per flusso ID: {}", flussoId, e);
            throw new Exception("Errore interno: impossibile creare DTO per riga CSV.", e);
        }
        
        LOG.info("File letto e mappato con successo. Numero di righe processate: {}", dtoList.size());
        return dtoList;
    }

    /**
     * Metodo per validare il formato generale del file CSV, inclusa l'intestazione se presente.
     *
     * @param lines Righe del file CSV.
     * @param expectedHeaderColumns Numero di colonne attese per l'intestazione.
     * @param hasHeader Indica se il file ha un'intestazione.
     * @return true se il formato è valido, false altrimenti.
     * @deprecated Questo metodo è meno robusto; preferire {@link #readAndMapFile(String, Class, boolean)}.
     */
    /**
     * Splitta una riga CSV rispettando i campi racchiusi tra doppi apici.
     * Un campo quotato può contenere il delimitatore senza che venga interpretato come separatore.
     * Le virgolette esterne vengono rimosse dal valore risultante.
     */
    private String[] splitCsvLine(String line, char delimiter) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result.toArray(new String[0]);
    }

    @Deprecated
    public boolean validateFormat(List<String> lines, int expectedHeaderColumns, boolean hasHeader) {
        if (lines.isEmpty()) {
            LOG.warn("File vuoto");
            return false;
        }

        int startLine = hasHeader ? 1 : 0; // Se ha header, inizia a controllare dalla seconda riga

        if (lines.size() <= startLine && hasHeader) { // Solo header presente, nessun dato
            LOG.warn("File contiene solo intestazione e nessun dato.");
            return false;
        }

        // Se c'è un header, controlla le sue colonne. Altrimenti, controlla la prima riga di dati.
        String lineToCheck = lines.get(0);
        String[] columns = lineToCheck.split(DEFAULT_DELIMITER);

        if (columns.length != expectedHeaderColumns) {
            LOG.warn("Numero di colonne nell'intestazione/prima riga non corrispondente. Atteso: {}, Trovato: {}", expectedHeaderColumns, columns.length);
            return false;
        }
        
        // Esegue un controllo base sul numero di colonne per le righe dati (se presenti)
        for (int i = startLine; i < lines.size(); i++) {
            String[] dataColumns = lines.get(i).split(DEFAULT_DELIMITER);
            if (dataColumns.length != expectedHeaderColumns) { // Assumiamo che il numero di colonne dei dati sia uguale a quello dell'header
                LOG.warn("Numero di colonne non corrispondente alla riga dati {}. Atteso: {}, Trovato: {}", i + 1, expectedHeaderColumns, dataColumns.length);
                return false;
            }
        }

        LOG.info("Validazione formato CSV completata con successo");
        return true;
    }
}
