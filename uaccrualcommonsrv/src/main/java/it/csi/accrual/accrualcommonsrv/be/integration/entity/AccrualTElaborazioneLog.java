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

@Entity
@Table(name = "accrual_t_elaborazione_log", schema = "accrual")
public class AccrualTElaborazioneLog extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_elaborazione_log_elaborazione_log_id_seq")
    @SequenceGenerator(name = "accrual_t_elaborazione_log_elaborazione_log_id_seq", sequenceName = "accrual.accrual_t_elaborazione_log_elaborazione_log_id_seq", allocationSize = 1)
    @Column(name = "elaborazione_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elaborazione_id", nullable = true)
    private AccrualTElaborazione elaborazione;


    @Column(name = "elaborazione_log_operazione", nullable = false, length = 2500)
    private String operazione;

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

    public AccrualTElaborazione getElaborazione() {
        return elaborazione;
    }

    public void setElaborazione(AccrualTElaborazione elaborazione) {
        this.elaborazione = elaborazione;
    }

    /**
     * Metodo helper per ottenere il file attraverso l'elaborazione
     */
    public AccrualTFile getElaborazioneFile() {
        return elaborazione != null ? elaborazione.getFile() : null;
    }

    /**
     * Metodo helper per impostare il file attraverso l'elaborazione
     * ATTENZIONE: Questo metodo presuppone che l'elaborazione esistente sia già stata creata
     * e collegata al file. Non crea nuove istanze di AccrualTElaborazione.
     */
    public void setElaborazioneFile(AccrualTFile file) {
        // Non creiamo nuove istanze di elaborazione per evitare problemi di integrità
        // Cerchiamo invece di collegare a un'elaborazione esistente
        if (this.elaborazione != null) {
            this.elaborazione.setFile(file);
        }
        // Altrimenti, il collegamento sarà gestito esternamente quando l'elaborazione sarà disponibile
    }

    public String getOperazione() {
        return operazione;
    }

    public void setOperazione(String operazione) {
        this.operazione = operazione;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}
