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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "accrualauth_d_ruolo",
    schema = "accrualauth",
    uniqueConstraints = {
        @UniqueConstraint(name = "accrualauth_d_ruolo_ruolo_codice_key", columnNames = "ruolo_codice")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class AccrualAuthDRuolo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ruolo_id", nullable = false)
    private Integer ruoloId;

    @Column(name = "ruolo_codice", nullable = false, length = 50)
    private String ruoloCodice;

    @Column(name = "ruolo_descrizione", nullable = false, length = 255)
    private String ruoloDescrizione;

    // Validità
    @Column(name = "inizio_validita", nullable = false)
    private LocalDateTime inizioValidita;

    @Column(name = "fine_validita")
    private LocalDateTime fineValidita;

    // Audit fields
    @Column(name = "gest_data_ins", nullable = false, updatable = false)
    private LocalDateTime gestDataIns;

    @Column(name = "gest_data_upd")
    private LocalDateTime gestDataUpd;

    @Column(name = "gest_data_del")
    private LocalDateTime gestDataDel;

    @Column(name = "gest_utente_ins", nullable = false, updatable = false, length = 200)
    private String gestUtenteIns = "admin";

    @Column(name = "gest_utente_upd", length = 200)
    private String gestUtenteUpd;

    @Column(name = "gest_utente_del", length = 200)
    private String gestUtenteDel;

    /* ===========================
       Costruttori
       =========================== */

  
    @PrePersist
    protected void onCreate() {
        this.gestDataIns = LocalDateTime.now();
        if (this.inizioValidita == null) {
            this.inizioValidita = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.gestDataUpd = LocalDateTime.now();
    }
}
