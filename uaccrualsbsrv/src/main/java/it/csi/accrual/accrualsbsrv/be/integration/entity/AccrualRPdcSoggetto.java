/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.entity;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import java.time.LocalDateTime;

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
@Table(name = "accrual_r_pdc_soggetto", schema = "accrual")
public class AccrualRPdcSoggetto extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_pdc_soggetto_pdc_soggetto_r_id_seq")
    @SequenceGenerator(name = "accrual_r_pdc_soggetto_pdc_soggetto_r_id_seq", sequenceName = "accrual.accrual_r_pdc_soggetto_pdc_soggetto_r_id_seq", allocationSize = 1)
    @Column(name = "pdc_soggetto_r_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_id", nullable = false)
    private AccrualTPdc pdc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soggetto_id", nullable = false)
    private AccrualTSoggetto soggetto;

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

    public AccrualTPdc getPdc() {
        return pdc;
    }

    public void setPdc(AccrualTPdc pdc) {
        this.pdc = pdc;
    }

    public AccrualTSoggetto getSoggetto() {
        return soggetto;
    }

    public void setSoggetto(AccrualTSoggetto soggetto) {
        this.soggetto = soggetto;
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