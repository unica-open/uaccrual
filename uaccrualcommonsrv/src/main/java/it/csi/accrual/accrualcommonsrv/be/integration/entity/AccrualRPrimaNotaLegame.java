/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

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
@Table(name = "accrual_r_prima_nota_legame", schema = "accrual")
public class AccrualRPrimaNotaLegame extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_prima_nota_legame_prima_nota_legame_r_id_seq")
    @SequenceGenerator(name = "accrual_r_prima_nota_legame_prima_nota_legame_r_id_seq", sequenceName = "accrual.accrual_r_prima_nota_legame_prima_nota_legame_r_id_seq", allocationSize = 1)
    @Column(name = "prima_nota_legame_r_id")
    private Long id;

    @Column(name = "prima_nota_legame_note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prima_nota_legame_id_da", nullable = false)
    private AccrualTPrimaNota primaNotaDa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prima_nota_legame_id_a", nullable = false)
    private AccrualTPrimaNota primaNotaA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prima_nota_tipo_relazione_id")
    private AccrualDPrimaNotaTipoRelazione primaNotaTipoRelazione;

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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public AccrualTPrimaNota getPrimaNotaDa() {
        return primaNotaDa;
    }

    public void setPrimaNotaDa(AccrualTPrimaNota primaNotaDa) {
        this.primaNotaDa = primaNotaDa;
    }

    public AccrualTPrimaNota getPrimaNotaA() {
        return primaNotaA;
    }

    public void setPrimaNotaA(AccrualTPrimaNota primaNotaA) {
        this.primaNotaA = primaNotaA;
    }

    public AccrualDPrimaNotaTipoRelazione getPrimaNotaTipoRelazione() {
        return primaNotaTipoRelazione;
    }

    public void setPrimaNotaTipoRelazione(AccrualDPrimaNotaTipoRelazione primaNotaTipoRelazione) {
        this.primaNotaTipoRelazione = primaNotaTipoRelazione;
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
