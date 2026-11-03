/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualsbsrv.be.integration.entity;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.*;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.*;
import it.csi.accrual.accrualcommonsrv.be.integration.repository.*;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoStrada;
import it.csi.accrual.accrualcommonsrv.be.integration.type.TipoStradaUserType;
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

import org.hibernate.annotations.Type;

@Entity
@Table(name = "accrual_t_soggetto_indirizzo", schema = "accrual")
public class AccrualTSoggettoIndirizzo extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_soggetto_indirizzo_soggetto_indirizzo_id_seq")
    @SequenceGenerator(name = "accrual_t_soggetto_indirizzo_soggetto_indirizzo_id_seq", sequenceName = "accrual.accrual_t_soggetto_indirizzo_soggetto_indirizzo_id_seq", allocationSize = 1)
    @Column(name = "soggetto_indirizzo_id")
    private Long id;

    @Column(name = "soggetto_indirizzo_toponimo", length = 500)
    private String toponimo;

    @Column(name = "soggetto_indirizzo_numero_civico", length = 7)
    private String numeroCivico;

    @Column(name = "soggetto_indirizzo_frazione", length = 500)
    private String frazione;

    @Column(name = "soggetto_indirizzo_interno", length = 10)
    private String interno;

    @Column(name = "soggetto_indirizzo_codice_zip", length = 10)
    private String codiceZip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soggetto_id", nullable = false)
    private AccrualTSoggetto soggetto;

    @Type(TipoStradaUserType.class)
    @Column(name = "via_tipo", columnDefinition = "tipo_strada")
    private TipoStrada viaTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comune_id")
    private AccrualTComune comune;

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

    public String getToponimo() {
        return toponimo;
    }

    public void setToponimo(String toponimo) {
        this.toponimo = toponimo;
    }

    public String getNumeroCivico() {
        return numeroCivico;
    }

    public void setNumeroCivico(String numeroCivico) {
        this.numeroCivico = numeroCivico;
    }

    public String getFrazione() {
        return frazione;
    }

    public void setFrazione(String frazione) {
        this.frazione = frazione;
    }

    public String getInterno() {
        return interno;
    }

    public void setInterno(String interno) {
        this.interno = interno;
    }

    public String getCodiceZip() {
        return codiceZip;
    }

    public void setCodiceZip(String codiceZip) {
        this.codiceZip = codiceZip;
    }

    public AccrualTSoggetto getSoggetto() {
        return soggetto;
    }

    public void setSoggetto(AccrualTSoggetto soggetto) {
        this.soggetto = soggetto;
    }

    public TipoStrada getViaTipo() {
        return viaTipo;
    }

    public void setViaTipo(TipoStrada viaTipo) {
        this.viaTipo = viaTipo;
    }

    public AccrualTComune getComune() {
        return comune;
    }

    public void setComune(AccrualTComune comune) {
        this.comune = comune;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}