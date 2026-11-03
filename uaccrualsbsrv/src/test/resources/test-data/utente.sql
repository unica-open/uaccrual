--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

DELETE FROM ACCRUAL.accrual_t_utente;
DELETE FROM ACCRUAL.accrual_r_ruolo_azione;
DELETE FROM ACCRUAL.accrual_t_azione;
DELETE FROM ACCRUAL.accrual_d_ruolo;
DELETE FROM ACCRUAL.accrual_d_autenticazione_tipo;
DELETE FROM ACCRUAL.accrual_t_soggetto;
DELETE FROM ACCRUAL.accrual_t_ente;

INSERT INTO ACCRUAL.accrual_t_ente (ente_id, ente_codice_fiscale, ente_denominazione, gest_data_ins, gest_utente_ins) VALUES (1, 'EN00000000000', 'Ente Test', CURRENT_TIMESTAMP, 'TEST');

INSERT INTO ACCRUAL.accrual_d_ruolo (ruolo_id, ruolo_codice, ruolo_descrizione, gest_data_ins, gest_utente_ins) VALUES (1, 'RUOLO_TEST', 'Ruolo di Test', CURRENT_TIMESTAMP, 'TEST');

INSERT INTO ACCRUAL.accrual_t_azione (azione_id, azione_codice, azione_descrizione, azione_ordine, gest_data_ins, gest_utente_ins) VALUES (1, 'AZIONE_TEST', 'Azione di Test', 1, CURRENT_TIMESTAMP, 'TEST');

INSERT INTO ACCRUAL.accrual_r_ruolo_azione (ruolo_azione_r_id, ruolo_id, azione_id, ente_id, gest_data_ins, gest_utente_ins) VALUES (1, 1, 1, 1, CURRENT_TIMESTAMP, 'TEST');

INSERT INTO ACCRUAL.accrual_d_autenticazione_tipo (autenticazione_tipo_id, autenticazione_tipo_codice, autenticazione_tipo_descrizione, gest_data_ins, gest_utente_ins) VALUES (1, 'TIPO_AUTH', 'Tipo Autenticazione Test', CURRENT_TIMESTAMP, 'TEST');

INSERT INTO ACCRUAL.accrual_t_utente (utente_id, utente_codice_fiscale, utente_codice, utente_descrizione, ente_id, ruolo_id, autenticazione_tipo_id, gest_data_ins, gest_utente_ins) VALUES (1, 'RSSMRA80A01H501Z', 'RSSMRA80A01H501Z', 'Mario Rossi', 1, 1, 1, CURRENT_TIMESTAMP, 'TEST');
