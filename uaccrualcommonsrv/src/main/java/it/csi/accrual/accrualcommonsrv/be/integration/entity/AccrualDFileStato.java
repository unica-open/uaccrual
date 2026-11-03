/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "accrual_d_file_stato", schema = "accrual")
public class AccrualDFileStato extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_d_file_stato_file_stato_id_seq")
    @SequenceGenerator(name = "accrual_d_file_stato_file_stato_id_seq", sequenceName = "accrual.accrual_d_file_stato_file_stato_id_seq", allocationSize = 1)
    @Column(name = "file_stato_id")
    private Long id;

    @Column(name = "file_stato_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "file_stato_descrizione", nullable = false, length = 500)
    private String descrizione;

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
}
