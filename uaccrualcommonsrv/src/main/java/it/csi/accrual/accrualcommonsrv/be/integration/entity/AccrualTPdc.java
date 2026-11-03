/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import jakarta.persistence.Version;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "accrual_t_pdc", schema = "accrual")
@SQLRestriction("gest_data_del IS NULL")
public class AccrualTPdc extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_pdc_pdc_id_seq")
    @SequenceGenerator(name = "accrual_t_pdc_pdc_id_seq", sequenceName = "accrual.accrual_t_pdc_pdc_id_seq", allocationSize = 1)
    @Column(name = "pdc_id")
    private Long id;

    @Column(name = "pdc_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "pdc_codice_ufficiale", length = 200)
    private String codiceUfficiale;

    @Column(name = "pdc_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Column(name = "pdc_a_partita", nullable = false)
    private boolean partita;

    @Column(name = "pdc_ordine")
    private String ordine;

    @Column(name = "pdc_livello", nullable = false)
    private Integer livello;

    @Column(name = "pdc_collegamento_bil_itas", length = 200)
    private String collegamentoBilItas;

    @Column(name = "pdc_fondo_dotazione", nullable = false)
    private boolean fondoDotazione;

    @Column(name = "pdc_foglia", nullable = false)
    private boolean foglia;

    @Column(name = "pdc_foglia_non_foglia", nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean fogliaNonFoglia = false;

    @Column(name = "pdc_siac", nullable = false, columnDefinition = "boolean DEFAULT false")
    private Boolean pdcSiac = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_id_padre")
    private AccrualTPdc pdcPadre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_famiglia_id", nullable = false)
    private AccrualTPdcFamiglia pdcFamiglia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_classe_id")
    private AccrualDPdcClasse pdcClasse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_tipo_id", nullable = false)
    private AccrualDPdcTipo pdcTipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdc_stato_id")
    private AccrualDPdcStato pdcStato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

    @Column(name = "inizio_validita", nullable = false)
    private LocalDateTime inizioValidita;

    @Column(name = "fine_validita")
    private LocalDateTime fineValidita;

    @Version
    @Column(name = "version")
    private Integer version;

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

    public String getCodiceUfficiale() {
        return codiceUfficiale;
    }

    public void setCodiceUfficiale(String codiceUfficiale) {
        this.codiceUfficiale = codiceUfficiale;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public boolean isPartita() {
        return partita;
    }

    public void setPartita(boolean partita) {
        this.partita = partita;
    }

    public String getOrdine() {
        return ordine;
    }

    public void setOrdine(String ordine) {
        this.ordine = ordine;
    }

    public Integer getLivello() {
        return livello;
    }

    public void setLivello(Integer livello) {
        this.livello = livello;
    }

    public String getCollegamentoBilItas() {
        return collegamentoBilItas;
    }

    public void setCollegamentoBilItas(String collegamentoBilItas) {
        this.collegamentoBilItas = collegamentoBilItas;
    }

    public boolean isFondoDotazione() {
        return fondoDotazione;
    }

    public void setFondoDotazione(boolean fondoDotazione) {
        this.fondoDotazione = fondoDotazione;
    }

    public boolean isFoglia() {
        return foglia;
    }

    public void setFoglia(boolean foglia) {
        this.foglia = foglia;
    }

    public Boolean getFogliaNonFoglia() {
        return fogliaNonFoglia;
    }

    public void setFogliaNonFoglia(Boolean fogliaNonFoglia) {
        this.fogliaNonFoglia = fogliaNonFoglia;
    }

    public AccrualTPdc getPdcPadre() {
        return pdcPadre;
    }

    public void setPdcPadre(AccrualTPdc pdcPadre) {
        this.pdcPadre = pdcPadre;
    }

    public AccrualTPdcFamiglia getPdcFamiglia() {
        return pdcFamiglia;
    }

    public void setPdcFamiglia(AccrualTPdcFamiglia pdcFamiglia) {
        this.pdcFamiglia = pdcFamiglia;
    }

    public AccrualDPdcClasse getPdcClasse() {
        return pdcClasse;
    }

    public void setPdcClasse(AccrualDPdcClasse pdcClasse) {
        this.pdcClasse = pdcClasse;
    }

    public AccrualDPdcTipo getPdcTipo() {
        return pdcTipo;
    }

    public void setPdcTipo(AccrualDPdcTipo pdcTipo) {
        this.pdcTipo = pdcTipo;
    }

    public AccrualDPdcStato getPdcStato() {
        return pdcStato;
    }

    public void setPdcStato(AccrualDPdcStato pdcStato) {
        this.pdcStato = pdcStato;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }

    public Boolean getPdcSiac() {
        return pdcSiac;
    }

    public void setPdcSiac(Boolean pdcSiac) {
        this.pdcSiac = pdcSiac;
    }

    public LocalDateTime getInizioValidita() {
        return inizioValidita;
    }

    public void setInizioValidita(LocalDateTime inizioValidita) {
        this.inizioValidita = inizioValidita;
    }

    public LocalDateTime getFineValidita() {
        return fineValidita;
    }

    public void setFineValidita(LocalDateTime fineValidita) {
        this.fineValidita = fineValidita;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}