/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "accrual_r_causale_stato", schema = "accrual")
public class AccrualRCausaleStato extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_causale_stato_causale_stato_r_id_seq")
    @SequenceGenerator(name = "accrual_r_causale_stato_causale_stato_r_id_seq", sequenceName = "accrual.accrual_r_causale_stato_causale_stato_r_id_seq", allocationSize = 1)
    @Column(name = "causale_stato_r_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causale_id", nullable = false)
    private AccrualTCausale causale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causale_stato_id", nullable = false)
    private AccrualDCausaleStato causaleStato;

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

    public AccrualDCausaleStato getCausaleStato() {
        return causaleStato;
    }

    public void setCausaleStato(AccrualDCausaleStato causaleStato) {
        this.causaleStato = causaleStato;
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
