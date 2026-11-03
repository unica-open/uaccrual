/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbapi.be.service.impl.dto;

import java.math.BigDecimal;

/**
 * DTO per rappresentare una riga del file CSV per il CDU-009: "Elabora variazioni inventariali"
 * Allineato al tracciato della Sezione 2.6.4 del documento di analisi.
 */
public class VariazioniInventarialiFileRowDTO implements CsvRowDto {

    private String codiceFiscaleEnte;
    private String codiceUnivocoEnte;
    private String fonte;
    private Integer anno;
    private String numeroInventarioBene;
    private String descrizioneBene;
    private String codiceContoIniziale;
    private String descrizioneContoIniziale;
    private BigDecimal valoreIniziale;
    private String codiceContoAttuale;
    private String descrizioneContoAttuale;
    private BigDecimal valoreAttuale;

    public VariazioniInventarialiFileRowDTO() {}

    @Override
    public void fromColumns(String[] columns) throws IllegalArgumentException {
        if (columns.length < 12) {
            throw new IllegalArgumentException("Numero insufficiente di colonne. Attese: 12, Ricevute: " + columns.length);
        }

        this.codiceFiscaleEnte = columns[0].trim();
        this.codiceUnivocoEnte = columns[1].trim();
        this.fonte = columns[2].trim();

        try {
            this.anno = Integer.parseInt(columns[3].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Anno non valido: " + columns[3]);
        }

        this.numeroInventarioBene = columns[4].trim();
        this.descrizioneBene = columns[5].trim();
        this.codiceContoIniziale = columns[6].trim();
        this.descrizioneContoIniziale = columns[7].trim();
        this.valoreIniziale = parseBigDecimal(columns[8]);
        this.codiceContoAttuale = columns[9].trim();
        this.descrizioneContoAttuale = columns[10].trim();
        this.valoreAttuale = parseBigDecimal(columns[11]);
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.trim().isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(val.trim().replace(",", "."));
    }

    @Override
    public void validateSemantic() throws IllegalArgumentException {
        if (isNullOrEmpty(codiceFiscaleEnte)) throw new IllegalArgumentException("Codice fiscale Ente obbligatorio");
        if (isNullOrEmpty(numeroInventarioBene)) throw new IllegalArgumentException("Numero inventario bene obbligatorio");
        if (anno == null) throw new IllegalArgumentException("Anno obbligatorio");

        int currentYear = java.time.Year.now().getValue();
        if (anno < currentYear - 2 || anno > currentYear + 1) {
            throw new IllegalArgumentException("Anno " + anno + " non coerente con l'esercizio corrente");
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Getters and Setters
    public String getCodiceFiscaleEnte() { return codiceFiscaleEnte; }
    public String getCodiceUnivocoEnte() { return codiceUnivocoEnte; }
    public String getFonte() { return fonte; }
    public Integer getAnno() { return anno; }
    public String getNumeroInventarioBene() { return numeroInventarioBene; }
    public String getDescrizioneBene() { return descrizioneBene; }
    public String getCodiceContoPatrimonialeInizialeBene() { return codiceContoIniziale; }
    public String getDescrizioneContoPatrimonialeInizialeBene() { return descrizioneContoIniziale; }
    public BigDecimal getValoreRealeInizialeBeneBigDecimal() { return valoreIniziale; }
    public String getCodiceContoPatrimonialeAttualeBene() { return codiceContoAttuale; }
    public String getDescrizioneContoPatrimonialeAttualeBene() { return descrizioneContoAttuale; }
    public BigDecimal getValoreRealeAttualeBeneBigDecimal() { return valoreAttuale; }
}