/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.service.impl.dto;

/**
 * DTO per rappresentare una riga del file CSV per il CDU-009: "Elabora variazioni inventariali"
 * 
 * Struttura ipotetica del file CSV (separatore ;):
 * - codice_fiscale_ente: Codice fiscale dell'ente
 * - codice_fiscale_ente_originale: Codice fiscale dell'ente originale
 * - fonte_dati: Fonte dei dati (es. SIAC)
 * - anno: Anno di riferimento
 * - codice_cespite: Codice del cespite
 * - valore_variazione: Valore della variazione
 */
public class VariazioniInventarialiFileRowDTO implements CsvRowDto {

    private String codiceFiscaleEnte;
    private String codiceFiscaleEnteOriginale;
    private String fonteDati;
    private Integer anno;
    private String codiceCespite;
    private Double valoreVariazione;
    private java.math.BigDecimal valoreVariazioneBigDecimal;
    
    // Costruttore di default
    public VariazioniInventarialiFileRowDTO() {
    }
    
    // Costruttore con parametri
    public VariazioniInventarialiFileRowDTO(String codiceFiscaleEnte, String codiceFiscaleEnteOriginale, String fonteDati,
                                          Integer anno, String codiceCespite, Double valoreVariazione) {
        this.codiceFiscaleEnte = codiceFiscaleEnte;
        this.codiceFiscaleEnteOriginale = codiceFiscaleEnteOriginale;
        this.fonteDati = fonteDati;
        this.anno = anno;
        this.codiceCespite = codiceCespite;
        this.valoreVariazione = valoreVariazione;
        this.valoreVariazioneBigDecimal = valoreVariazione != null ? java.math.BigDecimal.valueOf(valoreVariazione) : null;
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
    
    public String getCodiceCespite() {
        return codiceCespite;
    }
    
    public void setCodiceCespite(String codiceCespite) {
        this.codiceCespite = codiceCespite;
    }
    
    public Double getValoreVariazione() {
        return valoreVariazione;
    }
    
    public void setValoreVariazione(Double valoreVariazione) {
        this.valoreVariazione = valoreVariazione;
    }
    
    @Override
    public String toString() {
        return "VariazioniInventarialiFileRowDTO{" +
                "codiceFiscaleEnte='" + codiceFiscaleEnte + '\'' +
                ", codiceFiscaleEnteOriginale='" + codiceFiscaleEnteOriginale + '\'' +
                ", fonteDati='" + fonteDati + '\'' +
                ", anno=" + anno +
                ", codiceCespite='" + codiceCespite + '\'' +
                ", valoreVariazione=" + valoreVariazione +
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
        
        this.codiceCespite = columns[4].trim();
        
        try {
            this.valoreVariazione = Double.parseDouble(columns[5].trim());
            this.valoreVariazioneBigDecimal = java.math.BigDecimal.valueOf(this.valoreVariazione);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valore variazione non valido: " + columns[5], e);
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
        if (this.codiceCespite == null || this.codiceCespite.isEmpty()) {
            throw new IllegalArgumentException("Codice cespite obbligatorio");
        }
        if (this.valoreVariazione == null) {
            throw new IllegalArgumentException("Valore variazione obbligatorio");
        }
    }
    
    // Metodi aggiuntivi richiesti dai processor
    public String getNumeroInventarioBene() {
        return this.codiceCespite;
    }
    
    public String getDescrizioneBene() {
        return this.codiceCespite; // Utilizzo il codice cespite come descrizione temporaneamente
    }
    
    public String getCodiceContoPatrimonialeInizialeBene() {
        return this.codiceCespite; // Placeholder
    }
    
    public String getDescrizioneContoPatrimonialeInizialeBene() {
        return this.codiceCespite; // Placeholder
    }
    
    public Double getValoreRealeInizialeBene() {
        return this.valoreVariazione; // Placeholder
    }
    
    public java.math.BigDecimal getValoreRealeInizialeBeneBigDecimal() {
        return this.valoreVariazioneBigDecimal;
    }
    
    public String getCodiceContoPatrimonialeAttualeBene() {
        return this.codiceCespite; // Placeholder
    }
    
    public String getDescrizioneContoPatrimonialeAttualeBene() {
        return this.codiceCespite; // Placeholder
    }
    
    public Double getValoreRealeAttualeBene() {
        return this.valoreVariazione; // Placeholder
    }
    
    public java.math.BigDecimal getValoreRealeAttualeBeneBigDecimal() {
        return this.valoreVariazioneBigDecimal;
    }
    
    public String getFonte() {
        return this.fonteDati;
    }
}