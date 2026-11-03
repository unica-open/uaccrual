--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--
-- Delta dello schema accrual per la versione 1.1.0.
-- Contiene le sole modifiche successive alla 1.0.0: si esegue su un database
-- che ha gia' la 1.0.0, dopo il suo delta-1.0.0.sql.

--Data modifica 23/09/2026 - task-1140
/* BEGIN: tabella delle notifiche persistenti */

-- Le notifiche mostrate nel pannello "Notifiche" del client vivevano solo in memoria
-- nel frontend, quindi andavano perse al cambio di sessione dell'utente.
CREATE TABLE IF NOT EXISTS accrual.accrual_t_notifica (
	notifica_id serial4 NOT NULL,
	ente_id int4 NOT NULL,
	notifica_utente varchar(200) NULL,
	notifica_tipo varchar(20) NOT NULL,
	notifica_codice varchar(200) NULL,
	notifica_titolo varchar(500) NOT NULL,
	notifica_messaggio varchar(2500) NOT NULL,
	file_id int4 NULL,
	notifica_resume_token varchar(2000) NULL,
	notifica_data_lettura timestamp NULL,

	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,

	CONSTRAINT pk_accrual_t_notifica PRIMARY KEY (notifica_id),
	CONSTRAINT accrual_t_ente_accrual_t_notifica FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_file_accrual_t_notifica FOREIGN KEY (file_id) REFERENCES accrual.accrual_t_file(file_id)
);
CREATE INDEX IF NOT EXISTS accrual_t_notifica_ente_id_idx ON accrual.accrual_t_notifica USING btree (ente_id);
CREATE INDEX IF NOT EXISTS accrual_t_notifica_file_id_idx ON accrual.accrual_t_notifica USING btree (file_id);
COMMENT ON TABLE accrual.accrual_t_notifica IS 'Notifiche mostrate all''utente nel centro avvisi, persistenti fra le sessioni';
COMMENT ON COLUMN accrual.accrual_t_notifica.notifica_utente IS 'Destinatario della notifica; NULL indica una notifica valida per tutto l''ente';
COMMENT ON COLUMN accrual.accrual_t_notifica.notifica_tipo IS 'INFO, WARN, ERROR oppure CONFIRM';
COMMENT ON COLUMN accrual.accrual_t_notifica.notifica_resume_token IS 'Token di ripresa elaborazione, valorizzato solo per le notifiche di tipo CONFIRM';
COMMENT ON COLUMN accrual.accrual_t_notifica.notifica_data_lettura IS 'Valorizzata quando l''utente apre la notifica; la chiusura logica usa gest_data_del';

/* END: tabella delle notifiche persistenti */
