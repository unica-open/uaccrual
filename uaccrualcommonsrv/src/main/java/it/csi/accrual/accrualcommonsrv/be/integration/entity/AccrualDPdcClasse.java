/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.time.LocalDateTime;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoImporto;
import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoSegno;
import it.csi.accrual.accrualcommonsrv.be.integration.type.TipoImportoUserType;
import it.csi.accrual.accrualcommonsrv.be.integration.type.TipoSegnoUserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "accrual_d_pdc_classe", schema = "accrual")
public class AccrualDPdcClasse extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_d_pdc_classe_pdc_classe_id_seq")
    @SequenceGenerator(name = "accrual_d_pdc_classe_pdc_classe_id_seq", sequenceName = "accrual.accrual_d_pdc_classe_pdc_classe_id_seq", allocationSize = 1)
    @Column(name = "pdc_classe_id")
    private Long id;

    @Column(name = "pdc_classe_codice", nullable = false, length = 200)
    private String codice;

    @Column(name = "pdc_classe_descrizione", nullable = false, length = 500)
    private String descrizione;

    @Type(TipoSegnoUserType.class)
    @Column(name = "pdc_classe_segno", nullable = false, columnDefinition = "tipo_segno")
    private TipoSegno segno;

    @Type(TipoImportoUserType.class)
    @Column(name = "pdc_classe_tipo_importo", nullable = false, columnDefinition = "tipo_importo")
    private TipoImporto tipoImporto;

    @Column(name = "inizio_validita", nullable = false)
    private LocalDateTime inizioValidita;

    @Column(name = "fine_validita")
    private LocalDateTime fineValidita;

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

    public TipoSegno getSegno() {
        return segno;
    }

    public void setSegno(TipoSegno segno) {
        this.segno = segno;
    }


    public TipoImporto getTipoImporto() {
        return tipoImporto;
    }

    public void setTipoImporto(TipoImporto tipoImporto) {
        this.tipoImporto = tipoImporto;
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
}
