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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "accrual_t_comune", schema = "accrual")
public class AccrualTComune extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_comune_comune_id_seq")
    @SequenceGenerator(name = "accrual_t_comune_comune_id_seq", sequenceName = "accrual.accrual_t_comune_comune_id_seq", allocationSize = 1)
    @Column(name = "comune_id")
    private Long id;

    @Column(name = "comune_codice_istat", length = 200)
    private String codiceIstat;

    @Column(name = "comune_denominazione", nullable = false, length = 500)
    private String denominazione;

    @Column(name = "comune_codice_belfiore")
    private String codiceBelfiore;

    @Column(name = "comune_cap")
    private String cap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nazione_id", nullable = false)
    private AccrualTNazione nazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia_regione_r_id", nullable = false)
    private AccrualRProvinciaRegione provinciaRegione;

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

    public String getDenominazione() {
        return denominazione;
    }

    public void setDenominazione(String denominazione) {
        this.denominazione = denominazione;
    }

    public String getCodiceBelfiore() {
        return codiceBelfiore;
    }

    public void setCodiceBelfiore(String codiceBelfiore) {
        this.codiceBelfiore = codiceBelfiore;
    }

    public AccrualTNazione getNazione() {
        return nazione;
    }

    public void setNazione(AccrualTNazione nazione) {
        this.nazione = nazione;
    }

    public AccrualRProvinciaRegione getProvinciaRegione() {
        return provinciaRegione;
    }

    public void setProvinciaRegione(AccrualRProvinciaRegione provinciaRegione) {
        this.provinciaRegione = provinciaRegione;
    }
}
