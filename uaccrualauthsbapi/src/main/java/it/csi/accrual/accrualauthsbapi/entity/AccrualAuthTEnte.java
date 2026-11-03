/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "accrualauth_t_ente",
    schema = "accrualauth"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class AccrualAuthTEnte implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ente_id", nullable = false)
    @EqualsAndHashCode.Include
    private Integer enteId;

    @Column(name = "ente_codice_univoco", nullable = false, length = 50)
    private String enteCodiceUnivoco;

    @Column(name = "ente_codice_fiscale", nullable = false, length = 16)
    private String enteCodiceFiscale;

    @Column(name = "ente_denominazione", nullable = false, length = 500)
    private String enteDenominazione;

    // =========================
    // Validità
    // =========================

    @Column(name = "inizio_validita", nullable = false)
    private LocalDateTime inizioValidita;

    @Column(name = "fine_validita")
    private LocalDateTime fineValidita;

    // =========================
    // Audit fields
    // =========================

    @Column(name = "gest_data_ins", nullable = false, updatable = false)
    private LocalDateTime gestDataIns;

    @Column(name = "gest_data_upd")
    private LocalDateTime gestDataUpd;

    @Column(name = "gest_data_del")
    private LocalDateTime gestDataDel;

    @Column(name = "gest_utente_ins", nullable = false, updatable = false, length = 200)
    @Builder.Default
    private String gestUtenteIns = "admin";

    @Column(name = "gest_utente_upd", length = 200)
    private String gestUtenteUpd;

    @Column(name = "gest_utente_del", length = 200)
    private String gestUtenteDel;

    // =========================
    // Lifecycle hooks
    // =========================

    @PrePersist
    protected void onCreate() {
        if (this.gestDataIns == null) {
            this.gestDataIns = LocalDateTime.now();
        }
        if (this.inizioValidita == null) {
            this.inizioValidita = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.gestDataUpd = LocalDateTime.now();
    }
}
