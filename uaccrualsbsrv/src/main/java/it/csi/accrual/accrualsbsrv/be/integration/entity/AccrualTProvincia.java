/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.entity;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "accrual_t_provincia", schema = "accrual")
public class AccrualTProvincia extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_provincia_provincia_id_seq")
    @SequenceGenerator(name = "accrual_t_provincia_provincia_id_seq", sequenceName = "accrual.accrual_t_provincia_provincia_id_seq", allocationSize = 1)
    @Column(name = "provincia_id")
    private Long id;

    @Column(name = "provincia_codice_istat", length = 200)
    private String codiceIstat;

    @Column(name = "provincia_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Column(name = "provincia_sigla_automobilistica", length = 2)
    private String siglaAutomobilistica;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodiceIstat() {
        return codiceIstat;
    }

    public void setCodiceIstat(String codiceIstat) {
        this.codiceIstat = codiceIstat;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getSiglaAutomobilistica() {
        return siglaAutomobilistica;
    }

    public void setSiglaAutomobilistica(String siglaAutomobilistica) {
        this.siglaAutomobilistica = siglaAutomobilistica;
    }
}
