--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

-- Script insert accrualauth_t_ente
INSERT INTO accrualauth_t_ente (ente_id, ente_codice_univoco, ente_codice_fiscale, ente_denominazione) VALUES (8, 'ENTE_TEST', 'ENTE_TEST', 'Ente TEST') ON CONFLICT DO NOTHING;

TRUNCATE TABLE accrualauth.accrualauth_d_ruolo CASCADE;
TRUNCATE TABLE accrualauth.accrualauth_d_autenticazione_tipo CASCADE;
TRUNCATE TABLE accrualauth.accrualauth_t_utente CASCADE;
INSERT INTO accrualauth_d_ruolo (ruolo_id, ruolo_codice, ruolo_descrizione) VALUES (1, 'LETTURA', 'Utente in lettura') ON CONFLICT DO NOTHING;
INSERT INTO accrualauth_d_ruolo (ruolo_id, ruolo_codice, ruolo_descrizione) VALUES (2, 'SCRITTURA_LIMITATA', 'Utente in scrittura solo su alcune funzioni del sistema') ON CONFLICT DO NOTHING;
INSERT INTO accrualauth_d_ruolo (ruolo_id, ruolo_codice, ruolo_descrizione) VALUES (3, 'SCRITTURA_COMPLETA', 'Utente in scrittura su tutte le funzioni del sistema') ON CONFLICT DO NOTHING;
INSERT INTO accrualauth_d_ruolo (ruolo_id, ruolo_codice, ruolo_descrizione) VALUES (4, 'REPORT', 'Utente che accede solo alla reportistica') ON CONFLICT DO NOTHING;

INSERT INTO accrualauth_d_ruolo (ruolo_id, ruolo_codice, ruolo_descrizione) VALUES (5, 'SISTEMI_EXT', 'Utente che accede solo da sistemi esterni') ON CONFLICT DO NOTHING;


INSERT INTO accrualauth_d_autenticazione_tipo (autenticazione_tipo_codice, autenticazione_tipo_descrizione,autenticazione_tipo_plugin, autenticazione_tipo_url) VALUES ('SIAC-SHIB','Autenticazione da SIAC attraverso Shibboleth interno', 'Shibboleth', 'it/siac/accrual/') ON CONFLICT DO NOTHING;

INSERT INTO accrualauth_d_autenticazione_tipo (autenticazione_tipo_codice, autenticazione_tipo_descrizione,autenticazione_tipo_plugin, autenticazione_tipo_url) VALUES ('SIAC-CIFA','Autenticazione da SIAC attraverso Shibboleth CIFA (internet)', 'Shibboleth', 'it/siac-ext/accrual') ON CONFLICT DO NOTHING;

INSERT INTO accrualauth_d_autenticazione_tipo (autenticazione_tipo_codice, autenticazione_tipo_descrizione,autenticazione_tipo_plugin) VALUES ('EXT-SPRING','Autenticazione integrata in accrual', 'SpringSecurity') ON CONFLICT DO NOTHING;

INSERT INTO accrualauth_d_autenticazione_tipo (autenticazione_tipo_codice, autenticazione_tipo_descrizione,autenticazione_tipo_plugin, autenticazione_tipo_url) VALUES ('EXT-TOKEN','Autenticazione con passaggio di token', 'Token', 'it/accrual-ext/milano') ON CONFLICT DO NOTHING;

INSERT INTO accrualauth_d_autenticazione_tipo (autenticazione_tipo_codice, autenticazione_tipo_descrizione,autenticazione_tipo_plugin) VALUES ('CSI-JOB','Batch sistema esterno a SIAC ma di CSI (Factotum)', 'Batch') ON CONFLICT DO NOTHING;

INSERT INTO accrualauth_t_utente (utente_codice_fiscale, utente_codice,
utente_descrizione, utente_email, utente_primo_accesso, ente_id, ruolo_id, 
autenticazione_tipo_id, inizio_validita) 
select 'ENTETS00A00A000I', 'ENTETS00A00A000I', 
'Utente Test', 'prova@gmail.com', true, ente_id, ruolo_id, autenticazione_tipo_id, '2026-04-01 19:59:24.426' 
from accrualauth_t_ente e, accrualauth_d_ruolo adr, accrualauth_d_autenticazione_tipo adat  
where e.ente_codice_univoco = 'ENTE_TEST'
and adr.ruolo_codice = 'SCRITTURA_COMPLETA'
and adat.autenticazione_tipo_codice = 'EXT-SPRING';

INSERT INTO accrualauth.accrualauth_t_password (password_value, utente_id, inizio_validita) 
SELECT '$2a$10$7OCqT2aJSu9iwORQTqyX3eIn6vsWRt.Dx6M9TWXTfzZ.X8Sys2dVy', utente_id, '2026-04-02 13:03:50.419' from accrual_t_utente where utente_codice_fiscale='ENTETS00A00A000I';

UPDATE accrualauth.accrualauth_t_utente SET utente_primo_accesso=false WHERE utente_codice_fiscale='ENTETS00A00A000I';
