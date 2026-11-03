/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "accrual_t_etichetta", schema = "accrual")
public class AccrualTEtichetta extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_etichetta_etichetta_id_seq")
    @SequenceGenerator(name = "accrual_t_etichetta_etichetta_id_seq", sequenceName = "accrual.accrual_t_etichetta_etichetta_id_seq", allocationSize = 50)
    @Column(name = "etichetta_id")
    private Long id;

    @Column(name = "etichetta_codice", nullable = false, length = 50, unique = true)
    private String codice;

    @Column(name = "etichetta_descrizione", nullable = false, length = 255)
    private String descrizione;

    @Column(name = "etichetta_anno", nullable = false)
    private Integer anno;

    @Column(name = "etichetta_codice_conto_storico", nullable = false, length = 50, unique = true)
    private String codiceContoStorico;

    @Column(name = "etichetta_descrizione_conto_storico", nullable = false, length = 255)
    private String descrizioneContoStorico;

    @Column(name = "etichetta_valore_conto_storico", nullable = false)
    private BigDecimal valoreContoStorico;

    @Column(name = "etichetta_codice_conto_corrente", nullable = false, length = 50, unique = true)
    private String codiceContoCorrente;

    @Column(name = "etichetta_descrizione_conto_corrente", nullable = false, length = 255)
    private String descrizioneContoCorrente;

    @Column(name = "etichetta_valore_conto_corrente", nullable = false)
    private BigDecimal valoreContoCorrente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fonte_id", nullable = false)
    private AccrualDFonte fonte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public String getCodiceContoStorico() {
        return codiceContoStorico;
    }

    public void setCodiceContoStorico(String codiceContoStorico) {
        this.codiceContoStorico = codiceContoStorico;
    }

    public String getDescrizioneContoStorico() {
        return descrizioneContoStorico;
    }

    public void setDescrizioneContoStorico(String descrizioneContoStorico) {
        this.descrizioneContoStorico = descrizioneContoStorico;
    }

    public BigDecimal getValoreContoStorico() {
        return valoreContoStorico;
    }

    public void setValoreContoStorico(BigDecimal valoreContoStorico) {
        this.valoreContoStorico = valoreContoStorico;
    }

    public String getCodiceContoCorrente() {
        return codiceContoCorrente;
    }

    public void setCodiceContoCorrente(String codiceContoCorrente) {
        this.codiceContoCorrente = codiceContoCorrente;
    }

    public String getDescrizioneContoCorrente() {
        return descrizioneContoCorrente;
    }

    public void setDescrizioneContoCorrente(String descrizioneContoCorrente) {
        this.descrizioneContoCorrente = descrizioneContoCorrente;
    }

    public BigDecimal getValoreContoCorrente() {
        return valoreContoCorrente;
    }

    public void setValoreContoCorrente(BigDecimal valoreContoCorrente) {
        this.valoreContoCorrente = valoreContoCorrente;
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
}