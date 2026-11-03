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
@Table(name = "accrual_d_file_tipo", schema = "accrual")
public class AccrualDFileTipo extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_d_file_tipo_file_tipo_id_seq")
    @SequenceGenerator(name = "accrual_d_file_tipo_file_tipo_id_seq", sequenceName = "accrual.accrual_d_file_tipo_file_tipo_id_seq", allocationSize = 1)
    @Column(name = "file_tipo_id")
    private Long id;

    @Column(name = "file_tipo_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "file_tipo_descrizione", nullable = false, length = 500)
    private String descrizione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "azione_id")
    private AccrualTAzione azione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fonte_id", nullable = false)
    private AccrualDFonte fonte;

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

    public AccrualTAzione getAzione() {
        return azione;
    }

    public void setAzione(AccrualTAzione azione) {
        this.azione = azione;
    }

    public AccrualDFonte getFonte() {
        return fonte;
    }

    public void setFonte(AccrualDFonte fonte) {
        this.fonte = fonte;
    }
}
