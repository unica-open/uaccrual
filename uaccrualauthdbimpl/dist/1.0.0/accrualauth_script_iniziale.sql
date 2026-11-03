--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

DROP TABLE IF EXISTS accrualauth.accrualauth_d_ruolo CASCADE;
DROP TABLE IF EXISTS accrualauth.accrualauth_t_ente CASCADE;
DROP TABLE IF EXISTS accrualauth.accrualauth_d_autenticazione_tipo CASCADE;
DROP TABLE IF EXISTS accrualauth.accrualauth_t_utente CASCADE;
DROP TABLE IF EXISTS accrualauth.accrualauth_t_token CASCADE;
DROP TABLE IF EXISTS accrualauth.accrualauth_t_password CASCADE;
DROP TABLE IF EXISTS accrualauth.accrualauth_t_reset_password CASCADE;


CREATE TABLE accrualauth.accrualauth_d_ruolo (
    ruolo_id serial4 NOT NULL,  -- condiviso con accrual
    ruolo_codice VARCHAR(50) NOT NULL UNIQUE,
    ruolo_descrizione VARCHAR(255) NOT NULL,

	-- Validate fiels
	inizio_validita timestamp DEFAULT now() NOT NULL, -- non siamo sicuri serva ma potrebbe
	fine_validita timestamp NULL,
	
    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrualauth_d_ruolo_pkey PRIMARY KEY (ruolo_id)	
);
COMMENT ON TABLE accrualauth.accrualauth_d_ruolo IS 'Tabella dei Ruoli (decodifica): il ruolo_codice viene passato ad accrual per recuperare le azioni';


CREATE TABLE accrualauth.accrualauth_t_ente (
	ente_id serial4 NOT NULL,
	ente_codice_univoco varchar(50) NOT NULL, -- nasce per le associazioni/ direzioni che gestiscono contabilità autonoma - viene passato a siac nel claim del JWT
	ente_codice_fiscale varchar(16) NOT NULL,
	ente_denominazione varchar(500) NOT NULL,
	
	-- Validate fiels
	inizio_validita timestamp DEFAULT now() NOT NULL, 
	fine_validita timestamp NULL,

	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrualauth_t_ente PRIMARY KEY (ente_id)
);
CREATE INDEX idx_accrualauth_t_ente_1 ON accrualauth.accrualauth_t_ente USING btree (ente_codice_univoco) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrualauth.accrualauth_t_ente IS 'Tabella degli Enti gestiti da accrual';
 

-- vediamo se creare un type per i valori di plugin possibili (Shibboleth, SpringSecurity, LDAP, ...)
CREATE TABLE accrualauth.accrualauth_d_autenticazione_tipo (
    autenticazione_tipo_id serial4 NOT NULL,
    autenticazione_tipo_codice VARCHAR(50) NOT NULL,
    autenticazione_tipo_descrizione VARCHAR(255) NOT NULL,
	autenticazione_tipo_plugin VARCHAR(255) NOT NULL, --Shib / interna
	autenticazione_tipo_url VARCHAR(255) NULL, --pezzo del contesto (url completo o solo accrual-ext/milano, accrual-ext/roma, accrual/accrual) per online, vuoto per i batch
	
	-- Validate fiels
	inizio_validita timestamp DEFAULT now() NOT NULL, 
	fine_validita timestamp NULL,

    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrualauth_d_autenticazione_tipo PRIMARY KEY (autenticazione_tipo_id)
	
);
COMMENT ON TABLE accrualauth.accrualauth_d_autenticazione_tipo IS 'Tabella per il tipo di autenticazione (serve per capire quale plugin deve utilizzare la componente centrale di accrual che,sulla base del CF determina che tipo di plugin invocare - Es. SIAC - chiamata a servizio Contabilia, LDAP, Header...)';


CREATE TABLE accrualauth.accrualauth_t_utente (
    utente_id serial4 NOT NULL,
    utente_codice_fiscale CHAR(16) NOT NULL UNIQUE,
    utente_codice VARCHAR(200) NOT NULL, -- passerei questo a siac per la memorizzazione nelle tabelle di log 
										 -- (concatenato TIPOAUTH-ENTE-RUOLO-CODICEFISCALE es. SIAC_SHIB-REGP-SCRITTURA_LIMITATA-CMGRNN55R47L219L) 
										 -- questo codice viene memorizzato nelle tabelle di audit
    utente_descrizione VARCHAR(100) NOT NULL,
    utente_email VARCHAR(255),
	utente_primo_accesso BOOLEAN NOT NULL DEFAULT TRUE,
    ente_id INT NOT NULL REFERENCES accrualauth.accrualauth_t_ente(ente_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    ruolo_id INT NOT NULL REFERENCES accrualauth.accrualauth_d_ruolo(ruolo_id) ON UPDATE CASCADE ON DELETE RESTRICT,
	autenticazione_tipo_id INT NOT NULL REFERENCES accrualauth.accrualauth_d_autenticazione_tipo(autenticazione_tipo_id), 
	
	-- Validate fiels
	inizio_validita timestamp DEFAULT now() NOT NULL, 
	fine_validita timestamp NULL,

    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrualauth_t_utente PRIMARY KEY (utente_id)
);
CREATE INDEX idx_utente_cf ON accrualauth.accrualauth_t_utente(utente_codice_fiscale);
CREATE INDEX idx_utente_email ON accrualauth.accrualauth_t_utente(utente_email);
CREATE INDEX idx_utente_ente ON accrualauth.accrualauth_t_utente(ente_id);
CREATE INDEX idx_utente_ruolo ON accrualauth.accrualauth_t_utente(ruolo_id);
CREATE INDEX idx_utente_autenticazione_tipo ON accrualauth.accrualauth_t_utente(autenticazione_tipo_id);
COMMENT ON TABLE accrualauth.accrualauth_t_utente IS 'Tabella degli Utenti';

ALTER TABLE accrualauth.accrualauth_t_utente
ADD CONSTRAINT uq_accrualauth_t_utente_cf_ente UNIQUE (utente_codice_fiscale, ente_id);
 

-- Tabella Token
CREATE TABLE IF NOT EXISTS accrualauth.accrualauth_t_token (
    token_id serial4 NOT NULL,
    token_value TEXT,
    token_start_time TIMESTAMP,
    token_nonce TEXT,
    utente_id INT NOT NULL REFERENCES accrualauth.accrualauth_t_utente(utente_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    
	-- Validate fiels
	inizio_validita timestamp DEFAULT now() NOT NULL, 
	fine_validita timestamp NULL,
	
    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrualauth_t_token_pkey PRIMARY KEY (token_id)	
);
COMMENT ON TABLE accrualauth.accrualauth_t_token IS 'Tabella dei token JWT per intescambio fe/be';

 
-- Tabella Password per i soli utenti che accedono direttamente ad accrual
CREATE TABLE IF NOT EXISTS accrualauth.accrualauth_t_password (
    password_id serial4 NOT NULL,
    password_value VARCHAR(255) NOT NULL,
    utente_id INT NOT NULL REFERENCES accrualauth.accrualauth_t_utente(utente_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    
	-- Validate fiels
	inizio_validita timestamp DEFAULT now() NOT NULL, 
	fine_validita timestamp NULL,
	
    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrualauth_t_password_pkey PRIMARY KEY (password_id)	
);
CREATE UNIQUE INDEX uq_password_utente ON accrualauth.accrualauth_t_password (utente_id) WHERE fine_validita IS NULL;
COMMENT ON TABLE accrualauth.accrualauth_t_password IS 'Tabella delle password (criptate con BCrypt) per gli utenti che accedono direttamente ad accrual';


-- Tabella per il reset/conferma della password per gli utenti che usano l'autenticazione su accrual - viene inviato token
CREATE TABLE IF NOT EXISTS accrualauth.accrualauth_t_reset_password (
    reset_password_id serial4 NOT NULL,
    reset_password_token_value TEXT,
    utente_id INT NOT NULL REFERENCES accrualauth.accrualauth_t_utente(utente_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    
	reset_password_expiresAt TIMESTAMP,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrualauth_t_reset_password_pkey PRIMARY KEY (reset_password_id)	
);
COMMENT ON TABLE accrualauth.accrualauth_t_reset_password IS 'Tabella per il reset/conferma della password per gli utenti che usano autenticazione su accrual';


CREATE OR REPLACE FUNCTION accrualauth.fn_check_password_autenticazione()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_plugin VARCHAR(255);
BEGIN
    SELECT at.autenticazione_tipo_plugin
    INTO v_plugin
    FROM accrualauth.accrualauth_t_utente u
    JOIN accrualauth.accrualauth_d_autenticazione_tipo at
        ON at.autenticazione_tipo_id = u.autenticazione_tipo_id
    WHERE u.utente_id = NEW.utente_id;

    IF v_plugin IS NULL THEN
        RAISE EXCEPTION 'Utente % non trovato', NEW.utente_id;
    END IF;

    IF v_plugin <> 'SpringSecurity' THEN
        RAISE EXCEPTION
            'Password non consentita per utenti con autenticazione_tipo_plugin = %',
            v_plugin;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_check_password_autenticazione
BEFORE INSERT OR UPDATE
ON accrualauth.accrualauth_t_password
FOR EACH ROW
EXECUTE FUNCTION accrualauth.fn_check_password_autenticazione();

CREATE OR REPLACE FUNCTION accrualauth.fn_check_springsecurity_password()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_plugin VARCHAR(255);
    v_password_count INT;
BEGIN
    -- Recupero plugin
    SELECT at.autenticazione_tipo_plugin
    INTO v_plugin
    FROM accrualauth.accrualauth_d_autenticazione_tipo at
    WHERE at.autenticazione_tipo_id = NEW.autenticazione_tipo_id;

    -- Se non SpringSecurity, nessun vincolo
    IF v_plugin <> 'SpringSecurity' THEN
        RETURN NEW;
    END IF;

    -- Se primo accesso, la password NON è obbligatoria
    IF NEW.utente_primo_accesso IS TRUE THEN
        RETURN NEW;
    END IF;

    -- Da qui in poi: SpringSecurity + NON primo accesso
    SELECT COUNT(*)
    INTO v_password_count
    FROM accrualauth.accrualauth_t_password p
    WHERE p.utente_id = NEW.utente_id
      AND p.fine_validita IS NULL;

    IF v_password_count = 0 THEN
        RAISE EXCEPTION
            'Utente % SpringSecurity non al primo accesso deve avere una password attiva',
            NEW.utente_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE CONSTRAINT TRIGGER trg_check_springsecurity_password
AFTER INSERT OR UPDATE
ON accrualauth.accrualauth_t_utente
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION accrualauth.fn_check_springsecurity_password();


GRANT TRIGGER, REFERENCES, DELETE, TRUNCATE, UPDATE, INSERT, SELECT ON ALL TABLES IN SCHEMA accrualauth TO accrualauth_rw;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA accrualauth TO accrualauth_rw;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA accrualauth TO accrualauth_rw;