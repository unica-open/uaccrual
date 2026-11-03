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
@Table(name = "accrual_r_evento_attributo", schema = "accrual")
public class AccrualREventoAttributo extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_r_evento_attributo_evento_attributo_r_id_seq")
    @SequenceGenerator(name = "accrual_r_evento_attributo_evento_attributo_r_id_seq", sequenceName = "accrual.accrual_r_evento_attributo_evento_attributo_r_id_seq", allocationSize = 1)
    @Column(name = "evento_attributo_r_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private AccrualTEvento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attributo_id", nullable = false)
    private AccrualDAttributo attributo;

    @Column(name = "inizio_validita", nullable = false)
    private LocalDateTime inizioValidita;

    @Column(name = "fine_validita")
    private LocalDateTime fineValidita;

    @Column(name = "evento_attributo_obbligatorio", nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean obbligatorio = false;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccrualTEvento getEvento() {
        return evento;
    }

    public void setEvento(AccrualTEvento evento) {
        this.evento = evento;
    }

    public AccrualDAttributo getAttributo() {
        return attributo;
    }

    public void setAttributo(AccrualDAttributo attributo) {
        this.attributo = attributo;
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

    public Boolean getObbligatorio() {
        return obbligatorio;
    }

    public void setObbligatorio(Boolean obbligatorio) {
        this.obbligatorio = obbligatorio;
    }
}
