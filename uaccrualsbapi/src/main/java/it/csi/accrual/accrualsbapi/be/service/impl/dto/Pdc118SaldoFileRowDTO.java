/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl.dto;

import java.math.BigDecimal;

/**
 * DTO per rappresentare una riga del file CSV per il CDU-008: "Elabora saldi PdC 118"
 * Allineato al tracciato della Sezione 2.6.4 del documento di analisi.
 *
 * Struttura del file CSV (separatore virgola):
 * 1. Codice fiscale Ente
 * 2. Codice univoco Ente
 * 3. Fonte
 * 4. Anno
 * 5. Codice conto 118
 * 6. Importo
 */
public class Pdc118SaldoFileRowDTO implements CsvRowDto {

    private String codiceFiscaleEnte;
    private String codiceUnivocoEnte;
    private String fonte;
    private Integer anno;
    private String codiceConto118;
    private BigDecimal importo;

    public Pdc118SaldoFileRowDTO() {}

    @Override
    public void fromColumns(String[] columns) throws IllegalArgumentException {
        if (columns.length < 6) {
            throw new IllegalArgumentException("Numero insufficiente di colonne. Attese: 6, Ricevute: " + columns.length);
        }

        // Trimma gli spazi bianchi ma mantiene il contenuto originale
        this.codiceFiscaleEnte = columns[0].trim();
        this.codiceUnivocoEnte = columns[1].trim();
        this.fonte = columns[2].trim();

        try {
            this.anno = Integer.parseInt(columns[3].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Anno non valido: " + columns[3]);
        }

        this.codiceConto118 = columns[4].trim();

        try {
            // Sostituisce l'eventuale virgola decimale con il punto per il parsing BigDecimal
            String importoStr = columns[5].trim().replace(",", ".");
            this.importo = new BigDecimal(importoStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Importo non valido: " + columns[5]);
        }
    }

    @Override
    public void validateSemantic() throws IllegalArgumentException {
        // Il codice fiscale ente nel file è informativo - l'ente reale è determinato dal contesto (file/ente)
        // Quindi non validiamo il CF ma solo che sia presente
        if (isNullOrEmpty(codiceFiscaleEnte)) throw new IllegalArgumentException("Codice fiscale Ente obbligatorio");
        
        if (isNullOrEmpty(codiceUnivocoEnte)) throw new IllegalArgumentException("Codice univoco Ente obbligatorio");
        if (isNullOrEmpty(fonte)) throw new IllegalArgumentException("Fonte obbligatoria");
        if (anno == null) throw new IllegalArgumentException("Anno obbligatorio");

        int currentYear = java.time.Year.now().getValue();
        // Allarghiamo il range per accettare esercizi storici (fino a 10 anni indietro)
        if (anno < currentYear - 10 || anno > currentYear + 1) {
            throw new IllegalArgumentException("Anno " + anno + " non coerente con l'esercizio corrente");
        }

        if (isNullOrEmpty(codiceConto118)) throw new IllegalArgumentException("Codice conto 118 obbligatorio");
        if (importo == null) throw new IllegalArgumentException("Importo obbligatorio");
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Getters and Setters
    public String getCodiceFiscaleEnte() { return codiceFiscaleEnte; }
    public void setCodiceFiscaleEnte(String codiceFiscaleEnte) { this.codiceFiscaleEnte = codiceFiscaleEnte; }

    public String getCodiceUnivocoEnte() { return codiceUnivocoEnte; }
    public void setCodiceUnivocoEnte(String codiceUnivocoEnte) { this.codiceUnivocoEnte = codiceUnivocoEnte; }

    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }

    public Integer getAnno() { return anno; }
    public void setAnno(Integer anno) { this.anno = anno; }

    public String getCodiceConto118() { return codiceConto118; }
    public void setCodiceConto118(String codiceConto118) { this.codiceConto118 = codiceConto118; }

    public BigDecimal getImporto() { return importo; }
    public void setImporto(BigDecimal importo) { this.importo = importo; }

    @Override
    public String toString() {
        return "Pdc118SaldoFileRowDTO{" +
                "ente='" + codiceFiscaleEnte + '\'' +
                ", univoco='" + codiceUnivocoEnte + '\'' +
                ", anno=" + anno +
                ", conto118='" + codiceConto118 + '\'' +
                ", importo=" + importo +
                '}';
    }
}