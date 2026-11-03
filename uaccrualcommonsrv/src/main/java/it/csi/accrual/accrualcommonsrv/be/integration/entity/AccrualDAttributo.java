/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoDato;
import it.csi.accrual.accrualcommonsrv.be.integration.type.TipoDatoUserType;
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

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "accrual_d_attributo", schema = "accrual")
@SQLRestriction("gest_data_del IS NULL")
public class AccrualDAttributo extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_d_attributo_attributo_id_seq")
    @SequenceGenerator(name = "accrual_d_attributo_attributo_id_seq", sequenceName = "accrual.accrual_d_attributo_attributo_id_seq", allocationSize = 1)
    @Column(name = "attributo_id")
    private Long id;

    @Column(name = "attributo_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "attributo_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Type(TipoDatoUserType.class)
    @Column(name = "attributo_tipo", nullable = false, columnDefinition = "tipo_dato")
    private TipoDato tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id")
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

    public TipoDato getTipo() {
        return tipo;
    }

    public void setTipo(TipoDato tipo) {
        this.tipo = tipo;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}
