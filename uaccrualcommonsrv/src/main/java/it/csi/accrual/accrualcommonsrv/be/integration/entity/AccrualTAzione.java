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
@Table(name = "accrual_t_azione", schema = "accrual")
public class AccrualTAzione extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_azione_azione_id_seq")
    @SequenceGenerator(name = "accrual_t_azione_azione_id_seq", sequenceName = "accrual.accrual_t_azione_azione_id_seq", allocationSize = 1)
    @Column(name = "azione_id")
    private Long id;

    @Column(name = "azione_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "azione_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Column(name = "azione_ordine", nullable = false)
    private Integer ordine;

    @Column(name = "azione_urlapplicazione")
    private String path;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gruppo_azione_id")
    private AccrualTGruppoAzione gruppoAzione;

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

    public Integer getOrdine() {
        return ordine;
    }

    public void setOrdine(Integer ordine) {
        this.ordine = ordine;
    }

    public AccrualTGruppoAzione getGruppoAzione() {
        return gruppoAzione;
    }

    public void setGruppoAzione(AccrualTGruppoAzione gruppoAzione) {
        this.gruppoAzione = gruppoAzione;
    }

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


    
}
