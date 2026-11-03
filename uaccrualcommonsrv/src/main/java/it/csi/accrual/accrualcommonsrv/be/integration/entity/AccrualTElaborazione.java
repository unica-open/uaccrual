/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

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
import java.sql.Timestamp;

@Entity
@Table(name = "accrual_t_elaborazione", schema = "accrual")
public class AccrualTElaborazione extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_elaborazione_elaborazione_id_seq")
    @SequenceGenerator(name = "accrual_t_elaborazione_elaborazione_id_seq", sequenceName = "accrual.accrual_t_elaborazione_elaborazione_id_seq", allocationSize = 1)
    @Column(name = "elaborazione_id")
    private Long id;

    @Column(name = "elaborazione_data", nullable = false)
    private Timestamp data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elaborazione_stato_id", nullable = false)
    private AccrualDElaborazioneStato stato;

    @Column(name = "elaborazione_note", nullable = false, length = 1500)
    private String note;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = true) // file_id is nullable in SQL
    private AccrualTFile file;

    @Column(name = "inizio_validita", nullable = false)
    private Timestamp inizioValidita;

    @Column(name = "fine_validita")
    private Timestamp fineValidita;

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

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
    }

    public AccrualDElaborazioneStato getStato() {
        return stato;
    }

    public void setStato(AccrualDElaborazioneStato stato) {
        this.stato = stato;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getInizioValidita() {
        return inizioValidita;
    }

    public void setInizioValidita(Timestamp inizioValidita) {
        this.inizioValidita = inizioValidita;
    }

    public Timestamp getFineValidita() {
        return fineValidita;
    }

    public void setFineValidita(Timestamp fineValidita) {
        this.fineValidita = fineValidita;
    }

    public AccrualTFile getFile() {
        return file;
    }

    public void setFile(AccrualTFile file) {
        this.file = file;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}
