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
@Table(name = "accrual_t_ente", schema = "accrual")
public class AccrualTEnte extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_ente_ente_id_seq")
    @SequenceGenerator(name = "accrual_t_ente_ente_id_seq", sequenceName = "accrual.accrual_t_ente_ente_id_seq", allocationSize = 1)
    @Column(name = "ente_id")
    private Long id;

    @Column(name = "ente_codice_univoco", nullable = false, length = 50)
    private String codiceUnivoco;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodiceUnivoco() {
        return codiceUnivoco;
    }

    public void setCodiceUnivoco(String codiceUnivoco) {
        this.codiceUnivoco = codiceUnivoco;
    }
}
