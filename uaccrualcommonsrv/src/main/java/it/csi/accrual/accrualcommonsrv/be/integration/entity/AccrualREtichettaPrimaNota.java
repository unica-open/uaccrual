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
@Table(name = "accrual_r_etichetta_prima_nota", schema = "accrual")
public class AccrualREtichettaPrimaNota extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_etichetta_prima_nota_etichetta_prima_nota_r_id_seq")
    @SequenceGenerator(name = "accrual_r_etichetta_prima_nota_etichetta_prima_nota_r_id_seq", sequenceName = "accrual.accrual_r_etichetta_prima_nota_etichetta_prima_nota_r_id_seq", allocationSize = 50)
    @Column(name = "etichetta_prima_nota_r_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etichetta_id", nullable = false)
    private AccrualTEtichetta etichetta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prima_nota_id", nullable = false)
    private AccrualTPrimaNota primaNota;

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

    public AccrualTEtichetta getEtichetta() {
        return etichetta;
    }

    public void setEtichetta(AccrualTEtichetta etichetta) {
        this.etichetta = etichetta;
    }

    public AccrualTPrimaNota getPrimaNota() {
        return primaNota;
    }

    public void setPrimaNota(AccrualTPrimaNota primaNota) {
        this.primaNota = primaNota;
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