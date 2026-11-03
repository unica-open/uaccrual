/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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
    name = "accrualauth_t_reset_password",
    schema = "accrualauth"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"utente"})
@Comment("Tabella per il reset/conferma della password per gli utenti che usano autenticazione su accrual")
public class AccrualAuthTResetPassword implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reset_password_id", nullable = false)
    @EqualsAndHashCode.Include
    private Integer resetPasswordId;

    @Column(name = "reset_password_token_value")
    private String resetPasswordTokenValue;

    @Column(name = "reset_password_expiresat")
    private LocalDateTime resetPasswordExpiresAt;

    // =========================
    // Relazione
    // =========================

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private AccrualAuthTUtente utente;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.gestDataUpd = LocalDateTime.now();
    }
}
