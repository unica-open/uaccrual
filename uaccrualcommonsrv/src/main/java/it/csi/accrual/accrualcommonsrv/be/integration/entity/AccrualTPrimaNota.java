/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "accrual_t_prima_nota", schema = "accrual")
public class AccrualTPrimaNota extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_prima_nota_prima_nota_id_seq")
    @SequenceGenerator(name = "accrual_t_prima_nota_prima_nota_id_seq", sequenceName = "accrual.accrual_t_prima_nota_prima_nota_id_seq", allocationSize = 50)
    @Column(name = "prima_nota_id")
    private Long id;

    @Column(name = "prima_nota_numero", nullable = false)
    private Integer numero;

    @Column(name = "prima_nota_anno", nullable = false)
    private Integer anno;

    @Column(name = "prima_nota_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Column(name = "prima_nota_data")
    private LocalDateTime data;

    @Column(name = "prima_nota_progressivogiornale")
    private Integer progressivoGiornale;

    @Column(name = "prima_nota_dataregistrazionegiornale")
    private LocalDateTime dataRegistrazioneGiornale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causale_id")
    private AccrualTCausale causale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fonte_id", nullable = false)
    private AccrualDFonte fonte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id")
    private AccrualTEnte ente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prima_nota_tipo_id")
    private AccrualDPrimaNotaTipo tipo;

    @OneToMany(mappedBy = "primaNota", fetch = FetchType.LAZY)
    private Set<AccrualRPrimaNotaStato> stati;

    @OneToMany(mappedBy = "primaNota", fetch = FetchType.LAZY)
    private Set<AccrualTPrimaNotaImporto> importi;

    @OneToMany(mappedBy = "primaNota", fetch = FetchType.LAZY)
    private Set<AccrualRPrimaNotaAttributo> attributi;

    @OneToMany(mappedBy = "primaNotaDa", fetch = FetchType.LAZY)
    private Set<AccrualRPrimaNotaLegame> legamiComeDestinazione;

    @OneToMany(mappedBy = "primaNotaA", fetch = FetchType.LAZY)
    private Set<AccrualRPrimaNotaLegame> legamiComeOrigine;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private AccrualTFile file;
	*/
	
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public Integer getProgressivoGiornale() {
        return progressivoGiornale;
    }

    public void setProgressivoGiornale(Integer progressivoGiornale) {
        this.progressivoGiornale = progressivoGiornale;
    }

    public LocalDateTime getDataRegistrazioneGiornale() {
        return dataRegistrazioneGiornale;
    }

    public void setDataRegistrazioneGiornale(LocalDateTime dataRegistrazioneGiornale) {
        this.dataRegistrazioneGiornale = dataRegistrazioneGiornale;
    }

    public AccrualTCausale getCausale() {
        return causale;
    }

    public void setCausale(AccrualTCausale causale) {
        this.causale = causale;
    }

    public AccrualDFonte getFonte() {
        return fonte;
    }

    public void setFonte(AccrualDFonte fonte) {
        this.fonte = fonte;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }

    public AccrualDPrimaNotaTipo getTipo() {
        return tipo;
    }

    public void setTipo(AccrualDPrimaNotaTipo tipo) {
        this.tipo = tipo;
    }

    public Set<AccrualRPrimaNotaStato> getStati() {
        return stati;
    }

    public void setStati(Set<AccrualRPrimaNotaStato> stati) {
        this.stati = stati;
    }

    public Set<AccrualTPrimaNotaImporto> getImporti() {
        return importi;
    }

    public void setImporti(Set<AccrualTPrimaNotaImporto> importi) {
        this.importi = importi;
    }

	/*
    public AccrualTFile getFile() {
        return file;
    }

    public void setFile(AccrualTFile file) {
        this.file = file;
    }
	*/

    public Set<AccrualRPrimaNotaAttributo> getAttributi() {
        return attributi;
    }

    public void setAttributi(Set<AccrualRPrimaNotaAttributo> attributi) {
        this.attributi = attributi;
    }

    public Set<AccrualRPrimaNotaLegame> getLegamiComeDestinazione() {
        return legamiComeDestinazione;
    }

    public void setLegamiComeDestinazione(Set<AccrualRPrimaNotaLegame> legamiComeDestinazione) {
        this.legamiComeDestinazione = legamiComeDestinazione;
    }

    public Set<AccrualRPrimaNotaLegame> getLegamiComeOrigine() {
        return legamiComeOrigine;
    }

    public void setLegamiComeOrigine(Set<AccrualRPrimaNotaLegame> legamiComeOrigine) {
        this.legamiComeOrigine = legamiComeOrigine;
    }

    /**
     * Restituisce un codice univoco per la prima nota
     * @return codice della prima nota
     */
    public String getCodice() {
        if (numero != null && anno != null) {
            return String.format("%d/%d", numero, anno);
        } else if (id != null) {
            return String.format("PN-%d", id);
        }
        return null;
    }
}