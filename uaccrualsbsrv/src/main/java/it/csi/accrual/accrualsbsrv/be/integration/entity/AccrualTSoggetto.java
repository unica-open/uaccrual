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
@Table(name = "accrual_t_soggetto", schema = "accrual")
public class AccrualTSoggetto extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_soggetto_soggetto_id_seq")
    @SequenceGenerator(name = "accrual_t_soggetto_soggetto_id_seq", sequenceName = "accrual.accrual_t_soggetto_soggetto_id_seq", allocationSize = 1)
    @Column(name = "soggetto_id")
    private Long id;

    @Column(name = "soggetto_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "soggetto_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Column(name = "soggetto_codice_fiscale", length = 16)
    private String codiceFiscale;

    @Column(name = "soggetto_codice_fiscale_estero", length = 500)
    private String codiceFiscaleEstero;

    @Column(name = "soggetto_partita_iva", length = 500)
    private String partitaIva;

    @Column(name = "soggetto_email", length = 256)
    private String email;

    @Column(name = "soggetto_is_pa", nullable = false)
    private boolean pa;

    @Column(name = "soggetto_istituto_di_credito", nullable = false)
    private boolean istitutoDiCredito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soggetto_stato_id", nullable = false)
    private AccrualDSoggettoStato stato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soggetto_tipo_id", nullable = false)
    private AccrualDSoggettoTipo tipo;

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

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public String getCodiceFiscaleEstero() {
        return codiceFiscaleEstero;
    }

    public void setCodiceFiscaleEstero(String codiceFiscaleEstero) {
        this.codiceFiscaleEstero = codiceFiscaleEstero;
    }

    public String getPartitaIva() {
        return partitaIva;
    }

    public void setPartitaIva(String partitaIva) {
        this.partitaIva = partitaIva;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPa() {
        return pa;
    }

    public void setPa(boolean pa) { // Corrected setter
        this.pa = pa;
    }

    public boolean isIstitutoDiCredito() {
        return istitutoDiCredito;
    }

    public void setIstitutoDiCredito(boolean istitutoDiCredito) {
        this.istitutoDiCredito = istitutoDiCredito;
    }

    public AccrualDSoggettoStato getStato() {
        return stato;
    }

    public void setStato(AccrualDSoggettoStato stato) {
        this.stato = stato;
    }

    public AccrualDSoggettoTipo getTipo() {
        return tipo;
    }

    public void setTipo(AccrualDSoggettoTipo tipo) {
        this.tipo = tipo;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}