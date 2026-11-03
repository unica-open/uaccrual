--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

-- Test data per Piano dei Conti (PdC)
-- Per test di integrazione FM-02 - Validazione Univocità

-- Pulizia dati esistenti (solo tabelle PdC, non ente che è condiviso)
DELETE FROM ACCRUAL.accrual_t_pdc;
DELETE FROM ACCRUAL.accrual_d_pdc_classe;
DELETE FROM ACCRUAL.accrual_d_pdc_stato;
DELETE FROM ACCRUAL.accrual_t_pdc_famiglia;
DELETE FROM ACCRUAL.accrual_d_pdc_tipo;

-- Reset sequenze per permettere insert con ID espliciti
ALTER SEQUENCE ACCRUAL.accrual_d_pdc_tipo_pdc_tipo_id_seq RESTART WITH 3;
ALTER SEQUENCE ACCRUAL.accrual_t_pdc_famiglia_pdc_famiglia_id_seq RESTART WITH 102;
ALTER SEQUENCE ACCRUAL.accrual_d_pdc_classe_pdc_classe_id_seq RESTART WITH 21;
ALTER SEQUENCE ACCRUAL.accrual_d_pdc_stato_pdc_stato_id_seq RESTART WITH 3;

-- Insert per AccrualDPdcTipo
INSERT INTO ACCRUAL.accrual_d_pdc_tipo (pdc_tipo_id, pdc_tipo_codice, pdc_tipo_descrizione, gest_data_ins, gest_utente_ins)
VALUES (1, 'PATRIMONIALE', 'Conto Patrimoniale', CURRENT_TIMESTAMP, 'test_user');

INSERT INTO ACCRUAL.accrual_d_pdc_tipo (pdc_tipo_id, pdc_tipo_codice, pdc_tipo_descrizione, gest_data_ins, gest_utente_ins)
VALUES (2, 'ECONOMICO', 'Conto Economico', CURRENT_TIMESTAMP, 'test_user');

-- Insert per AccrualTPdcFamiglia
INSERT INTO ACCRUAL.accrual_t_pdc_famiglia (pdc_famiglia_id, pdc_famiglia_codice, pdc_famiglia_descrizione, pdc_famiglia_modificabile, pdc_famiglia_ricercabile, gest_data_ins, gest_utente_ins)
VALUES (100, 'ACCRUAL', 'Famiglia Accrual', FALSE, TRUE, CURRENT_TIMESTAMP, 'test_user');

INSERT INTO ACCRUAL.accrual_t_pdc_famiglia (pdc_famiglia_id, pdc_famiglia_codice, pdc_famiglia_descrizione, pdc_famiglia_modificabile, pdc_famiglia_ricercabile, gest_data_ins, gest_utente_ins)
VALUES (101, '118', 'Famiglia 118', FALSE, TRUE, CURRENT_TIMESTAMP, 'test_user');

-- Insert per AccrualDPdcClasse (2 classi per test "classe diversa")
-- NOTA: pdc_classe_tipo_importo è NOT NULL - usa i valori dell'enum TipoImporto
INSERT INTO ACCRUAL.accrual_d_pdc_classe (pdc_classe_id, pdc_classe_codice, pdc_classe_descrizione, pdc_classe_segno, pdc_classe_tipo_importo, inizio_validita, gest_data_ins, gest_utente_ins)
VALUES (10, 'CLASSE_A', 'Classe A', 'DARE', 'LORDO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test_user');

INSERT INTO ACCRUAL.accrual_d_pdc_classe (pdc_classe_id, pdc_classe_codice, pdc_classe_descrizione, pdc_classe_segno, pdc_classe_tipo_importo, inizio_validita, gest_data_ins, gest_utente_ins)
VALUES (20, 'CLASSE_B', 'Classe B', 'AVERE', 'IMPONIBILE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test_user');

-- Insert per AccrualDPdcStato
INSERT INTO ACCRUAL.accrual_d_pdc_stato (pdc_stato_id, pdc_stato_codice, pdc_stato_descrizione, gest_data_ins, gest_utente_ins)
VALUES (1, 'ATTIVO', 'Stato Attivo', CURRENT_TIMESTAMP, 'test_user');

INSERT INTO ACCRUAL.accrual_d_pdc_stato (pdc_stato_id, pdc_stato_codice, pdc_stato_descrizione, gest_data_ins, gest_utente_ins)
VALUES (2, 'INATTIVO', 'Stato Inattivo', CURRENT_TIMESTAMP, 'test_user');
