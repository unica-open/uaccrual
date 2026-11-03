/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl.dto;

/**
 * DTO per rappresentare una riga del file CSV per il CDU-008: "Elabora saldi PdC 118"
 * 
 * Struttura del file CSV (separatore ;):
 * - codice_fiscale_ente: Codice fiscale dell'ente
 * - codice_fiscale_ente_originale: Codice fiscale dell'ente originale
 * - fonte_dati: Fonte dei dati (es. SIAC)
 * - anno: Anno di riferimento
 * - codice_conto: Codice del conto nel piano dei conti
 * - saldo: Saldo del conto
 */
public class Pdc118SaldoFileRowDTO implements CsvRowDto {

    private String codiceFiscaleEnte;
    private String codiceFiscaleEnteOriginale;
    private String fonteDati;
    private Integer anno;
    private String codiceConto;
    private Double saldo;
    private java.math.BigDecimal saldoBigDecimal;
    
    // Costruttore di default
    public Pdc118SaldoFileRowDTO() {
    }
    
    // Costruttore con parametri
    public Pdc118SaldoFileRowDTO(String codiceFiscaleEnte, String codiceFiscaleEnteOriginale, String fonteDati,
                                Integer anno, String codiceConto, Double saldo) {
        this.codiceFiscaleEnte = codiceFiscaleEnte;
        this.codiceFiscaleEnteOriginale = codiceFiscaleEnteOriginale;
        this.fonteDati = fonteDati;
        this.anno = anno;
        this.codiceConto = codiceConto;
        this.saldo = saldo;
        this.saldoBigDecimal = saldo != null ? java.math.BigDecimal.valueOf(saldo) : null;
    }
    
    // Getter e Setter
    public String getCodiceFiscaleEnte() {
        return codiceFiscaleEnte;
    }
    
    public void setCodiceFiscaleEnte(String codiceFiscaleEnte) {
        this.codiceFiscaleEnte = codiceFiscaleEnte;
    }
    
    public String getCodiceFiscaleEnteOriginale() {
        return codiceFiscaleEnteOriginale;
    }
    
    public void setCodiceFiscaleEnteOriginale(String codiceFiscaleEnteOriginale) {
        this.codiceFiscaleEnteOriginale = codiceFiscaleEnteOriginale;
    }
    
    public String getFonteDati() {
        return fonteDati;
    }
    
    public void setFonteDati(String fonteDati) {
        this.fonteDati = fonteDati;
    }
    
    public Integer getAnno() {
        return anno;
    }
    
    public void setAnno(Integer anno) {
        this.anno = anno;
    }
    
    public String getCodiceConto() {
        return codiceConto;
    }
    
    public void setCodiceConto(String codiceConto) {
        this.codiceConto = codiceConto;
    }
    
    public Double getSaldo() {
        return saldo;
    }
    
    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }
    
    @Override
    public String toString() {
        return "Pdc118SaldoFileRowDTO{" +
                "codiceFiscaleEnte='" + codiceFiscaleEnte + '\'' +
                ", codiceFiscaleEnteOriginale='" + codiceFiscaleEnteOriginale + '\'' +
                ", fonteDati='" + fonteDati + '\'' +
                ", anno=" + anno +
                ", codiceConto='" + codiceConto + '\'' +
                ", saldo=" + saldo +
                '}';
    }

    @Override
    public void fromColumns(String[] columns) throws IllegalArgumentException {
        if (columns.length < 6) {
            throw new IllegalArgumentException("Numero insufficiente di colonne. Attese: 6, Ricevute: " + columns.length);
        }

        this.codiceFiscaleEnte = columns[0].trim();
        this.codiceFiscaleEnteOriginale = columns[1].trim();
        this.fonteDati = columns[2].trim();
        
        try {
            this.anno = Integer.parseInt(columns[3].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Anno non valido: " + columns[3], e);
        }
        
        this.codiceConto = columns[4].trim();
        
        try {
            this.saldo = Double.parseDouble(columns[5].trim());
            this.saldoBigDecimal = java.math.BigDecimal.valueOf(this.saldo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Saldo non valido: " + columns[5], e);
        }
    }

    @Override
    public void validateSemantic() throws IllegalArgumentException {
        if (this.codiceFiscaleEnte == null || this.codiceFiscaleEnte.isEmpty()) {
            throw new IllegalArgumentException("Codice fiscale ente obbligatorio");
        }
        if (this.codiceFiscaleEnteOriginale == null || this.codiceFiscaleEnteOriginale.isEmpty()) {
            throw new IllegalArgumentException("Codice fiscale ente originale obbligatorio");
        }
        if (this.fonteDati == null || this.fonteDati.isEmpty()) {
            throw new IllegalArgumentException("Fonte dati obbligatoria");
        }
        if (this.anno == null) {
            throw new IllegalArgumentException("Anno obbligatorio");
        }
        if (this.codiceConto == null || this.codiceConto.isEmpty()) {
            throw new IllegalArgumentException("Codice conto obbligatorio");
        }
        if (this.saldo == null) {
            throw new IllegalArgumentException("Saldo obbligatorio");
        }
    }
    
    // Metodi aggiuntivi richiesti dai processor
    public String getCodiceConto118() {
        return this.codiceConto;
    }
    
    public java.math.BigDecimal getImporto() {
        return this.saldoBigDecimal;
    }
}