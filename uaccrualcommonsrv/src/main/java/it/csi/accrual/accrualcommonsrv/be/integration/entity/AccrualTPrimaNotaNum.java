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
@Table(name = "accrual_t_prima_nota_num", schema = "accrual")
public class AccrualTPrimaNotaNum extends AccrualBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accrual_t_prima_nota_num_prima_nota_num_id_seq")
    @SequenceGenerator(name = "accrual_t_prima_nota_num_prima_nota_num_id_seq", sequenceName = "accrual.accrual_t_prima_nota_num_prima_nota_num_id_seq", allocationSize = 1)
    @Column(name = "prima_nota_num_id")
    private Long id;

    @Column(name = "prima_nota_num_anno", nullable = false, length = 4)
    private String anno;

    @Column(name = "prima_nota_num_numero", nullable = false)
    private Integer numero;

    @Column(name = "prima_nota_num_is_definitiva", nullable = false)
    private Boolean isDefinitiva;

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

    public String getAnno() {
        return anno;
    }

    public void setAnno(String anno) {
        this.anno = anno;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Boolean getIsDefinitiva() {
        return isDefinitiva;
    }

    public void setIsDefinitiva(Boolean isDefinitiva) {
        this.isDefinitiva = isDefinitiva;
    }

    public AccrualTEnte getEnte() {
        return ente;
    }

    public void setEnte(AccrualTEnte ente) {
        this.ente = ente;
    }
}
