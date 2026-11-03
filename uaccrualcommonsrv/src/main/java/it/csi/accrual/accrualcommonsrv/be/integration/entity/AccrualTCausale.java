/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
@Table(name = "accrual_t_causale", schema = "accrual")
@AttributeOverrides({
    @AttributeOverride(name = "utenteInserimento", column = @Column(name = "gest_utente_ins")),
    @AttributeOverride(name = "utenteAggiornamento", column = @Column(name = "gest_utente_upd")),
    @AttributeOverride(name = "utenteCancellazione", column = @Column(name = "gest_utente_del"))
})
public class AccrualTCausale extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_causale_causale_id_seq")
    @SequenceGenerator(name = "accrual_t_causale_causale_id_seq", sequenceName = "accrual.accrual_t_causale_causale_id_seq", allocationSize = 1)
    @Column(name = "causale_id")
    private Long id;

    @Column(name = "causale_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "causale_descrizione", nullable = false, length = 500)
    private String descrizione;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causale_tipo_id", nullable = false)
    private AccrualDCausaleTipo causaleTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private AccrualTEvento evento;

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

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public AccrualDCausaleTipo getCausaleTipo() {
        return causaleTipo;
    }

    public void setCausaleTipo(AccrualDCausaleTipo causaleTipo) {
        this.causaleTipo = causaleTipo;
    }

    public AccrualTEvento getEvento() {
        return evento;
    }

    public void setEvento(AccrualTEvento evento) {
        this.evento = evento;
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
