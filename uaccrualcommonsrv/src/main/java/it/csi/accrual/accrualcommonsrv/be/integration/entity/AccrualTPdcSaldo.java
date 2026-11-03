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
@Table(name = "accrual_t_pdc_saldo", schema = "accrual")
public class AccrualTPdcSaldo extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_pdc_saldo_pdc_saldo_id_seq")
    @SequenceGenerator(name = "accrual_t_pdc_saldo_pdc_saldo_id_seq", sequenceName = "accrual.accrual_t_pdc_saldo_pdc_saldo_id_seq", allocationSize = 50)
    @Column(name = "pdc_saldo_id")
    private Long id;

    @Column(name = "pdc_saldo_importo")
    private BigDecimal importo;

    @Column(name = "pdc_saldo_anno", nullable = false)
    private Integer anno;

    @Column(name = "pdc_codice", nullable = false, length = 200)
    private String codice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_id")
    private AccrualTPdc pdc;

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

    public BigDecimal getImporto() {
        return importo;
    }

    public void setImporto(BigDecimal importo) {
        this.importo = importo;
    }

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public AccrualTPdc getPdc() {
        return pdc;
    }

    public void setPdc(AccrualTPdc pdc) {
        this.pdc = pdc;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}