--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

DELETE FROM ACCRUAL.accrual_t_soggetto;
DELETE FROM ACCRUAL.accrual_t_ente;
DELETE FROM ACCRUAL.accrual_d_soggetto_tipo;
DELETE FROM ACCRUAL.accrual_d_soggetto_stato;

-- Insert data for AccrualDSoggettoStato
INSERT INTO ACCRUAL.accrual_d_soggetto_stato (soggetto_stato_id, soggetto_stato_codice, soggetto_stato_descrizione, gest_data_ins, gest_utente_ins)
VALUES (1, 'ATTIVO', 'Attivo', CURRENT_TIMESTAMP, 'test_user');
INSERT INTO ACCRUAL.accrual_d_soggetto_stato (soggetto_stato_id, soggetto_stato_codice, soggetto_stato_descrizione, gest_data_ins, gest_utente_ins)
VALUES (2, 'INATTIVO', 'Inattivo', CURRENT_TIMESTAMP, 'test_user');

-- Insert data for AccrualDSoggettoTipo
INSERT INTO ACCRUAL.accrual_d_soggetto_tipo (soggetto_tipo_id, soggetto_tipo_codice, soggetto_tipo_descrizione, gest_data_ins, gest_utente_ins)
VALUES (1, 'PERSONA_FISICA', 'Persona Fisica', CURRENT_TIMESTAMP, 'test_user');
INSERT INTO ACCRUAL.accrual_d_soggetto_tipo (soggetto_tipo_id, soggetto_tipo_codice, soggetto_tipo_descrizione, gest_data_ins, gest_utente_ins)
VALUES (2, 'PERSONA_GIURIDICA', 'Persona Giuridica', CURRENT_TIMESTAMP, 'test_user');

-- Insert data for AccrualTEnte
INSERT INTO ACCRUAL.accrual_t_ente (ente_id, ente_codice_fiscale, ente_denominazione, gest_data_ins, gest_utente_ins)
VALUES (1, 'ENTE0000000000', 'Ente di Test', CURRENT_TIMESTAMP, 'test_user');

-- Insert data for AccrualTSoggetto
-- Explicitly setting soggetto_id for test case
INSERT INTO ACCRUAL.accrual_t_soggetto (soggetto_id, soggetto_codice, soggetto_descrizione, soggetto_codice_fiscale, soggetto_is_pa, soggetto_istituto_di_credito, soggetto_stato_id, soggetto_tipo_id, ente_id, gest_data_ins, gest_utente_ins)
VALUES (100, 'SOGG001', 'Franco Verdi', 'MRGFNC85M01H501X', FALSE, FALSE, 1, 1, 1, CURRENT_TIMESTAMP, 'test_user');

INSERT INTO ACCRUAL.accrual_t_soggetto (soggetto_id, soggetto_codice, soggetto_descrizione, soggetto_codice_fiscale, soggetto_is_pa, soggetto_istituto_di_credito, soggetto_stato_id, soggetto_tipo_id, ente_id, gest_data_ins, gest_utente_ins)
VALUES (101, 'SOGG002', 'Mario Rossi', 'RSSMRA80A01H501Z', FALSE, FALSE, 1, 1, 1, CURRENT_TIMESTAMP, 'test_user');