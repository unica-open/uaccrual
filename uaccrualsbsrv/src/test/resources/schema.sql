--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

-- Schema H2 per test di integrazione
-- Nomi tabelle e colonne in minuscolo tra virgolette per compatibilità con Hibernate

CREATE SCHEMA IF NOT EXISTS "accrual";

-- ============================================================================
-- Tabelle esistenti (Soggetti, Enti, Utenti, etc.)
-- ============================================================================

CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_soggetto_stato_soggetto_stato_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_soggetto_stato" (
    "soggetto_stato_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_soggetto_stato_soggetto_stato_id_seq" PRIMARY KEY,
    "soggetto_stato_codice" VARCHAR(200) NOT NULL,
    "soggetto_stato_descrizione" VARCHAR(500) NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_soggetto_tipo_soggetto_tipo_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_soggetto_tipo" (
    "soggetto_tipo_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_soggetto_tipo_soggetto_tipo_id_seq" PRIMARY KEY,
    "soggetto_tipo_codice" VARCHAR(200) NOT NULL,
    "soggetto_tipo_descrizione" VARCHAR(500) NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_t_ente_ente_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_t_ente" (
    "ente_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_t_ente_ente_id_seq" PRIMARY KEY,
    "ente_codice_fiscale" VARCHAR(16) NOT NULL,
    "ente_denominazione" VARCHAR(500) NOT NULL,
    "ente_codice_univoco" VARCHAR(200),
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_t_soggetto_soggetto_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_t_soggetto" (
    "soggetto_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_t_soggetto_soggetto_id_seq" PRIMARY KEY,
    "soggetto_codice" VARCHAR(200) NOT NULL,
    "soggetto_descrizione" VARCHAR(500) NOT NULL,
    "soggetto_codice_fiscale" VARCHAR(16),
    "soggetto_codice_fiscale_estero" VARCHAR(500),
    "soggetto_partita_iva" VARCHAR(500),
    "soggetto_email" VARCHAR(256),
    "soggetto_is_pa" BOOLEAN NOT NULL,
    "soggetto_istituto_di_credito" BOOLEAN NOT NULL,
    "soggetto_stato_id" BIGINT NOT NULL,
    "soggetto_tipo_id" BIGINT NOT NULL,
    "ente_id" BIGINT NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200),
    FOREIGN KEY ("soggetto_stato_id") REFERENCES "accrual"."accrual_d_soggetto_stato"("soggetto_stato_id"),
    FOREIGN KEY ("soggetto_tipo_id") REFERENCES "accrual"."accrual_d_soggetto_tipo"("soggetto_tipo_id"),
    FOREIGN KEY ("ente_id") REFERENCES "accrual"."accrual_t_ente"("ente_id")
);

-- AccrualDAutenticazioneTipo
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_autenticazione_tipo_autenticazione_tipo_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_autenticazione_tipo" (
    "autenticazione_tipo_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_autenticazione_tipo_autenticazione_tipo_id_seq" PRIMARY KEY,
    "autenticazione_tipo_codice" VARCHAR(200) NOT NULL,
    "autenticazione_tipo_descrizione" VARCHAR(500) NOT NULL,
    "autenticazione_tipo_plugin" VARCHAR(255) NOT NULL,
    "autenticazione_tipo_url" VARCHAR(255),
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualDRuolo
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_ruolo_ruolo_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_ruolo" (
    "ruolo_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_ruolo_ruolo_id_seq" PRIMARY KEY,
    "ruolo_codice" VARCHAR(50) NOT NULL UNIQUE,
    "ruolo_descrizione" VARCHAR(255) NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualTAzione
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_t_azione_azione_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_t_azione" (
    "azione_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_t_azione_azione_id_seq" PRIMARY KEY,
    "azione_codice" VARCHAR(200) NOT NULL,
    "azione_descrizione" VARCHAR(500) NOT NULL,
    "azione_ordine" INTEGER NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualRRuoloAzione
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_r_ruolo_azione_ruolo_azione_r_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_r_ruolo_azione" (
    "ruolo_azione_r_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_r_ruolo_azione_ruolo_azione_r_id_seq" PRIMARY KEY,
    "ruolo_id" BIGINT NOT NULL,
    "azione_id" BIGINT NOT NULL,
    "ente_id" BIGINT NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200),
    FOREIGN KEY ("ruolo_id") REFERENCES "accrual"."accrual_d_ruolo"("ruolo_id"),
    FOREIGN KEY ("azione_id") REFERENCES "accrual"."accrual_t_azione"("azione_id"),
    FOREIGN KEY ("ente_id") REFERENCES "accrual"."accrual_t_ente"("ente_id")
);

-- AccrualTUtente
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_t_utente_utente_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_t_utente" (
    "utente_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_t_utente_utente_id_seq" PRIMARY KEY,
    "utente_codice_fiscale" VARCHAR(16) NOT NULL UNIQUE,
    "utente_codice" VARCHAR(100) NOT NULL,
    "utente_descrizione" VARCHAR(100) NOT NULL,
    "utente_email" VARCHAR(255),
    "ente_id" BIGINT NOT NULL,
    "ruolo_id" BIGINT NOT NULL,
    "autenticazione_tipo_id" BIGINT NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200),
    FOREIGN KEY ("ente_id") REFERENCES "accrual"."accrual_t_ente"("ente_id"),
    FOREIGN KEY ("ruolo_id") REFERENCES "accrual"."accrual_d_ruolo"("ruolo_id"),
    FOREIGN KEY ("autenticazione_tipo_id") REFERENCES "accrual"."accrual_d_autenticazione_tipo"("autenticazione_tipo_id")
);

-- ============================================================================
-- Tabelle per Piano dei Conti (PdC) - Per test di integrazione FM-02
-- ============================================================================

-- AccrualDPdcTipo
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_pdc_tipo_pdc_tipo_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_pdc_tipo" (
    "pdc_tipo_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_pdc_tipo_pdc_tipo_id_seq" PRIMARY KEY,
    "pdc_tipo_codice" VARCHAR(200) NOT NULL,
    "pdc_tipo_descrizione" VARCHAR(500) NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualTPdcFamiglia
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_t_pdc_famiglia_pdc_famiglia_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_t_pdc_famiglia" (
    "pdc_famiglia_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_t_pdc_famiglia_pdc_famiglia_id_seq" PRIMARY KEY,
    "pdc_famiglia_codice" VARCHAR(200) NOT NULL,
    "pdc_famiglia_descrizione" VARCHAR(500) NOT NULL,
    "pdc_famiglia_modificabile" BOOLEAN DEFAULT FALSE NOT NULL,
    "pdc_famiglia_ricercabile" BOOLEAN DEFAULT FALSE NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualDPdcClasse
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_pdc_classe_pdc_classe_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_pdc_classe" (
    "pdc_classe_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_pdc_classe_pdc_classe_id_seq" PRIMARY KEY,
    "pdc_classe_codice" VARCHAR(200) NOT NULL,
    "pdc_classe_descrizione" VARCHAR(500) NOT NULL,
    "pdc_classe_segno" VARCHAR(50),
    "pdc_classe_tipo_importo" VARCHAR(50) NOT NULL,
    "inizio_validita" TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    "fine_validita" TIMESTAMP,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualDPdcStato
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_pdc_stato_pdc_stato_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_pdc_stato" (
    "pdc_stato_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_pdc_stato_pdc_stato_id_seq" PRIMARY KEY,
    "pdc_stato_codice" VARCHAR(200) NOT NULL,
    "pdc_stato_descrizione" VARCHAR(500) NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualTPdc
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_t_pdc_pdc_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_t_pdc" (
    "pdc_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_t_pdc_pdc_id_seq" PRIMARY KEY,
    "pdc_codice" VARCHAR(200) NOT NULL,
    "pdc_codice_ufficiale" VARCHAR(200),
    "pdc_descrizione" VARCHAR(500) NOT NULL,
    "pdc_id_padre" BIGINT,
    "pdc_a_partita" BOOLEAN DEFAULT FALSE NOT NULL,
    "pdc_ordine" VARCHAR,
    "pdc_livello" INTEGER NOT NULL,
    "pdc_collegamento_bil_itas" VARCHAR(200),
    "pdc_fondo_dotazione" BOOLEAN DEFAULT FALSE NOT NULL,
    "pdc_foglia" BOOLEAN DEFAULT FALSE NOT NULL,
    "pdc_foglia_non_foglia" BOOLEAN DEFAULT FALSE NOT NULL,
    "pdc_siac" BOOLEAN DEFAULT FALSE NOT NULL,
    "pdc_famiglia_id" BIGINT NOT NULL,
    "pdc_classe_id" BIGINT,
    "pdc_tipo_id" BIGINT NOT NULL,
    "pdc_stato_id" BIGINT,
    "ente_id" BIGINT NOT NULL,
    "version" BIGINT DEFAULT 0 NOT NULL,
    "inizio_validita" TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    "fine_validita" TIMESTAMP,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200),
    FOREIGN KEY ("pdc_stato_id") REFERENCES "accrual"."accrual_d_pdc_stato"("pdc_stato_id"),
    FOREIGN KEY ("pdc_tipo_id") REFERENCES "accrual"."accrual_d_pdc_tipo"("pdc_tipo_id"),
    FOREIGN KEY ("ente_id") REFERENCES "accrual"."accrual_t_ente"("ente_id"),
    FOREIGN KEY ("pdc_id_padre") REFERENCES "accrual"."accrual_t_pdc"("pdc_id"),
    FOREIGN KEY ("pdc_famiglia_id") REFERENCES "accrual"."accrual_t_pdc_famiglia"("pdc_famiglia_id"),
    FOREIGN KEY ("pdc_classe_id") REFERENCES "accrual"."accrual_d_pdc_classe"("pdc_classe_id")
);

-- AccrualDPdcTipoRelazione
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_d_pdc_tipo_relazione_pdc_tipo_relazione_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_d_pdc_tipo_relazione" (
    "pdc_tipo_relazione_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_d_pdc_tipo_relazione_pdc_tipo_relazione_id_seq" PRIMARY KEY,
    "pdc_tipo_relazione_codice" VARCHAR(200) NOT NULL,
    "pdc_tipo_relazione_descrizione" VARCHAR(500) NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200)
);

-- AccrualRPdcLegame
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_r_pdc_legame_pdc_legame_r_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_r_pdc_legame" (
    "pdc_legame_r_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_r_pdc_legame_pdc_legame_r_id_seq" PRIMARY KEY,
    "pdc_legame_a_id" BIGINT,
    "pdc_legame_b_id" BIGINT,
    "pdc_tipo_relazione_id" BIGINT,
    "ente_id" BIGINT NOT NULL,
    "inizio_validita" TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL,
    "fine_validita" TIMESTAMP,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200),
    FOREIGN KEY ("pdc_tipo_relazione_id") REFERENCES "accrual"."accrual_d_pdc_tipo_relazione"("pdc_tipo_relazione_id"),
    FOREIGN KEY ("ente_id") REFERENCES "accrual"."accrual_t_ente"("ente_id"),
    FOREIGN KEY ("pdc_legame_a_id") REFERENCES "accrual"."accrual_t_pdc"("pdc_id"),
    FOREIGN KEY ("pdc_legame_b_id") REFERENCES "accrual"."accrual_t_pdc"("pdc_id")
);

-- AccrualTPdcSaldo
CREATE SEQUENCE IF NOT EXISTS "accrual"."accrual_t_pdc_saldo_pdc_saldo_id_seq" START WITH 1 INCREMENT BY 1;
CREATE TABLE IF NOT EXISTS "accrual"."accrual_t_pdc_saldo" (
    "pdc_saldo_id" BIGINT DEFAULT NEXT VALUE FOR "accrual"."accrual_t_pdc_saldo_pdc_saldo_id_seq" PRIMARY KEY,
    "pdc_saldo_anno" INTEGER NOT NULL,
    "pdc_saldo_valore" DECIMAL(19,2) NOT NULL,
    "pdc_siac" BOOLEAN DEFAULT FALSE NOT NULL,
    "pdc_id" BIGINT NOT NULL,
    "ente_id" BIGINT NOT NULL,
    "gest_data_ins" TIMESTAMP NOT NULL,
    "gest_data_upd" TIMESTAMP,
    "gest_data_del" TIMESTAMP,
    "gest_utente_ins" VARCHAR(200) NOT NULL,
    "gest_utente_upd" VARCHAR(200),
    "gest_utente_del" VARCHAR(200),
    FOREIGN KEY ("ente_id") REFERENCES "accrual"."accrual_t_ente"("ente_id"),
    FOREIGN KEY ("pdc_id") REFERENCES "accrual"."accrual_t_pdc"("pdc_id")
);
