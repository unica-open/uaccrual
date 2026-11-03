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
@Table(name = "accrual_d_autenticazione_tipo", schema = "accrual")
public class AccrualDAutenticazioneTipo extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_d_autenticazione_tipo_autenticazione_tipo_id_seq")
    @SequenceGenerator(name = "accrual_d_autenticazione_tipo_autenticazione_tipo_id_seq", sequenceName = "accrual.accrual_d_autenticazione_tipo_autenticazione_tipo_id_seq", allocationSize = 1)
    @Column(name = "autenticazione_tipo_id")
    private Long id;

    @Column(name = "autenticazione_tipo_codice", nullable = false, length = 50)
    private String codice;

    @Column(name = "autenticazione_tipo_descrizione", nullable = false, length = 255)
    private String descrizione;

    @Column(name = "autenticazione_tipo_plugin", nullable = false, length = 255)
    private String plugin;

    @Column(name = "autenticazione_tipo_url", length = 255)
    private String url;

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

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
