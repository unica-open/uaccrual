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
@Table(name = "accrual_t_evento", schema = "accrual")
public class AccrualTEvento extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_evento_evento_id_seq")
    @SequenceGenerator(name = "accrual_t_evento_evento_id_seq", sequenceName = "accrual.accrual_t_evento_evento_id_seq", allocationSize = 1)
    @Column(name = "evento_id")
    private Long id;

    @Column(name = "evento_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "evento_descrizione", nullable = false, length = 500)
    private String descrizione;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prima_nota_stato_id", nullable = false)
    private AccrualDPrimaNotaStato primaNotaStato;


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

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }

    public AccrualDPrimaNotaStato getPrimaNotaStato() {
        return primaNotaStato;
    }

    public void setPrimaNotaStato(AccrualDPrimaNotaStato primaNotaStato) {
        this.primaNotaStato = primaNotaStato;
    }
}
