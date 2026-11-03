/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

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
@Table(name = "accrual_r_pdc_legame", schema = "accrual")
public class AccrualRPdcLegame extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_pdc_legame_pdc_legame_r_id_seq")
    @SequenceGenerator(name = "accrual_r_pdc_legame_pdc_legame_r_id_seq", sequenceName = "accrual.accrual_r_pdc_legame_pdc_legame_r_id_seq", allocationSize = 1)
    @Column(name = "pdc_legame_r_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_legame_a_id", nullable = true)
    private AccrualTPdc pdcLegameA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_legame_b_id", nullable = true)
    private AccrualTPdc pdcLegameB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_tipo_relazione_id", nullable = true)
    private AccrualDPdcTipoRelazione pdcTipoRelazione;

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

    public AccrualTPdc getPdcLegameA() {
        return pdcLegameA;
    }

    public void setPdcLegameA(AccrualTPdc pdcLegameA) {
        this.pdcLegameA = pdcLegameA;
    }

    public AccrualTPdc getPdcLegameB() {
        return pdcLegameB;
    }

    public void setPdcLegameB(AccrualTPdc pdcLegameB) {
        this.pdcLegameB = pdcLegameB;
    }

    public AccrualDPdcTipoRelazione getPdcTipoRelazione() {
        return pdcTipoRelazione;
    }

    public void setPdcTipoRelazione(AccrualDPdcTipoRelazione pdcTipoRelazione) {
        this.pdcTipoRelazione = pdcTipoRelazione;
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
    
    /**
     * Restituisce il PDC correlato. In base al contesto, questo potrebbe essere
     * il pdcLegameB che è collegato al pdcLegameA
     */
    public AccrualTPdc getPdcCorrelato() {
        return this.pdcLegameB;
    }
}