/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.util;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.AccrualTFile;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.AccrualTFileRepository;
import it.csi.accrual.accrualsbapi.be.service.impl.dto.CsvRowDto; // AGGIUNTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException; // AGGIUNTO

/**
 * Componente per la lettura e validazione dei file CSV
 * Configurabile per gestire diversi formati (CDU-008 e CDU-009)
 */
@Component("utilCsvReader")
public class CsvReader {

    private static final Logger LOG = LoggerFactory.getLogger(CsvReader.class);
    private static final String DEFAULT_DELIMITER = ","; // AGGIUNTO

    @Autowired
    private AccrualTFileRepository accrualTFileRepository;

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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileContent)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (skipHeader) {
                    skipHeader = false;
                    continue; // Salta l'intestazione
                }

                String[] columns = line.split(DEFAULT_DELIMITER);
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
