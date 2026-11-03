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
@Table(name = "accrual_r_provincia_regione", schema = "accrual")
public class AccrualRProvinciaRegione extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_provincia_regione_provincia_regione_r_id_seq")
    @SequenceGenerator(name = "accrual_r_provincia_regione_provincia_regione_r_id_seq", sequenceName = "accrual.accrual_r_provincia_regione_provincia_regione_r_id_seq", allocationSize = 1)
    @Column(name = "provincia_regione_r_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia_id", nullable = false)
    private AccrualTProvincia provincia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regione_id", nullable = false)
    private AccrualTRegione regione;

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

    public AccrualTProvincia getProvincia() {
        return provincia;
    }

    public void setProvincia(AccrualTProvincia provincia) {
        this.provincia = provincia;
    }

    public AccrualTRegione getRegione() {
        return regione;
    }

    public void setRegione(AccrualTRegione regione) {
        this.regione = regione;
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
