/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoImporto;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualcommonsrv.be.integration.type.TipoImportoUserType;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "accrual_r_causale_pdc", schema = "accrual")
public class AccrualRCausalePdc extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_causale_pdc_causale_pdc_r_id_seq")
    @SequenceGenerator(name = "accrual_r_causale_pdc_causale_pdc_r_id_seq", sequenceName = "accrual.accrual_r_causale_pdc_causale_pdc_r_id_seq", allocationSize = 1)
    @Column(name = "causale_pdc_r_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causale_id", nullable = false)
    private AccrualTCausale causale;

    @Type(TipoImportoUserType.class)
    @Column(name = "causale_pdc_tipo_importo", nullable = false, columnDefinition = "tipo_importo")
    private TipoImporto tipoImporto;

    @Type(TipoSegnoUserType.class)
    @Column(name = "causale_pdc_segno", nullable = false, columnDefinition = "tipo_segno")
    private TipoSegno segno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_id", nullable = false)
    private AccrualTPdc pdc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

    @Column(name = "inizio_validita", nullable = false)
    private LocalDateTime inizioValidita;

    @Column(name = "fine_validita")
    private LocalDateTime fineValidita;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccrualTCausale getCausale() {
        return causale;
    }

    public void setCausale(AccrualTCausale causale) {
        this.causale = causale;
    }

    public TipoImporto getTipoImporto() {
        return tipoImporto;
    }

    public void setTipoImporto(TipoImporto tipoImporto) {
        this.tipoImporto = tipoImporto;
    }

    public TipoSegno getSegno() {
        return segno;
    }

    public void setSegno(TipoSegno segno) {
        this.segno = segno;
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

    public LocalDateTime getInizioValidita() {
        return inizioValidita;
    }

    public void setInizioValidita(LocalDateTime inizioValidita) {
        this.inizioValidita = inizioValidita;
    }

    public LocalDateTime getFineValidita() {
        return fineValidita;
    }

    public void setFineValidita(LocalDateTime fineValidita) {
        this.fineValidita = fineValidita;
    }
}
