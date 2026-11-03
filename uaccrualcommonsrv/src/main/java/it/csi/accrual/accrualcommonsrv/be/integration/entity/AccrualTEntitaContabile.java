/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualcommonsrv.be.integration.entity;

import java.time.LocalDateTime;

import it.csi.accrual.accrualcommonsrv.be.integration.entity.enumeration.TipoFase;
import it.csi.accrual.accrualcommonsrv.be.integration.type.TipoFaseUserType;
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
@Table(name = "accrual_t_entita_contabile", schema = "accrual")
public class AccrualTEntitaContabile extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_entita_contabile_entita_contabile_id_seq")
    @SequenceGenerator(name = "accrual_t_entita_contabile_entita_contabile_id_seq", sequenceName = "accrual.accrual_t_entita_contabile_entita_contabile_id_seq", allocationSize = 1)
    @Column(name = "entita_contabile_id")
    private Long id;

    @Column(name = "entita_contabile_anno", nullable = false, length = 4)
    private String anno;

    @Type(TipoFaseUserType.class)
    @Column(name = "entita_contabile_fase", nullable = false, columnDefinition = "tipo_fase")
    private TipoFase fase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualTEnte ente;

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

    public String getAnno() {
        return anno;
    }

    public void setAnno(String anno) {
        this.anno = anno;
    }

    public TipoFase getFase() {
        return fase;
    }

    public void setFase(TipoFase fase) {
        this.fase = fase;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
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
