/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.math.BigDecimal;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualcommonsrv.be.integration.type.TipoSegnoUserType;
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

import org.hibernate.annotations.Type;

@Entity
@Table(name = "accrual_t_prima_nota_importo", schema = "accrual")
public class AccrualTPrimaNotaImporto extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_prima_nota_importo_prima_nota_importo_id_seq")
    @SequenceGenerator(name = "accrual_t_prima_nota_importo_prima_nota_importo_id_seq", sequenceName = "accrual.accrual_t_prima_nota_importo_prima_nota_importo_id_seq", allocationSize = 50)
    @Column(name = "prima_nota_importo_id")
    private Long id;

    @Column(name = "prima_nota_importo_importo", nullable = false)
    private BigDecimal importo;

    @Type(TipoSegnoUserType.class)
    @Column(name = "prima_nota_importo_segno", nullable = false, columnDefinition = "tipo_segno")
    private TipoSegno segno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prima_nota_id", nullable = false)
    private AccrualTPrimaNota primaNota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_id", nullable = false)
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

    public TipoSegno getSegno() {
        return segno;
    }

    public void setSegno(TipoSegno segno) {
        this.segno = segno;
    }

    public AccrualTPrimaNota getPrimaNota() {
        return primaNota;
    }

    public void setPrimaNota(AccrualTPrimaNota primaNota) {
        this.primaNota = primaNota;
    }

    public AccrualTPdc getPdc() {
        return pdc;
    }

    public void setPdc(AccrualTPdc pdc) {
        this.pdc = pdc;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}