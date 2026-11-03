/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "accrual_t_parametro", schema = "accrual")
public class AccrualTParametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parametro_id", nullable = false)
    private Long id;

    @Column(name = "parametro_nome", nullable = false)
    private String parametroNome;

    @Column(name = "parametro_valore")
    private String parametroValore;

    @Column(name = "parametro_abilitato")
    private Boolean parametroAbilitato;

    @Column(name = "parametro_note")
    private String parametroNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

    // Audit fields
    @Column(name = "gest_data_ins", nullable = false)
    private LocalDateTime gestDataIns;

    @Column(name = "gest_data_upd")
    private LocalDateTime gestDataUpd;

    @Column(name = "gest_data_del")
    private LocalDateTime gestDataDel;

    @Column(name = "gest_utente_ins", nullable = false)
    private String gestUtenteIns;

    @Column(name = "gest_utente_upd")
    private String gestUtenteUpd;

    @Column(name = "gest_utente_del")
    private String gestUtenteDel;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getParametroNome() {
        return parametroNome;
    }

    public void setParametroNome(String parametroNome) {
        this.parametroNome = parametroNome;
    }

    public String getParametroValore() {
        return parametroValore;
    }

    public void setParametroValore(String parametroValore) {
        this.parametroValore = parametroValore;
    }

    public Boolean getParametroAbilitato() {
        return parametroAbilitato;
    }

    public void setParametroAbilitato(Boolean parametroAbilitato) {
        this.parametroAbilitato = parametroAbilitato;
    }

    public String getParametroNote() {
        return parametroNote;
    }

    public void setParametroNote(String parametroNote) {
        this.parametroNote = parametroNote;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }

    public LocalDateTime getGestDataIns() {
        return gestDataIns;
    }

    public void setGestDataIns(LocalDateTime gestDataIns) {
        this.gestDataIns = gestDataIns;
    }

    public LocalDateTime getGestDataUpd() {
        return gestDataUpd;
    }

    public void setGestDataUpd(LocalDateTime gestDataUpd) {
        this.gestDataUpd = gestDataUpd;
    }

    public LocalDateTime getGestDataDel() {
        return gestDataDel;
    }

    public void setGestDataDel(LocalDateTime gestDataDel) {
        this.gestDataDel = gestDataDel;
    }

    public String getGestUtenteIns() {
        return gestUtenteIns;
    }

    public void setGestUtenteIns(String gestUtenteIns) {
        this.gestUtenteIns = gestUtenteIns;
    }

    public String getGestUtenteUpd() {
        return gestUtenteUpd;
    }

    public void setGestUtenteUpd(String gestUtenteUpd) {
        this.gestUtenteUpd = gestUtenteUpd;
    }

    public String getGestUtenteDel() {
        return gestUtenteDel;
    }

    public void setGestUtenteDel(String gestUtenteDel) {
        this.gestUtenteDel = gestUtenteDel;
    }
}
