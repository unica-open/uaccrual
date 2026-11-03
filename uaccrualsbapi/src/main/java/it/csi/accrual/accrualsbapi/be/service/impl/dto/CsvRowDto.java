/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl.dto;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

/**
 * Interfaccia generica per un DTO che rappresenta una riga di un file CSV.
 * Fornisce metodi per popolare il DTO da un array di stringhe (colonne)
 * e per eseguire validazioni semantiche sui dati parsificati.
 */
public interface CsvRowDto {

    /**
     * Popola le proprietà del DTO a partire da un array di stringhe,
     * dove ogni stringa rappresenta una colonna della riga CSV.
     * Deve gestire la conversione dei tipi e il trimming degli spazi.
     *
     * @param columns Un array di stringhe, ogni elemento è una colonna della riga CSV.
     * @throws IllegalArgumentException se il numero di colonne non corrisponde
     *                                  o se si verificano errori di parsing.
     */
    void fromColumns(String[] columns) throws IllegalArgumentException;

    /**
     * Esegue validazioni semantiche sulle proprietà del DTO dopo che sono state popolate.
     * Questo può includere controlli di nullità, formati specifici, intervalli di valori, ecc.
     *
     * @throws IllegalArgumentException se una delle validazioni fallisce.
     */
    void validateSemantic() throws IllegalArgumentException;
}
