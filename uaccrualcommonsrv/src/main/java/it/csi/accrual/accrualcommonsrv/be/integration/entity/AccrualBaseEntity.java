/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.SQLRestriction;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("gest_data_del IS NULL")
public abstract class AccrualBaseEntity {

    @CreatedDate
    @Column(name = "gest_data_ins", nullable = false, updatable = false)
    private LocalDateTime dataInserimento;

    @LastModifiedDate
    @Column(name = "gest_data_upd")
    private LocalDateTime dataAggiornamento;

    @Column(name = "gest_data_del")
    private LocalDateTime dataCancellazione;

    @CreatedBy
    @Column(name = "gest_utente_ins", nullable = false, updatable = false, length = 200)
    private String utenteInserimento;

    @LastModifiedBy
    @Column(name = "gest_utente_upd", length = 200)
    private String utenteAggiornamento;

    @Column(name = "gest_utente_del", length = 200)
    private String utenteCancellazione;

    // Getters and Setters

    public LocalDateTime getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(LocalDateTime dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public LocalDateTime getDataAggiornamento() {
        return dataAggiornamento;
    }

    public void setDataAggiornamento(LocalDateTime dataAggiornamento) {
        this.dataAggiornamento = dataAggiornamento;
    }

    public LocalDateTime getDataCancellazione() {
        return dataCancellazione;
    }

    public void setDataCancellazione(LocalDateTime dataCancellazione) {
        this.dataCancellazione = dataCancellazione;
    }

    public String getUtenteInserimento() {
        return utenteInserimento;
    }

    public void setUtenteInserimento(String utenteInserimento) {
        this.utenteInserimento = utenteInserimento;
    }

    public String getUtenteAggiornamento() {
        return utenteAggiornamento;
    }

    public void setUtenteAggiornamento(String utenteAggiornamento) {
        this.utenteAggiornamento = utenteAggiornamento;
    }

    public String getUtenteCancellazione() {
        return utenteCancellazione;
    }

    public void setUtenteCancellazione(String utenteCancellazione) {
        this.utenteCancellazione = utenteCancellazione;
    }
}