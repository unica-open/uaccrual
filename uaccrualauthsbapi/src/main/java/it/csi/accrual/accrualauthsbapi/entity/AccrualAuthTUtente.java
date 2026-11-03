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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    name = "accrualauth_t_utente",
    schema = "accrualauth",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_accrualauth_t_utente_cf_ente",
            columnNames = {"utente_codice_fiscale", "ente_id"}
        )
    },
    indexes = {
        @Index(name = "idx_utente_cf", columnList = "utente_codice_fiscale"),
        @Index(name = "idx_utente_email", columnList = "utente_email"),
        @Index(name = "idx_utente_ente", columnList = "ente_id"),
        @Index(name = "idx_utente_ruolo", columnList = "ruolo_id"),
        @Index(name = "idx_utente_autenticazione_tipo", columnList = "autenticazione_tipo_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"ente", "ruolo", "autenticazioneTipo"})
@Comment("Tabella degli Utenti")
public class AccrualAuthTUtente implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utente_id", nullable = false)
    @EqualsAndHashCode.Include
    private Integer utenteId;

    @Column(name = "utente_codice_fiscale", nullable = false, length = 16)
    private String utenteCodiceUnivoco;

    @Column(name = "utente_codice", nullable = false, length = 200)
    private String utenteCodice;

    @Column(name = "utente_descrizione", nullable = false, length = 100)
    private String utenteDescrizione;

    @Column(name = "utente_email", length = 255)
    private String utenteEmail;

    @Column(name = "utente_primo_accesso", nullable = false)
    @Builder.Default
    private Boolean utentePrimoAccesso = true;

    // =========================
    // Relazioni
    // =========================

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ente_id", nullable = false)
    private AccrualAuthTEnte ente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ruolo_id", nullable = false)
    private AccrualAuthDRuolo ruolo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "autenticazione_tipo_id", nullable = false)
    private AccrualAuthDAutenticazioneTipo autenticazioneTipo;

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
        if (this.utentePrimoAccesso == null) {
            this.utentePrimoAccesso = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.gestDataUpd = LocalDateTime.now();
    }
}
