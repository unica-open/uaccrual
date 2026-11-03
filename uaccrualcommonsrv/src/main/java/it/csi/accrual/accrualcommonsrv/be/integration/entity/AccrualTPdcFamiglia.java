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
@Table(name = "accrual_t_pdc_famiglia", schema = "accrual")
public class AccrualTPdcFamiglia extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_pdc_famiglia_pdc_famiglia_id_seq")
    @SequenceGenerator(name = "accrual_t_pdc_famiglia_pdc_famiglia_id_seq", sequenceName = "accrual.accrual_t_pdc_famiglia_pdc_famiglia_id_seq", allocationSize = 1)
    @Column(name = "pdc_famiglia_id")
    private Long id;

    @Column(name = "pdc_famiglia_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "pdc_famiglia_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Column(name = "pdc_famiglia_modificabile", nullable = false)
    private Boolean modificabile;

    @Column(name = "pdc_famiglia_ricercabile", nullable = false)
    private Boolean ricercabile;

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

    public Boolean getModificabile() {
        return modificabile;
    }

    public void setModificabile(Boolean modificabile) {
        this.modificabile = modificabile;
    }

    public Boolean getRicercabile() {
        return ricercabile;
    }

    public void setRicercabile(Boolean ricercabile) {
        this.ricercabile = ricercabile;
    }
}