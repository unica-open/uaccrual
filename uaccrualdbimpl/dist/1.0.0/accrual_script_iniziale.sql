--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

/* le tabelle che non riportano ente_id sono quelle che idealmente potrebbero stare su uno schema "esterno" o recuperate da anagrafiche già esistenti tipo ANPR */
DROP TABLE IF EXISTS accrual.accrual_t_ente CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_ruolo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_gruppo_azione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_azione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_ruolo_azione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_autenticazione_tipo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_utente CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_entita_contabile CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_fonte CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_evento CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_evento_fonte CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_attributo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_evento_attributo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_soggetto_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_soggetto_tipo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_soggetto CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_soggetto_indirizzo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_regione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_provincia CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_provincia_regione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_nazione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_comune CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_comune_regione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_comune_provincia CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_file_tipo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_file_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_file CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_file_stato CASCADE;
-- Tabelle aggiunte per l'elaborazione di un file
DROP TABLE IF EXISTS accrual.accrual_d_elaborazione_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_elaborazione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_elaborazione_file CASCADE;
DROP TABLE if EXISTS accrual.accrual_t_elaborazione_log CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_causale_tipo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_causale_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_causale CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_causale_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_causale_pdc CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_pdc CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_pdc_tipo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_pdc_famiglia CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_pdc_classe CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_pdc_tipo_relazione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_pdc_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_pdc_legame CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_pdc_soggetto CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_pdc_saldo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_pdc_saldo_prima_nota CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_prima_nota CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_prima_nota_num CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_prima_nota_attributo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_prima_nota_tipo_relazione CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_prima_nota_tipo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_d_prima_nota_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_prima_nota_stato CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_prima_nota_legame CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_prima_nota_importo CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_etichetta CASCADE;
DROP TABLE IF EXISTS accrual.accrual_r_etichetta_prima_nota CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_parametro CASCADE;
DROP TABLE IF EXISTS accrual.accrual_t_async_report CASCADE;
DROP TABLE IF EXISTS accrual.tmp_async_reports  CASCADE;
DROP TYPE IF EXISTS accrual.tipo_fase CASCADE;
DROP TYPE IF EXISTS accrual.tipo_strada CASCADE;
DROP TYPE IF EXISTS accrual.tipo_dato CASCADE;
DROP TYPE IF EXISTS accrual.tipo_importo CASCADE;
DROP TYPE IF EXISTS accrual.tipo_segno CASCADE;

DROP FUNCTION IF EXISTS accrual.accrual_rep001_stampa_mastrino(int4, varchar, date, date, varchar);
DROP FUNCTION IF EXISTS accrual.accrual_rep002_stampa_libro_giornale(int4, varchar, varchar, varchar, varchar, varchar, int4, int4, varchar, varchar, date, date, date, date);
DROP FUNCTION IF EXISTS accrual.accrual_rep003_stampa_bilancio_verifica(int4, varchar, varchar, varchar);




/* gli enti stanno su accrualauth - qui resta per comodità l'id che può anche non corrispondere con quello di accrualauth - deve però corrispondere il codice univoco */
CREATE TABLE accrual.accrual_t_ente (
	ente_id serial4 NOT NULL,
	ente_codice_univoco varchar(50) NOT NULL,
	-- Audit fields
	
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_ente PRIMARY KEY (ente_id)
);
CREATE INDEX idx_accrual_t_ente_1 ON accrual.accrual_t_ente USING btree (ente_codice_univoco);
COMMENT ON TABLE accrual.accrual_t_ente IS 'Tabella degli Enti gestiti da accrualauth - relazione con accrual tramite codice_univoco';

/* i ruoli stanno su accrualauth - qui manteniamo solo il riferimento per le azioni
CREATE TABLE accrual.accrual_d_ruolo (
    ruolo_id serial4 NOT NULL,
    ruolo_codice VARCHAR(50) NOT NULL UNIQUE,
    ruolo_descrizione VARCHAR(255) NOT NULL,

    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrual_d_ruolo_pkey PRIMARY KEY (ruolo_id)	
);
COMMENT ON TABLE accrual.accrual_d_ruolo IS 'Tabella dei Ruoli (decodifica): valutare se ruoli e azioni li vogliamo "trasversali" o per ente. Ente lo terrei solo sulla accrual_r_ruolo_azione)';

CREATE TABLE accrual.accrual_d_ruolo (
    ruolo_codice VARCHAR(50) NOT NULL UNIQUE,
	CONSTRAINT accrual_d_ruolo_pkey PRIMARY KEY (ruolo_codice)	
);
COMMENT ON TABLE accrual.accrual_d_ruolo IS 'Tabella dei Ruoli (valutare se serve): arriva da accrualauth il codice';
*/


CREATE TABLE accrual.accrual_t_gruppo_azione (
	gruppo_azione_id serial4 NOT NULL,
	gruppo_azione_codice varchar(200) NOT NULL,
	gruppo_azione_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrual_t_gruppo_azione_pkey PRIMARY KEY (gruppo_azione_id)	
);
COMMENT ON TABLE accrual.accrual_t_gruppo_azione IS 'Tabella per il raggruppamento delle azioni accrual. Per ora le azioni di primo livello hanno gruppo, le altre no';


CREATE TABLE accrual.accrual_t_azione (
	azione_id serial4 NOT NULL,
	azione_codice varchar(200) NOT NULL,
	azione_descrizione varchar(500) NOT NULL,
	azione_ordine int4 DEFAULT 0 NOT NULL, -- per eventuale ordinamento
	gruppo_azione_id int4 NULL,
	azione_urlapplicazione varchar(500) NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrual_t_azione_pkey PRIMARY KEY (azione_id),
	CONSTRAINT accrual_t_azione_accrual_t_gruppo_azione FOREIGN KEY (gruppo_azione_id) REFERENCES accrual.accrual_t_gruppo_azione(gruppo_azione_id)
	
);
COMMENT ON TABLE accrual.accrual_t_azione IS 'Tabella delle azioni accrual';


CREATE TABLE accrual.accrual_r_ruolo_azione (
	ruolo_azione_r_id serial4 NOT NULL,
	ruolo_codice varchar(50) NOT NULL,
	azione_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_ruolo_azione PRIMARY KEY (ruolo_azione_r_id),
	--CONSTRAINT accrual_d_ruolo_accrual_r_ruolo_azione FOREIGN KEY (ruolo_id) REFERENCES accrual.accrual_d_ruolo(ruolo_id),
	CONSTRAINT accrual_t_azione_accrual_r_ruolo_azione FOREIGN KEY (azione_id) REFERENCES accrual.accrual_t_azione(azione_id),
	CONSTRAINT accrual_t_ente_accrual_r_ruolo_azione FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE UNIQUE INDEX idx_accrual_r_ruolo_azione_1 ON accrual.accrual_r_ruolo_azione USING btree (ente_id, ruolo_codice, azione_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_ruolo_azione_fk_id_azione_idx ON accrual.accrual_r_ruolo_azione USING btree (azione_id);
CREATE INDEX accrual_r_ruolo_azione_fk_ente_id_idx ON accrual.accrual_r_ruolo_azione USING btree (ente_id);
CREATE INDEX accrual_r_ruolo_azione_fk_ruolo_codice_idx ON accrual.accrual_r_ruolo_azione USING btree (ruolo_codice);
COMMENT ON TABLE accrual.accrual_r_ruolo_azione IS 'Tabella di relazione ruolo_azioni';

/*
-- vediamo se creare un type per i valori di plugin possibili (Shibboleth, SpringSecurity, LDAP, ...)
CREATE TABLE accrual.accrual_d_autenticazione_tipo (
    autenticazione_tipo_id serial4 NOT NULL,
    autenticazione_tipo_codice VARCHAR(50) NOT NULL,
    autenticazione_tipo_descrizione VARCHAR(255) NOT NULL,
	autenticazione_tipo_plugin VARCHAR(255) NOT NULL, --Shib / interna
	autenticazione_tipo_url VARCHAR(255) NULL, --pezzo del contesto (url completo o solo accrual-ext/milano, accrual-ext/roma, accrual/accrual) per online, vuoto per i batch
	
	
    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_autenticazione_tipo PRIMARY KEY (autenticazione_tipo_id)
	
);
COMMENT ON TABLE accrual.accrual_d_autenticazione_tipo IS 'Tabella per il tipo di autenticazione (serve per capire quale plugin deve utilizzare la componente centrale di accrual che,sulla base del CF determina che tipo di plugin invocare - Es. SIAC - chiamata a servizio Contabilia, LDAP, Header...)';


CREATE TABLE accrual.accrual_t_utente (
    utente_id serial4 NOT NULL,
    utente_codice_fiscale CHAR(16) NOT NULL UNIQUE,
    utente_codice VARCHAR(100) NOT NULL,
    utente_descrizione VARCHAR(100) NOT NULL,
    utente_email VARCHAR(255),
    ente_id INT NOT NULL REFERENCES accrual.accrual_t_ente(ente_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    ruolo_id INT NOT NULL REFERENCES accrual.accrual_d_ruolo(ruolo_id) ON UPDATE CASCADE ON DELETE RESTRICT,
	autenticazione_tipo_id INT NOT NULL REFERENCES accrual.accrual_d_autenticazione_tipo(autenticazione_tipo_id), 

    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_utente PRIMARY KEY (utente_id)
);
CREATE INDEX idx_utente_cf ON accrual.accrual_t_utente(utente_codice_fiscale);
CREATE INDEX idx_utente_ente ON accrual.accrual_t_utente(ente_id);
CREATE INDEX idx_utente_ruolo ON accrual.accrual_t_utente(ruolo_id);
CREATE INDEX idx_utente_autenticazione_tipo ON accrual.accrual_t_utente(autenticazione_tipo_id);
COMMENT ON TABLE accrual.accrual_t_utente IS 'Tabella degli Utenti';

ALTER TABLE accrual.accrual_t_utente
ADD CONSTRAINT uq_accrual_t_utente_cf_ente UNIQUE (utente_codice_fiscale, ente_id);
*/

CREATE TYPE accrual.tipo_fase AS ENUM (
    'PREVISIONE',
    'GESTIONE',
    'PREDISPOSIZIONE_CONSUNTIVO',
    'CHIUSO'
);


CREATE TABLE accrual.accrual_t_entita_contabile (
	entita_contabile_id serial4 NOT NULL,
	entita_contabile_anno varchar(4) NOT NULL,
	entita_contabile_fase tipo_fase NOT NULL, -- potrebbe essere gestione / previsione...
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT accrual_t_entita_contabile_pk PRIMARY KEY (entita_contabile_id),
	CONSTRAINT accrual_t_ente_accrual_t_entita_contabile FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
COMMENT ON TABLE accrual.accrual_t_entita_contabile IS 'Tabella che riporta la fase contabile: teoricamente in base ad anno e fase contabile potresti, se il tuo ruolo lo permette, vedere / fare cose diverse, verrà riportato sulle tabelle delle prime note)';


CREATE TABLE accrual.accrual_d_fonte (
	fonte_id serial4 NOT NULL,
	fonte_codice varchar(200) NOT NULL,
	fonte_descrizione varchar(500) NOT NULL,
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrual_d_fonte_pk PRIMARY KEY (fonte_id),
	CONSTRAINT accrual_t_ente_accrual_d_fonte FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE INDEX accrual_d_fonte_fk_ente_id_idx ON accrual.accrual_d_fonte USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_d_fonte IS 'Tabella per le fonti contabili e sistemi esterni (per ora SIAC/FACTOTUM/ACCRUAL se creata direttamente dalla GUI)';


/* BEGIN: BLOCCO TABELLE COMUNI_REGIONI_SOGGETTI */

CREATE TABLE accrual.accrual_d_soggetto_stato (
	soggetto_stato_id serial4 NOT NULL,
	soggetto_stato_codice varchar(200) NOT NULL,
	soggetto_stato_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_soggetto_stato PRIMARY KEY (soggetto_stato_id)
);
CREATE UNIQUE INDEX idx_accrual_d_soggetto_stato_1 ON accrual.accrual_d_soggetto_stato USING btree (soggetto_stato_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_soggetto_stato IS 'Tabella stati soggetto - consideriamo che da accrual un operatore possa inserire dei soggetti e ci sia un processo di validazione (PROVVISORIO, VALIDO, ANNULLATO.. come per altri anche gli stati li vedrei trasversali, non per ente)';


CREATE TABLE accrual.accrual_d_soggetto_tipo (
	soggetto_tipo_id serial4 NOT NULL,
	soggetto_tipo_codice varchar(200) NOT NULL,
	soggetto_tipo_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_soggetto_tipo PRIMARY KEY (soggetto_tipo_id)
);
CREATE UNIQUE INDEX idx_accrual_d_soggetto_tipo_1 ON accrual.accrual_d_soggetto_tipo USING btree (soggetto_tipo_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_soggetto_tipo IS 'Tabella tipo soggetto - PF / PG..';


CREATE TABLE accrual.accrual_t_soggetto (
	soggetto_id serial4 NOT NULL,
	soggetto_codice varchar(200) NOT NULL,
	soggetto_descrizione varchar(500) NOT NULL,
	soggetto_codice_fiscale bpchar(16) NULL,
	soggetto_codice_fiscale_estero varchar(500) NULL,
	soggetto_partita_iva varchar(500) NULL,
	soggetto_email varchar(256) NULL,
	soggetto_is_pa bool DEFAULT false NOT NULL,
	soggetto_istituto_di_credito bool DEFAULT false NOT NULL,
	soggetto_stato_id serial4 NOT NULL,
	soggetto_tipo_id serial4 NOT NULL,
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_soggetto PRIMARY KEY (soggetto_id),
	CONSTRAINT accrual_d_soggetto_tipo_accrual_t_soggetto FOREIGN KEY (soggetto_tipo_id) REFERENCES accrual.accrual_d_soggetto_tipo(soggetto_tipo_id),
	CONSTRAINT accrual_d_soggetto_stato_accrual_t_soggetto FOREIGN KEY (soggetto_stato_id) REFERENCES accrual.accrual_d_soggetto_stato(soggetto_stato_id),
	CONSTRAINT accrual_t_ente_accrual_t_soggetto FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE INDEX accrual_t_soggetto_fk_ente_id_idx ON accrual.accrual_t_soggetto USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_t_soggetto IS 'Tabella dei soggetti (non li aggancerei come in accrual a un utente ma solo anagrafiche per le ricerche)';


CREATE TABLE accrual.accrual_t_regione (
	regione_id serial4 NOT NULL,
	regione_codice_istat varchar(200) NULL,
	regione_denominazione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_regione PRIMARY KEY (regione_id)
);
CREATE UNIQUE INDEX idx_accrual_t_regione_1 ON accrual.accrual_t_regione USING btree (regione_codice_istat, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_t_regione IS 'Tabella delle Regioni (a tendere in fase 2 vediamo se riusciamo a puntare a ANPR - https://www.anagrafenazionale.interno.it/) anche qua io ente non lo metterei';


CREATE TABLE accrual.accrual_t_provincia (
	provincia_id serial4 NOT NULL,
	provincia_codice_istat varchar(200) NULL,
	provincia_descrizione varchar(500) NOT NULL,
	provincia_sigla_automobilistica varchar(2) NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_provincia PRIMARY KEY (provincia_id)
);
COMMENT ON TABLE accrual.accrual_t_provincia IS 'Tabella delle province (stesso discorso delle regioni)';


CREATE TABLE accrual.accrual_r_provincia_regione (
	provincia_regione_r_id serial4 NOT NULL,
	provincia_id int4 NOT NULL,
	regione_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_provincia_regione PRIMARY KEY (provincia_regione_r_id),
	CONSTRAINT accrual_t_provincia_accrual_r_provincia_regione FOREIGN KEY (provincia_id) REFERENCES accrual.accrual_t_provincia(provincia_id),
	CONSTRAINT accrual_t_regione_accrual_r_provincia_regione FOREIGN KEY (regione_id) REFERENCES accrual.accrual_t_regione(regione_id)
);
CREATE UNIQUE INDEX idx_accrual_r_provincia_regione_1 ON accrual.accrual_r_provincia_regione USING btree (provincia_id, regione_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_provincia_regione_fk_id_provincia_idx ON accrual.accrual_r_provincia_regione USING btree (provincia_id);
CREATE INDEX accrual_r_provincia_regione_fk_regione_id_idx ON accrual.accrual_r_provincia_regione USING btree (regione_id);
COMMENT ON TABLE accrual.accrual_r_provincia_regione IS 'Tabella di relazione provincia_regione (stesso discorso delle regioni)';


CREATE TABLE accrual.accrual_t_nazione (
	nazione_id serial4 NOT NULL,
	nazione_codice varchar(200) NOT NULL,
	nazione_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_nazione PRIMARY KEY (nazione_id)
);
CREATE UNIQUE INDEX idx_accrual_t_nazione_1 ON accrual.accrual_t_nazione USING btree (nazione_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_r_provincia_regione IS 'Tabella delle nazioni';


CREATE TABLE accrual.accrual_t_comune (
	comune_id serial4 NOT NULL,
	comune_codice_istat varchar(200) NULL,
	comune_denominazione varchar(500) NOT NULL,
	comune_codice_belfiore varchar NULL,
	comune_cap varchar NULL,
	nazione_id int4 NOT NULL,
	provincia_regione_r_id serial4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_comune PRIMARY KEY (comune_id),
	CONSTRAINT accrual_t_nazione_accrual_t_comune FOREIGN KEY (nazione_id) REFERENCES accrual.accrual_t_nazione(nazione_id),
	CONSTRAINT accrual_provincia_regione_accrual_t_comune FOREIGN KEY (provincia_regione_r_id) REFERENCES accrual.accrual_r_provincia_regione(provincia_regione_r_id)
	
);
CREATE INDEX accrual_t_denominazione_comune_idx ON accrual.accrual_t_comune USING btree (comune_denominazione);
CREATE INDEX accrual_t_comune_fk_nazione_id_idx ON accrual.accrual_t_comune USING btree (nazione_id);
CREATE INDEX accrual_t_comune_fk_provincia_regione_id_idx ON accrual.accrual_t_comune USING btree (provincia_regione_r_id);
COMMENT ON TABLE accrual.accrual_t_comune IS 'Tabella dei comuni: rispetto a accrual ho aggiunto qui la fk con provincia e regione e tolto le tabelle di relazione comune/provincia e provincia/regione';


/*
CREATE TABLE accrual.accrual_r_comune_regione (
	comune_regione_id serial4 NOT NULL,
	comune_id int4 NOT NULL,
	regione_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_r_comune_regione PRIMARY KEY (comune_regione_id),
	CONSTRAINT accrual_t_comune_accrual_r_comune_regione FOREIGN KEY (comune_id) REFERENCES accrual.accrual_t_comune(comune_id),
	CONSTRAINT accrual_t_regione_accrual_r_comune_regione FOREIGN KEY (regione_id) REFERENCES accrual.accrual_t_regione(regione_id)
);
CREATE UNIQUE INDEX idx_accrual_r_comune_regione_1 ON accrual.accrual_r_comune_regione USING btree (comune_id, regione_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_comune_regione_fk_id_comune_idx ON accrual.accrual_r_comune_regione USING btree (comune_id);
CREATE INDEX accrual_r_comune_regione_fk_regione_id_idx ON accrual.accrual_r_comune_regione USING btree (regione_id);


CREATE TABLE accrual.accrual_r_comune_provincia (
	comune_provincia_id serial4 NOT NULL,
	comune_id int4 NOT NULL,
	provincia_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_r_comune_provincia PRIMARY KEY (comune_provincia_id),
	CONSTRAINT accrual_t_comune_accrual_r_comune_provincia FOREIGN KEY (comune_id) REFERENCES accrual.accrual_t_comune(comune_id),
	CONSTRAINT accrual_t_provincia_accrual_r_comune_provincia FOREIGN KEY (provincia_id) REFERENCES accrual.accrual_t_provincia(provincia_id)
);
CREATE UNIQUE INDEX idx_accrual_r_comune_provincia_1 ON accrual.accrual_r_comune_provincia USING btree (comune_id, provincia_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_comune_provincia_fk_comune_id_idx ON accrual.accrual_r_comune_provincia USING btree (comune_id);
CREATE INDEX accrual_r_comune_provincia_fk_provincia_id_idx ON accrual.accrual_r_comune_provincia USING btree (provincia_id);
*/

CREATE TYPE accrual.tipo_strada AS ENUM (
    'VIA',
    'VIALE',
    'PIAZZA',
    'CORSO',
    'VICOLO',
    'LARGO',
    'STRADA',
	'PASSAGGIO',
    'PASSEGGIATA',
    'PONTE',
    'SALITA',
    'DISCESA',
    'PIAZZALE',
    'CIRCONVALLAZIONE',
    'ROTONDA'
);

CREATE TABLE accrual.accrual_t_soggetto_indirizzo (
	soggetto_indirizzo_id serial4 NOT NULL,
	soggetto_indirizzo_toponimo varchar(500) NULL,
	soggetto_indirizzo_numero_civico varchar(7) NULL,
	soggetto_indirizzo_frazione varchar(500) NULL,
	soggetto_indirizzo_interno varchar(10) NULL,
	soggetto_indirizzo_codice_zip varchar(10) NULL,
	soggetto_id int4 NOT NULL,
	via_tipo tipo_strada NULL,
	comune_id int4 NULL,
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_soggetto_indirizzo PRIMARY KEY (soggetto_indirizzo_id),
	CONSTRAINT accrual_t_comune_accrual_t_soggetto_indirizzo FOREIGN KEY (comune_id) REFERENCES accrual.accrual_t_comune(comune_id),
	CONSTRAINT accrual_t_ente_accrual_t_soggetto_indirizzo FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_soggetto_accrual_t_soggetto_indirizzo FOREIGN KEY (soggetto_id) REFERENCES accrual.accrual_t_soggetto(soggetto_id)
);
CREATE UNIQUE INDEX idx_accrual_t_soggetto_indirizzo_1 ON accrual.accrual_t_soggetto_indirizzo USING btree (ente_id, soggetto_id, soggetto_indirizzo_toponimo, soggetto_indirizzo_numero_civico, soggetto_indirizzo_frazione, soggetto_indirizzo_interno, soggetto_indirizzo_codice_zip, comune_id, via_tipo, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_t_soggetto_indirizzo_fk_comune_id_idx ON accrual.accrual_t_soggetto_indirizzo USING btree (comune_id);
CREATE INDEX accrual_t_soggetto_indirizzo_fk_ente_id_idx ON accrual.accrual_t_soggetto_indirizzo USING btree (ente_id);
CREATE INDEX accrual_t_soggetto_indirizzo_fk_soggetto_id_idx ON accrual.accrual_t_soggetto_indirizzo USING btree (soggetto_id);
COMMENT ON TABLE accrual.accrual_t_soggetto_indirizzo IS 'Tabella indirizzo del soggetto: non gestiamo sedi secondarie, un unico indirizzo per le ricerche';

/* END: BLOCCO TABELLE COMUNI_REGIONI_SOGGETTI */


/* BEGIN: tabelle per i file da gestire - elaborare (contabilità - sistemi esterni....) */

CREATE TABLE accrual.accrual_d_file_tipo (
	file_tipo_id serial4 NOT NULL,
	file_tipo_codice varchar(200) NOT NULL,
	file_tipo_descrizione varchar(500) NOT NULL,
	fonte_id int4 NOT NULL,   
	azione_id int4 NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_file_tipo PRIMARY KEY (file_tipo_id),
	CONSTRAINT accrual_d_fonte_accrual_d_file_tipo FOREIGN KEY (fonte_id) REFERENCES accrual.accrual_d_fonte(fonte_id),
	CONSTRAINT accrual_t_azione_accrual_d_file_tipo FOREIGN KEY (azione_id) REFERENCES accrual.accrual_t_azione(azione_id)
);
CREATE UNIQUE INDEX idx_accrual_d_file_tipo_1 ON accrual.accrual_d_file_tipo USING btree (file_tipo_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_d_file_tipo_fk_azione_id_idx ON accrual.accrual_d_file_tipo USING btree (azione_id);
CREATE INDEX accrual_d_file_tipo_fk_fonte_id_idx ON accrual.accrual_d_fonte USING btree (fonte_id);
COMMENT ON TABLE accrual.accrual_d_file_tipo IS 'Tabella tipo file associato ad una azione';


CREATE TABLE accrual.accrual_d_file_stato (
	file_stato_id serial4 NOT NULL,
	file_stato_codice varchar(200) NOT NULL,
	file_stato_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_file_stato PRIMARY KEY (file_stato_id)
);
CREATE UNIQUE INDEX idx_accrual_d_file_stato_1 ON accrual.accrual_d_file_stato USING btree (file_stato_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_file_tipo IS 'Tabella tipo file associato ad una azione';


CREATE TABLE accrual.accrual_t_file (
	"file_id" serial4 NOT NULL,
	"file_name" varchar(255) NOT NULL,
	file_size numeric NOT NULL,
	"file_type" varchar(500) NOT NULL,
	file_file bytea NULL,
	file_codice varchar NOT NULL,
	file_note varchar NULL,
	file_tipo_id int4 NULL,
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_file PRIMARY KEY (file_id),
	CONSTRAINT accrual_d_file_tipo_accrual_t_file FOREIGN KEY (file_tipo_id) REFERENCES accrual.accrual_d_file_tipo(file_tipo_id),
	CONSTRAINT accrual_t_ente_accrual_t_file FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE INDEX idx_accrual_t_file_1 ON accrual.accrual_t_file USING btree (file_name, ente_id) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_t_file_fk_ente_id_idx ON accrual.accrual_t_file USING btree (ente_id);
CREATE INDEX accrual_t_file_fk_file_tipo_id_idx ON accrual.accrual_t_file USING btree (file_tipo_id);
COMMENT ON TABLE accrual.accrual_t_file IS 'Tabella principale per la memorizzazione dei file';

CREATE TYPE accrual.tipo_dato AS ENUM ('numerico', 'boolean', 'testo', 'percentuale', 'tabella', 'bytea');

CREATE TABLE accrual.accrual_d_attributo (
	attributo_id serial4 NOT NULL,
	attributo_codice varchar(200) NOT NULL, 
	attributo_descrizione varchar(500) NOT NULL,
	attributo_tipo tipo_dato NOT NULL,
	ente_id int4 NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_attributo PRIMARY KEY (attributo_id),
	CONSTRAINT accrual_t_ente_accrual_d_attributo FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE UNIQUE INDEX idx_accrual_d_attributo_1 ON accrual.accrual_d_attributo USING btree (ente_id, attributo_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_attributo IS 'Tabella degli attributi. Nasce per esigenza delle prime note ma potrebbe servire anche per altro. Gli enti potrebbero averne di diversa natura';


CREATE TABLE accrual.accrual_r_file_stato (
	file_stato_r_id serial4 NOT NULL,
	file_id int4 NOT NULL,
	file_stato_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_file_stato PRIMARY KEY (file_stato_r_id),
	CONSTRAINT accrual_d_file_stato_accrual_r_file_stato FOREIGN KEY (file_stato_id) REFERENCES accrual.accrual_d_file_stato(file_stato_id),
	CONSTRAINT accrual_t_ente_accrual_r_file_stato FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_file_accrual_r_file_stato FOREIGN KEY (file_id) REFERENCES accrual.accrual_t_file(file_id)
);
CREATE UNIQUE INDEX idx_accrual_r_file_stato_1 ON accrual.accrual_r_file_stato USING btree (ente_id, file_id, file_stato_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_file_stato_fk_ente_id_idx ON accrual.accrual_r_file_stato USING btree (ente_id);
CREATE INDEX accrual_r_file_stato_fk_file_id_idx ON accrual.accrual_r_file_stato USING btree (file_id);
CREATE INDEX accrual_r_file_stato_fk_file_stato_id_idx ON accrual.accrual_r_file_stato USING btree (file_stato_id);
COMMENT ON TABLE accrual.accrual_r_file_stato IS 'Tabella per gestire i cambi di stato del file';


CREATE TABLE accrual.accrual_d_elaborazione_stato (
	elaborazione_stato_id serial4 NOT NULL,
	elaborazione_stato_codice varchar(200) NOT NULL,
	elaborazione_stato_descrizione varchar(500) NOT NULL,
	
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_elaborazione_stato PRIMARY KEY (elaborazione_stato_id)
);
CREATE UNIQUE INDEX idx_accrual_d_elaborazione_stato_1 ON accrual.accrual_d_elaborazione_stato USING btree (elaborazione_stato_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_elaborazione_stato IS 'Tabella on gli stati di una elaborazione';


CREATE TABLE accrual.accrual_t_elaborazione (
	elaborazione_id serial4 NOT NULL,
	elaborazione_data timestamp DEFAULT now() NOT NULL,
	elaborazione_stato_id int4 NOT NULL,
	elaborazione_note varchar(1500) NOT NULL,
	file_id int4 NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_t_elaborazione PRIMARY KEY (elaborazione_id),
	CONSTRAINT accrual_d_elaborazione_stato_accrual_t_elaborazione FOREIGN KEY (elaborazione_stato_id) REFERENCES accrual.accrual_d_elaborazione_stato(elaborazione_stato_id),
	CONSTRAINT accrual_t_ente_accrual_t_elaborazione FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_file_accrual_t_elaborazione FOREIGN KEY (file_id) REFERENCES accrual.accrual_t_file(file_id)
);
CREATE INDEX accrual_t_elaborazione_fk_ente_id_idx ON accrual.accrual_t_elaborazione USING btree (ente_id);
CREATE INDEX accrual_t_elaborazione_fk_accrual_elaborazione_stato_id_idx ON accrual.accrual_t_elaborazione USING btree (elaborazione_stato_id);
CREATE INDEX accrual_t_elaborazione_fk_accrual_t_file_accrual_idx ON accrual.accrual_t_elaborazione USING btree (file_id);
COMMENT ON TABLE accrual.accrual_t_elaborazione IS 'Tabella per le elaborazioni dei file';


CREATE TABLE accrual.accrual_t_elaborazione_log (
	elaborazione_log_id serial4 NOT NULL,
	elaborazione_id int4 NULL,
	elaborazione_log_operazione varchar(2500) NOT NULL,
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_elaborazione_t_elaborazione_log PRIMARY KEY (elaborazione_log_id),
	CONSTRAINT accrual_t_elaborazione_accrual_t_elaborazione_log FOREIGN KEY (elaborazione_id) REFERENCES accrual.accrual_t_elaborazione(elaborazione_id),
	CONSTRAINT accrual_t_ente_accrual_t_elaborazione_log FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE INDEX accrual_t_elaborazione_log_elaborazione_id_idx ON accrual.accrual_t_elaborazione_log USING btree (elaborazione_id);
CREATE INDEX accrual_t_elaborazione_log_ente_id_idx ON accrual.accrual_t_elaborazione_log USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_t_elaborazione_log IS 'Tabella che riporta il log di una elaborazione';

/* END: tabelle per i file da gestire - elaborare (contabilità - sistemi esterni....) */


/* BEGIN: tabelle pdc */

CREATE TABLE accrual.accrual_d_pdc_tipo (
	pdc_tipo_id serial4 NOT NULL,
	pdc_tipo_codice varchar(200) NOT NULL,
	pdc_tipo_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_pdc_tipo PRIMARY KEY (pdc_tipo_id)
);
CREATE UNIQUE INDEX idx_accrual_d_pdc_tipo_1 ON accrual.accrual_d_pdc_tipo USING btree (pdc_tipo_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_pdc_tipo IS 'Tabella dei tipi di piano di conto accrual';


CREATE TABLE accrual.accrual_t_pdc_famiglia (
	pdc_famiglia_id serial4 NOT NULL,
	pdc_famiglia_codice varchar(200) NOT NULL,
	pdc_famiglia_descrizione varchar(500) NOT NULL,
	pdc_famiglia_modificabile bool DEFAULT false NOT NULL,
	pdc_famiglia_ricercabile bool DEFAULT false NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_pdc_famiglia PRIMARY KEY (pdc_famiglia_id)
	
);
CREATE UNIQUE INDEX idx_accrual_t_pdc_famiglia_1 ON accrual.accrual_t_pdc_famiglia USING btree (pdc_famiglia_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_t_pdc_famiglia IS 'Tabella famiglia conti (dice se il pdc afferisce alla vecchia normativa 118 o al nuovo accrual)';


CREATE TYPE tipo_importo AS ENUM (
    'LORDO',
    'IMPONIBILE',
    'IMPOSTA'
);

CREATE TYPE tipo_segno AS ENUM (
    'DARE',
    'AVERE'
);

CREATE TABLE accrual.accrual_d_pdc_classe (
	pdc_classe_id serial4 NOT NULL,
	pdc_classe_codice varchar(200) NOT NULL,
	pdc_classe_descrizione varchar(500) NOT NULL,
	pdc_classe_segno tipo_segno NULL,
	pdc_classe_tipo_importo tipo_importo NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_d_pdc_classe PRIMARY KEY (pdc_classe_id)
);
CREATE UNIQUE INDEX idx_accrual_d_pdc_classe_1 ON accrual.accrual_d_pdc_classe USING btree (pdc_classe_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_pdc_classe IS 'Tabella contenente le classi a cui il pdc può fare riferimento - altra considerazione: i pdc accrual non avranno mai la classe OA e OP ma dobbiamo mantenere questi valori per i pdc 118, quindi anche qui inizio/fine validita';


CREATE TABLE accrual.accrual_d_pdc_stato (
	pdc_stato_id serial4 NOT NULL,
	pdc_stato_codice varchar(200) NOT NULL,
	pdc_stato_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_pdc_stato PRIMARY KEY (pdc_stato_id)
);
CREATE UNIQUE INDEX idx_accrual_d_pdc_stato_1 ON accrual.accrual_d_pdc_stato USING btree (pdc_stato_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_pdc_stato IS 'Tabella stati piano dei conti';


CREATE TABLE accrual.accrual_t_pdc (
	pdc_id serial4 NOT NULL,
	pdc_codice varchar(200) NOT NULL,
	pdc_codice_ufficiale varchar(200) NULL,
	pdc_descrizione varchar(500) NOT NULL,
	pdc_id_padre int4 NULL,
	pdc_a_partita bool DEFAULT false NOT NULL,
	pdc_ordine varchar NULL,
	pdc_livello int4 NOT NULL,
	pdc_collegamento_bil_itas varchar(200) NULL,
	pdc_fondo_dotazione bool DEFAULT false NOT NULL,
	pdc_foglia bool DEFAULT false NOT NULL,
	pdc_foglia_non_foglia bool DEFAULT false NOT NULL,
	pdc_siac bool DEFAULT false NOT NULL,
	pdc_famiglia_id int4 NOT NULL,
	pdc_classe_id int4 NULL,
	pdc_tipo_id int4 NOT NULL,
	pdc_stato_id int4 NULL,
	ente_id int4 NOT NULL,
	"version" int8 NOT NULL,
	
	
	-- Validate fiels
	inizio_validita timestamp DEFAULT now() NOT NULL, -- serve per gli inserimenti di pdc aggiunti che devono essere visibili solo da una certa data
	fine_validita timestamp NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_pdc PRIMARY KEY (pdc_id),
	CONSTRAINT accrual_d_pdc_stato_accrual_t_pdc FOREIGN KEY (pdc_stato_id) REFERENCES accrual.accrual_d_pdc_stato(pdc_stato_id),
	CONSTRAINT accrual_d_pdc_tipo_accrual_t_pdc FOREIGN KEY (pdc_tipo_id) REFERENCES accrual.accrual_d_pdc_tipo(pdc_tipo_id),
	CONSTRAINT accrual_t_ente_accrual_t_pdc FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_pdc_accrual_t_pdc FOREIGN KEY (pdc_id_padre) REFERENCES accrual.accrual_t_pdc(pdc_id),
	CONSTRAINT accrual_t_pdc_famiglia_accrual_t_pdc FOREIGN KEY (pdc_famiglia_id) REFERENCES accrual.accrual_t_pdc_famiglia(pdc_famiglia_id),
	CONSTRAINT accrual_d_pdc_classe_accrual_t_pdc FOREIGN KEY (pdc_classe_id) REFERENCES accrual.accrual_d_pdc_classe(pdc_classe_id)
);
CREATE INDEX idx_accrual_t_pdc_1 ON accrual.accrual_t_pdc USING btree (ente_id, pdc_codice, pdc_descrizione, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX idx_accrual_t_pdc_2 ON accrual.accrual_t_pdc USING btree (ente_id, pdc_codice_ufficiale, pdc_descrizione, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_t_pdc_fk_pdc_stato_id_idx ON accrual.accrual_t_pdc USING btree (pdc_stato_id);
CREATE INDEX accrual_t_pdc_fk_ente_id_idx ON accrual.accrual_t_pdc USING btree (ente_id);
CREATE INDEX accrual_t_pdc_fk_pdc_id_padre_idx ON accrual.accrual_t_pdc USING btree (pdc_id_padre);
CREATE INDEX accrual_t_pdc_fk_pdc_tipo_id_idx ON accrual.accrual_t_pdc USING btree (pdc_tipo_id);
CREATE INDEX accrual_t_pdc_fk_pdc_famiglia_id_idx ON accrual.accrual_t_pdc USING btree (pdc_famiglia_id);
CREATE INDEX accrual_t_pdc_fk_pdc_classe_id_idx ON accrual.accrual_t_pdc USING btree (pdc_classe_id);
COMMENT ON TABLE accrual.accrual_t_pdc IS 'Tabella piano dei conti accrual';

-- potrebbe nascere la necessità di una t_pdc_storico per storicizzare gli aggiornamenti sui conti (quando viene valorizzata la gest_data_upd viene memorizzato il record precedente sullo storico)

CREATE TABLE accrual.accrual_d_pdc_tipo_relazione (
	pdc_tipo_relazione_id serial4 NOT NULL,
	pdc_tipo_relazione_codice varchar(200) NOT NULL,
	pdc_tipo_relazione_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_pdc_tipo_relazione PRIMARY KEY (pdc_tipo_relazione_id)
);
CREATE UNIQUE INDEX idx_accrual_d_pdc_tipo_relazione_1 ON accrual.accrual_d_pdc_tipo_relazione USING btree (pdc_tipo_relazione_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_pdc_tipo_relazione IS 'Tipo relazione conti (FONDO - GENERICO)';


CREATE TABLE accrual.accrual_r_pdc_legame (
	pdc_legame_r_id serial4 NOT NULL,
	pdc_legame_a_id int4 NULL,
	pdc_legame_b_id int4 NULL,
	pdc_tipo_relazione_id int4 NULL, 
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_pdc_legame PRIMARY KEY (pdc_legame_r_id),
	CONSTRAINT accrual_d_pdc_tipo_relazione_accrual_r_pdc_legame FOREIGN KEY (pdc_tipo_relazione_id) REFERENCES accrual.accrual_d_pdc_tipo_relazione(pdc_tipo_relazione_id),
	CONSTRAINT accrual_t_ente_accrual_r_pdc_legame FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_pdc_accrual_r_pdc_legame_a FOREIGN KEY (pdc_legame_a_id) REFERENCES accrual.accrual_t_pdc(pdc_id),
	CONSTRAINT accrual_t_pdc_accrual_r_pdc_legame_b FOREIGN KEY (pdc_legame_b_id) REFERENCES accrual.accrual_t_pdc(pdc_id)
);
CREATE UNIQUE INDEX idx_accrual_r_pdc_legame_1 ON accrual.accrual_r_pdc_legame USING btree (ente_id, pdc_legame_a_id, pdc_legame_b_id, pdc_tipo_relazione_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_pdc_legame_fk_ente_id_idx ON accrual.accrual_r_pdc_legame USING btree (ente_id);
CREATE INDEX accrual_r_pdc_legame_fk_pdc_a_id_idx ON accrual.accrual_r_pdc_legame USING btree (pdc_legame_a_id);
CREATE INDEX accrual_r_pdc_legame_fk_pdc_b_id_idx ON accrual.accrual_r_pdc_legame USING btree (pdc_legame_b_id);
CREATE INDEX accrual_r_pdc_legame_fk_pdcrel_id_idx ON accrual.accrual_r_pdc_legame USING btree (pdc_tipo_relazione_id);
COMMENT ON TABLE accrual.accrual_r_pdc_legame IS 'Tabella raccordo/corrispondenza tra pdce conto d.lgs 118 (accrual_t_pdc) e pdc unico accrual/segmento A';


CREATE TABLE accrual.accrual_r_pdc_soggetto (
	pdc_soggetto_r_id serial4 NOT NULL,
	pdc_id int4 NOT NULL,
	soggetto_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_pdc_soggetto PRIMARY KEY (pdc_soggetto_r_id),
	CONSTRAINT accrual_t_ente_accrual_r_pdc_soggetto FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_pdc_accrual_r_pdc_soggetto FOREIGN KEY (pdc_id) REFERENCES accrual.accrual_t_pdc(pdc_id),
	CONSTRAINT accrual_t_soggetto_accrual_r_pdc_soggetto FOREIGN KEY (soggetto_id) REFERENCES accrual.accrual_t_soggetto(soggetto_id)
);
CREATE UNIQUE INDEX idx_accrual_r_pdc_soggetto_1 ON accrual.accrual_r_pdc_soggetto USING btree (ente_id, pdc_id, soggetto_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_pdc_soggetto_fk_ente_id_idx ON accrual.accrual_r_pdc_soggetto USING btree (ente_id);
CREATE INDEX accrual_r_pdc_soggetto_fk_pdc_id_idx ON accrual.accrual_r_pdc_soggetto USING btree (pdc_id);
CREATE INDEX accrual_r_pdc_soggetto_fk_soggetto_id_idx ON accrual.accrual_r_pdc_soggetto USING btree (soggetto_id);
COMMENT ON TABLE accrual.accrual_r_pdc_soggetto IS 'Tabella relazione pdc soggetto...Serve?';


/* END: Tabelle pdc */

/* BEGIN: tabelle causali */

CREATE TABLE accrual.accrual_d_causale_tipo (
	causale_tipo_id serial4 NOT NULL,
	causale_tipo_codice varchar(200) NOT NULL,
	causale_tipo_descrizione varchar(500) NOT NULL,
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_causale_tipo PRIMARY KEY (causale_tipo_id),
	CONSTRAINT accrual_t_ente_accrual_d_causale_tipo FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE UNIQUE INDEX idx_accrual_d_causale_tipo_1 ON accrual.accrual_d_causale_tipo USING btree (ente_id, causale_tipo_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_d_causale_tipo_fk_ente_id_idx ON accrual.accrual_d_causale_tipo USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_d_causale_tipo IS 'Tabella tipi di causale';


CREATE TABLE accrual.accrual_d_causale_stato (
	causale_stato_id serial4 NOT NULL,
	causale_stato_codice varchar(200) NOT NULL,
	causale_stato_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_causale_stato PRIMARY KEY (causale_stato_id)
);
CREATE UNIQUE INDEX idx_accrual_d_causale_stato_1 ON accrual.accrual_d_causale_stato USING btree (causale_stato_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_causale_stato IS 'Tabella stati della causale';

/* END: tabelle causali */

CREATE TABLE accrual.accrual_d_prima_nota_stato (
	prima_nota_stato_id serial4 NOT NULL,
	prima_nota_stato_codice varchar(200) NOT NULL,
	prima_nota_stato_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_prima_nota_stato PRIMARY KEY (prima_nota_stato_id)
);
CREATE UNIQUE INDEX idx_accrual_d_prima_nota_stato_1 ON accrual.accrual_d_prima_nota_stato USING btree (prima_nota_stato_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_prima_nota_stato IS 'Tabella stati prima nota (la farei senza ente)';


CREATE TABLE accrual.accrual_t_evento (
	evento_id serial4 NOT NULL,
	evento_codice varchar(200) NOT NULL,
	evento_descrizione varchar(500) NOT NULL,
	ente_id int4 NOT NULL,
	prima_nota_stato_id int4 NOT NULL,
	--fonte_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrual_t_evento_pk PRIMARY KEY (evento_id),
	CONSTRAINT accrual_t_ente_accrual_t_evento FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_d_prima_nota_stato_accrual_t_evento FOREIGN KEY (prima_nota_stato_id) REFERENCES accrual.accrual_d_prima_nota_stato(prima_nota_stato_id)
);
CREATE INDEX accrual_t_evento_fk_ente_id_idx ON accrual.accrual_t_evento USING btree (ente_id);
CREATE INDEX accrual_t_evento_fk_prima_nota_stato_id_idx ON accrual.accrual_t_evento USING btree (prima_nota_stato_id);
COMMENT ON TABLE accrual.accrual_t_evento IS 'Tabella per gli eventi';


CREATE TABLE accrual.accrual_r_evento_attributo (
	evento_attributo_r_id serial4 NOT NULL,
	evento_attributo_obbligatorio bool DEFAULT false NOT NULL,
	evento_id int4 NOT NULL,
	attributo_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_evento_attributo PRIMARY KEY (evento_attributo_r_id),
	CONSTRAINT accrual_t_ente_accrual_r_evento_attributo FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_evento_accrual_r_evento_attributo FOREIGN KEY (evento_id) REFERENCES accrual.accrual_t_evento(evento_id),
	CONSTRAINT accrual_d_attributo_accrual_r_evento_attributo FOREIGN KEY (attributo_id) REFERENCES accrual.accrual_d_attributo(attributo_id)
);
CREATE UNIQUE INDEX idx_accrual_r_evento_attributo_1 ON accrual.accrual_r_evento_attributo USING btree (ente_id, evento_id, attributo_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_evento_attributo_fk_ente_id_idx ON accrual.accrual_r_evento_attributo USING btree (ente_id);
CREATE INDEX accrual_r_evento_attributo_fk_evento_id_idx ON accrual.accrual_r_evento_attributo USING btree (evento_id);
CREATE INDEX accrual_r_evento_attributo_fk_attributo_id_idx ON accrual.accrual_r_evento_attributo USING btree (attributo_id);
COMMENT ON TABLE accrual.accrual_r_evento_attributo IS 'Tabella di relazione eventi - attributo';


CREATE TABLE accrual.accrual_r_evento_fonte (
	evento_fonte_r_id serial4 NOT NULL,
	evento_id int4 NOT NULL,
	fonte_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_evento_fonte PRIMARY KEY (evento_fonte_r_id),
	CONSTRAINT accrual_t_ente_accrual_r_evento_fonte FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_evento_accrual_r_evento_fonte FOREIGN KEY (evento_id) REFERENCES accrual.accrual_t_evento(evento_id),
	CONSTRAINT accrual_d_fonte_accrual_r_evento_fonte FOREIGN KEY (fonte_id) REFERENCES accrual.accrual_d_fonte(fonte_id)
);
CREATE UNIQUE INDEX idx_accrual_r_evento_fonte_1 ON accrual.accrual_r_evento_fonte USING btree (ente_id, evento_id, fonte_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_evento_fonte_fk_evento_idx ON accrual.accrual_r_evento_fonte USING btree (evento_id);
CREATE INDEX accrual_r_evento_fonte_fk_fonte_id_idx ON accrual.accrual_r_evento_fonte USING btree (fonte_id);
COMMENT ON TABLE accrual.accrual_r_evento_fonte IS 'Tabella di relazione eventi - fonte ';


CREATE TABLE accrual.accrual_t_etichetta (
    etichetta_id serial4 NOT NULL,
    etichetta_codice VARCHAR(50) NOT NULL UNIQUE,
    etichetta_descrizione VARCHAR(255) NOT NULL,
	etichetta_anno int4 NOT NULL,
	etichetta_codice_conto_storico VARCHAR(50) NOT NULL UNIQUE,
    etichetta_descrizione_conto_storico VARCHAR(255) NOT NULL,
	etichetta_valore_conto_storico numeric NOT NULL,
	etichetta_codice_conto_corrente VARCHAR(50) NOT NULL UNIQUE,
    etichetta_descrizione_conto_corrente VARCHAR(255) NOT NULL,
	etichetta_valore_conto_corrente numeric NOT NULL,
	fonte_id serial4 NOT NULL,
	ente_id int4 NOT NULL,

    -- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT accrual_t_etichetta_pkey PRIMARY KEY (etichetta_id),
	CONSTRAINT accrual_t_ente_accrual_t_etichetta FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_d_fonte_accrual_t_etichetta FOREIGN KEY (fonte_id) REFERENCES accrual.accrual_d_fonte(fonte_id)
);
COMMENT ON TABLE accrual.accrual_t_etichetta IS 'Tabella con le riclassificazioni e rivalorizzazioni delle etichette';

/* END: Tabelle prima nota */

/* INIZIO SECONDO PEZZO CAUSALI E PRIME NOTE */

CREATE TABLE accrual.accrual_t_causale (
	causale_id serial4 NOT NULL,
	causale_codice varchar(200) NOT NULL,
	causale_descrizione varchar(500) NOT NULL,
	causale_tipo_id int4 NOT NULL,
	evento_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_t_causale PRIMARY KEY (causale_id),
	CONSTRAINT accrual_d_causale_tipo_accrual_t_causale FOREIGN KEY (causale_tipo_id) REFERENCES accrual.accrual_d_causale_tipo(causale_tipo_id),
	CONSTRAINT accrual_t_evento_accrual_t_causale FOREIGN KEY (evento_id) REFERENCES accrual.accrual_t_evento(evento_id),
	CONSTRAINT accrual_t_ente_accrual_t_causale FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE UNIQUE INDEX idx_accrual_t_causale_1 ON accrual.accrual_t_causale USING btree (ente_id, causale_codice, causale_tipo_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_t_causale_fk_causale_tipo_id_idx ON accrual.accrual_t_causale USING btree (causale_tipo_id);
CREATE INDEX accrual_t_causale_fk_evento_id_idx ON accrual.accrual_t_causale USING btree (evento_id);
CREATE INDEX accrual_t_causale_fk_ente_id_idx ON accrual.accrual_t_causale USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_t_causale IS 'Tabella principale delle causali';


CREATE TABLE accrual.accrual_r_causale_stato (
	causale_stato_r_id serial4 NOT NULL,
	causale_id int4 NOT NULL,
	causale_stato_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_causale_stato PRIMARY KEY (causale_stato_r_id),
	CONSTRAINT accrual_d_causale_stato_accrual_r_causale_stato FOREIGN KEY (causale_stato_id) REFERENCES accrual.accrual_d_causale_stato(causale_stato_id),
	CONSTRAINT accrual_t_causale_accrual_r_causale_stato FOREIGN KEY (causale_id) REFERENCES accrual.accrual_t_causale(causale_id),
	CONSTRAINT accrual_t_ente_accrual_r_causale_stato FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE UNIQUE INDEX idx_accrual_r_causale_stato_1 ON accrual.accrual_r_causale_stato USING btree (ente_id, causale_id, causale_stato_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_causale_stato_fk_causale_id_idx ON accrual.accrual_r_causale_stato USING btree (causale_id);
CREATE INDEX accrual_r_causale_stato_fk_causale_stato_id_idx ON accrual.accrual_r_causale_stato USING btree (causale_stato_id);
CREATE INDEX accrual_r_causale_stato_fk_ente_id_idx ON accrual.accrual_r_causale_stato USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_r_causale_stato IS 'Tabella cambio stato causale';


CREATE TABLE accrual.accrual_r_causale_pdc (
	causale_pdc_r_id serial4 NOT NULL,
	causale_id int4 NOT NULL,
	causale_pdc_tipo_importo tipo_importo NOT NULL,
	causale_pdc_segno tipo_segno NOT NULL,
	pdc_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_causale_pdc PRIMARY KEY (causale_pdc_r_id),
	CONSTRAINT accrual_t_pdc_accrual_r_causale_pdc FOREIGN KEY (pdc_id) REFERENCES accrual.accrual_t_pdc(pdc_id),
	CONSTRAINT accrual_t_causale_accrual_r_causale_pdc FOREIGN KEY (causale_id) REFERENCES accrual.accrual_t_causale(causale_id),
	CONSTRAINT accrual_t_ente_accrual_r_causale_pdc FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE UNIQUE INDEX idx_accrual_r_causale_pdc_1 ON accrual.accrual_r_causale_pdc USING btree (ente_id, causale_id, pdc_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_causale_pdc_fk_causale_id_idx ON accrual.accrual_r_causale_pdc USING btree (causale_id);
CREATE INDEX accrual_r_causale_pdc_fk_pdc_id_idx ON accrual.accrual_r_causale_pdc USING btree (pdc_id);
CREATE INDEX accrual_r_causale_pdc_fk_ente_id_idx ON accrual.accrual_r_causale_pdc USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_r_causale_pdc IS 'Tabella per la relazione tra causale e pdc collegati: serve per proporre i pdc durante l''inserimento della prima nota';

/* BEGIN: Tabelle prime note */
CREATE TABLE accrual.accrual_d_prima_nota_tipo (
	prima_nota_tipo_id serial4 NOT NULL,
	prima_nota_tipo_codice varchar(200) NOT NULL,
	prima_nota_tipo_descrizione varchar(500) NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_prima_nota_tipo PRIMARY KEY (prima_nota_tipo_id)
);
CREATE UNIQUE INDEX idx_accrual_d_prima_nota_tipo_1 ON accrual.accrual_d_prima_nota_tipo USING btree (prima_nota_tipo_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_prima_nota_tipo IS 'Tabella dei tipi di prima nota';


CREATE TABLE accrual.accrual_t_prima_nota (
	prima_nota_id serial4 NOT NULL,
	prima_nota_numero int4 NOT NULL,
	prima_nota_anno int4 NOT NULL,
	prima_nota_descrizione varchar(500) NOT NULL,
	prima_nota_data timestamp NULL,
	prima_nota_progressivogiornale int4 NULL,
	prima_nota_dataregistrazionegiornale timestamp NULL,
	prima_nota_tipo_id int4 NULL,
	causale_id int4 NULL,
	fonte_id int4 NOT NULL,   -- se il file arriva da un sistema esterno tipo FACTOTUM salvo la relazione, oppure se è creata da front-end
	ente_id int4 NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_prima_nota PRIMARY KEY (prima_nota_id),
	CONSTRAINT accrual_t_causale_accrual_t_prima_nota FOREIGN KEY (causale_id) REFERENCES accrual.accrual_t_causale(causale_id),
	CONSTRAINT accrual_d_prima_nota_tipo_accrual_t_prima_nota FOREIGN KEY (prima_nota_tipo_id) REFERENCES accrual.accrual_d_prima_nota_tipo(prima_nota_tipo_id),
	CONSTRAINT accrual_t_ente_accrual_t_prima_nota FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_d_fonte_accrual_t_prima_nota FOREIGN KEY (fonte_id) REFERENCES accrual.accrual_d_fonte(fonte_id)
);
CREATE INDEX accrual_t_prima_nota_fk_causale_id_idx ON accrual.accrual_t_prima_nota USING btree (causale_id);
CREATE INDEX accrual_t_prima_nota_fk_prima_nota_tipo_id_idx ON accrual.accrual_t_prima_nota USING btree (prima_nota_tipo_id);
CREATE INDEX accrual_t_prima_nota_fk_ente_id_idx ON accrual.accrual_t_prima_nota USING btree (ente_id);
CREATE INDEX accrual_t_prima_nota_fk_fonte_id_idx ON accrual.accrual_t_prima_nota USING btree (fonte_id);

COMMENT ON TABLE accrual.accrual_t_prima_nota IS 'Tabelle prime note';


CREATE TABLE accrual.accrual_t_prima_nota_num (
	prima_nota_num_id serial4 NOT NULL,
	prima_nota_num_anno varchar(4) NOT NULL,
	prima_nota_num_numero int4 NOT NULL,
	prima_nota_num_is_definitiva bool DEFAULT true NOT NULL,
	ente_id int4 NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_prima_nota_num PRIMARY KEY (prima_nota_num_id),
	CONSTRAINT accrual_t_ente_accrual_t_prima_nota_num FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id)
);
CREATE INDEX accrual_t_prima_nota_num_fk_ente_id_idx ON accrual.accrual_t_prima_nota_num USING btree (ente_id);
COMMENT ON TABLE accrual.accrual_t_prima_nota_num IS 'Tabelle numerazione prime note';

-- importo corrispondente al pdc unico accrual/segmento A nell'anno di bilancio... I saldi arrivano dal sistema contabile.
-- Potrei non trovare la corrispondenza con l'id motivo per il quale mettiamo in tabella anche il codice
CREATE TABLE accrual.accrual_t_pdc_saldo (
	pdc_saldo_id serial4 NOT NULL,
	pdc_saldo_importo numeric NULL,
	pdc_saldo_anno int4 NOT NULL,
	pdc_id int4 NULL,
	pdc_codice varchar(200) NOT NULL, 
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_pdc_saldo PRIMARY KEY (pdc_saldo_id),
	CONSTRAINT accrual_t_ente_accrual_t_pdc_saldo FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_pdc_accrual_t_pdc_saldo FOREIGN KEY (pdc_id) REFERENCES accrual.accrual_t_pdc(pdc_id)
);
CREATE INDEX accrual_t_pdc_saldo_fk_ente_id_idx ON accrual.accrual_t_pdc_saldo USING btree (ente_id);
CREATE INDEX accrual_t_pdc_saldo_fk_pdc_id_idx ON accrual.accrual_t_pdc_saldo USING btree (pdc_id);
COMMENT ON TABLE accrual.accrual_t_pdc_saldo IS 'Tabella saldo piano dei conti accrual per anno / tipo bilancio';

--legame tra voce segmento A e voce schema bilancio itas1: verificare con haitham se andrà nelle tabelle x l'xbrl

CREATE TABLE accrual.accrual_r_pdc_saldo_prima_nota (
	pdc_saldo_prima_nota_r_id serial4 NOT NULL,
	prima_nota_id int4 NOT NULL,
	pdc_saldo_id serial4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_pdc_saldo_prima_nota PRIMARY KEY (pdc_saldo_prima_nota_r_id),
	CONSTRAINT accrual_t_pdc_saldo_accrual_r_pdc_saldo_prima_nota FOREIGN KEY (pdc_saldo_id) REFERENCES accrual.accrual_t_pdc_saldo(pdc_saldo_id),
	CONSTRAINT accrual_t_ente_accrual_r_pdc_saldo_prima_nota FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_prima_nota_accrual_r_pdc_saldo_prima_nota FOREIGN KEY (prima_nota_id) REFERENCES accrual.accrual_t_prima_nota(prima_nota_id)
);
CREATE UNIQUE INDEX idx_accrual_r_pdc_saldo_prima_nota_1 ON accrual.accrual_r_pdc_saldo_prima_nota USING btree (ente_id, prima_nota_id, pdc_saldo_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_pdc_saldo_prima_nota_fk_ente_id_idx ON accrual.accrual_r_pdc_saldo_prima_nota USING btree (ente_id);
CREATE INDEX accrual_r_pdc_saldo_prima_nota_fk_prima_nota_id_idx ON accrual.accrual_r_pdc_saldo_prima_nota USING btree (prima_nota_id);
CREATE INDEX accrual_r_pdc_saldo_prima_nota_fk_attributo_id_idx ON accrual.accrual_r_pdc_saldo_prima_nota USING btree (pdc_saldo_id);
COMMENT ON TABLE accrual.accrual_r_pdc_saldo_prima_nota IS 'Tabella di relazione tra saldi pdc e prime note';

CREATE TABLE accrual.accrual_t_prima_nota_importo (
	prima_nota_importo_id serial4 NOT NULL,
	prima_nota_importo_importo numeric NOT NULL,
	prima_nota_importo_segno tipo_segno NOT NULL,
	prima_nota_id int4 NOT NULL,
	pdc_id int4 NOT NULL,
	ente_id int4 NOT NULL,

	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_t_prima_nota_importo PRIMARY KEY (prima_nota_importo_id),
	CONSTRAINT accrual_t_pdc_accrual_t_prima_nota_importo FOREIGN KEY (pdc_id) REFERENCES accrual.accrual_t_pdc(pdc_id),
	CONSTRAINT accrual_t_ente_accrual_t_prima_nota_importo FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_prima_nota_t_prima_nota_importo FOREIGN KEY (prima_nota_id) REFERENCES accrual.accrual_t_prima_nota(prima_nota_id)
);
CREATE INDEX accrual_t_prima_nota_importo_fk_pdc_id_idx ON accrual.accrual_t_prima_nota_importo USING btree (pdc_id);
CREATE INDEX accrual_t_prima_nota_importo_fk_ente_id_idx ON accrual.accrual_t_prima_nota_importo USING btree (ente_id);
CREATE INDEX accrual_t_prima_nota_importo_fk_prima_nota_id_idx ON accrual.accrual_t_prima_nota USING btree (prima_nota_id);
COMMENT ON TABLE accrual.accrual_t_prima_nota_importo IS 'Tabella importo in dare/avere delle prime note';



CREATE TABLE accrual.accrual_r_prima_nota_attributo (
	prima_nota_attributo_r_id serial4 NOT NULL,
	prima_nota_id int4 NOT NULL,
	attributo_id int4 NOT NULL,
	prima_nota_attributo_valore varchar(500) NOT NULL,
	prima_nota_importo_id int4 NULL,
	ente_id int4 NOT NULL,
	file_id int4 NULL, -- se bytea nel codice mettere il riferimento alla accrual_t_file

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
	
	CONSTRAINT pk_accrual_r_prima_nota_attributo PRIMARY KEY (prima_nota_attributo_r_id),
	CONSTRAINT accrual_d_attributo_accrual_r_prima_nota_attributo FOREIGN KEY (attributo_id) REFERENCES accrual.accrual_d_attributo(attributo_id),
	CONSTRAINT accrual_t_ente_accrual_r_prima_nota_attributo FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_prima_nota_accrual_r_prima_nota_attributo FOREIGN KEY (prima_nota_id) REFERENCES accrual.accrual_t_prima_nota(prima_nota_id),
	CONSTRAINT accrual_t_prima_nota_importo_accrual_r_prima_nota_attributo FOREIGN KEY (prima_nota_importo_id) REFERENCES accrual.accrual_t_prima_nota_importo(prima_nota_importo_id),
	CONSTRAINT accrual_t_file_accrual_d_attributo FOREIGN KEY (file_id) REFERENCES accrual_t_file(file_id)
);
--CREATE UNIQUE INDEX idx_accrual_r_prima_nota_attributo_1 ON accrual.accrual_r_prima_nota_attributo USING btree (prima_nota_id, attributo_id, gest_data_ins, ente_id) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_prima_nota_attributo_fk_ente_id_idx ON accrual.accrual_r_prima_nota_attributo USING btree (ente_id);
CREATE INDEX accrual_r_prima_nota_attributo_fk_prima_nota_id_idx ON accrual.accrual_r_prima_nota_attributo USING btree (prima_nota_id);
CREATE INDEX accrual_r_prima_nota_attributo_fk_prima_nota_importo_id_idx ON accrual.accrual_r_prima_nota_attributo USING btree (prima_nota_importo_id);
CREATE INDEX accrual_r_prima_nota_attributo_fk_attributo_id_idx ON accrual.accrual_r_prima_nota_attributo USING btree (attributo_id);
COMMENT ON TABLE accrual.accrual_r_prima_nota_attributo IS 'Tabella di attributi delle prime note';


CREATE OR REPLACE FUNCTION accrual.fn_chk_attributo_tipo_bytea()
RETURNS trigger AS
$$
DECLARE
    v_tipo text;
BEGIN
    SELECT attributo_tipo
    INTO v_tipo
    FROM accrual.accrual_d_attributo
    WHERE attributo_id = NEW.attributo_id;

    IF v_tipo = 'bytea' AND NEW.file_id IS NULL THEN
        RAISE EXCEPTION
            'file_id obbligatorio per attributi di tipo bytea (attributo_id=%)',
            NEW.attributo_id;
    END IF;

    IF v_tipo <> 'bytea' AND NEW.file_id IS NOT NULL THEN
        RAISE EXCEPTION
            'file_id deve essere NULL per attributi non bytea (attributo_id=%)',
            NEW.attributo_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chk_attributo_tipo_bytea
BEFORE INSERT OR UPDATE
ON accrual.accrual_r_prima_nota_attributo
FOR EACH ROW
EXECUTE FUNCTION accrual.fn_chk_attributo_tipo_bytea();


CREATE TABLE accrual.accrual_d_prima_nota_tipo_relazione (
	prima_nota_tipo_relazione_id serial4 NOT NULL,
	prima_nota_tipo_relazione_codice varchar(200) NOT NULL,
	prima_nota_tipo_relazione_descrizione varchar(500) NOT NULL,
	prima_nota_rel_utilizzabile bool DEFAULT true NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL,
	
	CONSTRAINT pk_accrual_d_prima_nota_tipo_relazione PRIMARY KEY (prima_nota_tipo_relazione_id)
);
CREATE UNIQUE INDEX idx_accrual_d_prima_nota_tipo_relazione_1 ON accrual.accrual_d_prima_nota_tipo_relazione USING btree (prima_nota_tipo_relazione_codice, gest_data_ins) WHERE (gest_data_del IS NULL);
COMMENT ON TABLE accrual.accrual_d_prima_nota_tipo_relazione IS 'Tabella tipo di relazione tra due prime note';

CREATE TABLE accrual.accrual_r_prima_nota_stato (
	prima_nota_stato_r_id serial4 NOT NULL,
	prima_nota_id int4 NOT NULL,
	prima_nota_stato_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_prima_nota_stato PRIMARY KEY (prima_nota_stato_r_id),
	CONSTRAINT accrual_d_prima_nota_stato_accrual_r_prima_nota_stato FOREIGN KEY (prima_nota_stato_id) REFERENCES accrual.accrual_d_prima_nota_stato(prima_nota_stato_id),
	CONSTRAINT accrual_t_ente_accrual_r_prima_nota_stato FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_prima_nota_accrual_r_prima_nota_stato FOREIGN KEY (prima_nota_id) REFERENCES accrual.accrual_t_prima_nota(prima_nota_id)
);
CREATE UNIQUE INDEX idx_accrual_r_prima_nota_stato_1 ON accrual.accrual_r_prima_nota_stato USING btree (ente_id, prima_nota_id, prima_nota_stato_id, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_prima_nota_stato_fk_ente_id_idx ON accrual.accrual_r_prima_nota_stato USING btree (ente_id);
CREATE INDEX accrual_r_prima_nota_stato_fk_prima_nota_id_idx ON accrual.accrual_r_prima_nota_stato USING btree (prima_nota_id);
CREATE INDEX accrual_r_prima_nota_stato_fk_prima_nota_stato_id_idx ON accrual.accrual_r_prima_nota_stato USING btree (prima_nota_stato_id);
COMMENT ON TABLE accrual.accrual_r_prima_nota_stato IS 'Tabella cambi stato delle prime note';

CREATE TABLE accrual.accrual_r_prima_nota_legame (
	prima_nota_legame_r_id serial4 NOT NULL,
	prima_nota_legame_note varchar NULL,
	prima_nota_legame_id_da int4 NOT NULL,
	prima_nota_legame_id_a int4 NOT NULL,
	prima_nota_tipo_relazione_id int4 NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_prima_nota_legame PRIMARY KEY (prima_nota_legame_r_id),
	CONSTRAINT accrual_d_prima_nota_tipo_relazione_accrual_r_prima_nota_legame FOREIGN KEY (prima_nota_tipo_relazione_id) REFERENCES accrual.accrual_d_prima_nota_tipo_relazione(prima_nota_tipo_relazione_id),
	CONSTRAINT accrual_t_ente_accrual_r_prima_nota_legame FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_prima_nota_accrual_r_prima_nota_legame_a FOREIGN KEY (prima_nota_legame_id_a) REFERENCES accrual.accrual_t_prima_nota(prima_nota_id),
	CONSTRAINT accrual_t_prima_nota_accrual_r_prima_nota_legame_da FOREIGN KEY (prima_nota_legame_id_da) REFERENCES accrual.accrual_t_prima_nota(prima_nota_id)
);
CREATE UNIQUE INDEX idx_accrual_r_prima_nota_legame_1 ON accrual.accrual_r_prima_nota_legame USING btree (ente_id, prima_nota_legame_id_da, prima_nota_legame_id_a, gest_data_ins) WHERE (gest_data_del IS NULL);
CREATE INDEX accrual_r_prima_nota_legame_fk_ente_id_idx ON accrual.accrual_r_prima_nota_legame USING btree (ente_id);
CREATE INDEX accrual_r_prima_nota_legame_fk_prima_nota_id_a_idx ON accrual.accrual_r_prima_nota_legame USING btree (prima_nota_legame_id_a);
CREATE INDEX accrual_r_prima_nota_legame_fk_prima_nota_id_da_idx ON accrual.accrual_r_prima_nota_legame USING btree (prima_nota_legame_id_da);
CREATE INDEX accrual_r_prima_nota_legame_fk_prima_nota_tipo_relazione_id_idx ON accrual.accrual_r_prima_nota_legame USING btree (prima_nota_tipo_relazione_id);
COMMENT ON TABLE accrual.accrual_r_prima_nota_legame IS 'Tabella di relazione tra due prime note';


CREATE TABLE accrual.accrual_r_etichetta_prima_nota (
	etichetta_prima_nota_r_id serial4 NOT NULL,
	etichetta_id int4 NOT NULL,
	prima_nota_id int4 NOT NULL,
	ente_id int4 NOT NULL,
	
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
	
	CONSTRAINT pk_accrual_r_etichetta_prima_nota PRIMARY KEY (etichetta_prima_nota_r_id),
	CONSTRAINT accrual_t_ente_accrual_r_etichetta_prima_nota FOREIGN KEY (ente_id) REFERENCES accrual.accrual_t_ente(ente_id),
	CONSTRAINT accrual_t_prima_nota_accrual_r_etichetta_prima_nota FOREIGN KEY (prima_nota_id) REFERENCES accrual.accrual_t_prima_nota(prima_nota_id),
	CONSTRAINT accrual_t_etichetta_accrual_r_etichetta_prima_nota FOREIGN KEY (etichetta_id) REFERENCES accrual.accrual_t_etichetta(etichetta_id)
	
);
CREATE INDEX accrual_r_etichetta_prima_nota_fk_ente_id_idx ON accrual.accrual_r_etichetta_prima_nota USING btree (ente_id);
CREATE INDEX accrual_r_etichetta_prima_nota_fk_prima_nota_id_idx ON accrual.accrual_r_etichetta_prima_nota USING btree (prima_nota_id);
CREATE INDEX accrual_r_etichetta_prima_nota_fk_etichetta_id_idx ON accrual.accrual_r_etichetta_prima_nota USING btree (etichetta_id);
COMMENT ON TABLE accrual.accrual_r_etichetta_prima_nota IS 'Tabella di relazione tra etichetta e prima nota';

/* FINE SECONDO PEZZO CAUSALI E PRIME NOTE */

CREATE TABLE accrual.accrual_t_parametro (
	parametro_id serial4 NOT NULL,
	parametro_nome varchar NOT NULL,
	parametro_valore varchar NULL,
	parametro_abilitato bool DEFAULT true NOT NULL,
	parametro_note text NULL,
	ente_id int4 NOT NULL,
	
	-- Audit fields
	gest_data_ins timestamp DEFAULT now() NOT NULL,
	gest_data_upd timestamp NULL,
	gest_data_del timestamp NULL,
	gest_utente_ins varchar(200) DEFAULT 'admin' NOT NULL,
	gest_utente_upd varchar(200) NULL,
	gest_utente_del varchar(200) NULL
);
CREATE INDEX accrual_t_parametro_fk_ente_id_idx ON accrual.accrual_t_parametro USING btree (ente_id);
CREATE UNIQUE INDEX accrual_t_parametro_ente_id_nome_idx ON accrual.accrual_t_parametro USING btree (ente_id, parametro_nome);
COMMENT ON TABLE accrual.accrual_t_parametro IS 'Tabella per configurazioni specifiche di un ente';


ALTER TABLE accrual.accrual_t_pdc
ADD CONSTRAINT uq_accrual_t_pdc_ente_fam_classe_codice
UNIQUE (ente_id, pdc_famiglia_id, pdc_classe_id, pdc_codice_ufficiale);

-- Rimuovo i vincoli di unicità errati sui conti
ALTER TABLE accrual.accrual_t_etichetta DROP CONSTRAINT IF EXISTS accrual_t_etichetta_etichetta_codice_conto_storico_key;
ALTER TABLE accrual.accrual_t_etichetta DROP CONSTRAINT IF EXISTS accrual_t_etichetta_etichetta_codice_conto_corrente_key;
 
-- Rimuovo il vincolo di unicità globale sul codice etichetta
ALTER TABLE accrual.accrual_t_etichetta DROP CONSTRAINT IF EXISTS accrual_t_etichetta_etichetta_codice_key;

DROP INDEX idx_accrual_d_file_tipo_1;
CREATE UNIQUE INDEX idx_accrual_d_file_tipo_1 ON accrual.accrual_d_file_tipo USING btree (fonte_id, file_tipo_codice, gest_data_ins) WHERE (gest_data_del IS NULL);



CREATE TABLE accrual.accrual_t_async_report ( 
id uuid NOT NULL, ente_id int8 NOT NULL, 
status varchar(50) NOT NULL, format varchar(50) NOT NULL, 
rptdesign_file_name varchar(500) NOT NULL, 
rptconfig_file_name varchar(500) NULL, 
parameters jsonb NULL, 
file_content bytea NULL, 
file_size int8 NULL, error_message text NULL, 
progress_percentage int4 NULL, 
created_at timestamp DEFAULT now() NOT NULL, 
started_at timestamp NULL, 
completed_at timestamp NULL, 
"version" int8 DEFAULT 0 NOT NULL, 
CONSTRAINT accrual_t_async_report_pkey PRIMARY KEY (id), 
CONSTRAINT chk_accrual_t_async_report_progress CHECK (((progress_percentage IS NULL) OR ((progress_percentage >= 0) AND (progress_percentage <= 100)))), CONSTRAINT chk_accrual_t_async_report_status CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('PROCESSING'::character varying)::text, ('COMPLETED'::character varying)::text, ('ERROR'::character varying)::text]))));
CREATE INDEX idx_async_report_completed ON accrual.accrual_t_async_report USING btree (completed_at DESC) WHERE (completed_at IS NOT NULL);
COMMENT ON INDEX accrual.idx_async_report_completed IS 'CDU-015: Ottimizza ricerca report completati (completed_at non NULL)';
CREATE INDEX idx_async_report_created ON accrual.accrual_t_async_report USING btree (created_at DESC);
COMMENT ON INDEX accrual.idx_async_report_created IS 'CDU-015: Ottimizza ordinamento report per data creazione decrescente';
CREATE INDEX idx_async_report_ente_completed ON accrual.accrual_t_async_report USING btree (ente_id, completed_at DESC);
COMMENT ON INDEX accrual.idx_async_report_ente_completed IS 'CDU-015: Ottimizza ricerca report completati per ente';
CREATE INDEX idx_async_report_ente_created ON accrual.accrual_t_async_report USING btree (ente_id, created_at);
COMMENT ON INDEX accrual.idx_async_report_ente_created IS 'CDU-015: Ottimizza ricerca report per ente ordinati per data creazione';
CREATE INDEX idx_async_report_ente_status ON accrual.accrual_t_async_report USING btree (ente_id, status);
COMMENT ON INDEX accrual.idx_async_report_ente_status IS 'CDU-015: Ottimizza ricerca report per ente e stato';
CREATE INDEX idx_async_report_processing ON accrual.accrual_t_async_report USING btree (status, created_at) WHERE ((status)::text = 'PROCESSING'::text);
COMMENT ON INDEX accrual.idx_async_report_processing IS 'CDU-015: Ottimizza monitoraggio report in elaborazione';
CREATE INDEX idx_async_report_status ON accrual.accrual_t_async_report USING btree (status);
COMMENT ON INDEX accrual.idx_async_report_status IS 'CDU-015: Ottimizza ricerca report per stato (monitoraggio)';
COMMENT ON TABLE accrual.accrual_t_async_report IS 'CDU-015: Tabella per memorizzare i report asincroni generati dal sistema';

-- Column comments
COMMENT ON COLUMN accrual.accrual_t_async_report.id IS 'Identificativo univoco del report (UUID)';
COMMENT ON COLUMN accrual.accrual_t_async_report.ente_id IS 'ID dell''ente proprietario del report (multi-tenancy)';
COMMENT ON COLUMN accrual.accrual_t_async_report.status IS 'Stato di elaborazione del report: PENDING, PROCESSING, COMPLETED, ERROR';
COMMENT ON COLUMN accrual.accrual_t_async_report.format IS 'Formato di output del report (es. xlsx, pdf, csv)';
COMMENT ON COLUMN accrual.accrual_t_async_report.rptdesign_file_name IS 'Nome del file di design del report (.rptdesign)';
COMMENT ON COLUMN accrual.accrual_t_async_report.rptconfig_file_name IS 'Nome del file di configurazione del report (.rptconfig)';
COMMENT ON COLUMN accrual.accrual_t_async_report.parameters IS 'Parametri di generazione del report in formato JSON';
COMMENT ON COLUMN accrual.accrual_t_async_report.file_content IS 'Contenuto binario del file report generato (LOB)';
COMMENT ON COLUMN accrual.accrual_t_async_report.file_size IS 'Dimensione del file report in byte';
COMMENT ON COLUMN accrual.accrual_t_async_report.error_message IS 'Messaggio di errore dettagliato (se status = ERROR)';
COMMENT ON COLUMN accrual.accrual_t_async_report.progress_percentage IS 'Percentuale di avanzamento elaborazione (0-100, solo se status = PROCESSING)';
COMMENT ON COLUMN accrual.accrual_t_async_report.created_at IS 'Data e ora di creazione della richiesta di generazione report';
COMMENT ON COLUMN accrual.accrual_t_async_report.started_at IS 'Data e ora di inizio elaborazione del report';
COMMENT ON COLUMN accrual.accrual_t_async_report.completed_at IS 'Data e ora di completamento elaborazione del report';
COMMENT ON COLUMN accrual.accrual_t_async_report."version" IS 'Versione del record per optimistic locking (Hibernate)';


CREATE OR REPLACE FUNCTION accrual.accrual_rep001_stampa_mastrino(p_ente_id integer, p_anno_bilancio character varying, p_data_da date, p_data_a date, p_conto_pdc character varying)
 RETURNS TABLE(pdc_id1 integer, codice_pdc1 character varying, descr_pdc1 character varying, pdc_id2 integer, codice_pdc2 character varying, descr_pdc2 character varying, pdc_id3 integer, codice_pdc3 character varying, descr_pdc3 character varying, pdc_id4 integer, codice_pdc4 character varying, descr_pdc4 character varying, pdc_id5 integer, codice_pdc5 character varying, descr_pdc5 character varying, pdc_id6 integer, codice_pdc6 character varying, descr_pdc6 character varying, pdc_id7 integer, codice_pdc7 character varying, descr_pdc7 character varying, pdc_id8 integer, codice_pdc8 character varying, descr_pdc8 character varying, pdc_id9 integer, codice_pdc9 character varying, descr_pdc9 character varying, pdc_id10 integer, codice_pdc10 character varying, descr_pdc10 character varying, livello integer, prima_nota_id integer, prima_nota_numero integer, prima_nota_data date, prima_nota_progressivogiornale integer, prima_nota_dataregistrazionegiornale date, fonte_codice character varying, fonte_descrizione character varying, evento_codice character varying, evento_descrizione character varying, causale_codice character varying, causale_descrizione character varying, prima_nota_stato_codice character varying, prima_nota_stato_descrizione character varying, codice_tipo_prima_nota character varying, desc_tipo_prima_nota character varying, elenco_attributi character varying, importo_dare numeric, importo_avere numeric, importo_ini_dare numeric, importo_ini_avere numeric, display_error character varying)
 LANGUAGE plpgsql
AS $function$
DECLARE
pdce_conto_id_in integer;
conta_prime_note integer;
cod_pdc_classe varchar;
cod_pdc_causale varchar;
importo_ini_dare numeric;
importo_ini_avere numeric;
parametri_causali varchar[];

BEGIN

/*
	26/02/2026.
    Versione 1 della procedura che estrae i dati del mastrino per ACCRUAL.
    
    Parametri in input:
    - p_ente_id; OBBLIGATORIO; id dell'ente;
    - p_anno_bilancio; OBBLIGATORIO; anno del bilancio;
    - p_data_da; OBBLIGATORIO; data a partire dalla quale cercare la prima nota;
    - p_data_a; OBBLIGATORIO; data fino alla quale cercare la prima nota;
    - p_conto_pdc, OBBLIGATORIO; codice del conto da cercare.
    
*/

--cerco l'id del conto in input e anche la sua famiglia.
select pdc.pdc_id, pdc_classe.pdc_classe_codice
	into pdce_conto_id_in, cod_pdc_classe
from accrual_t_pdc pdc, accrual_d_pdc_classe pdc_classe
where pdc_classe.pdc_classe_id=pdc.pdc_classe_id
and pdc.ente_id = p_ente_id
and pdc.pdc_codice_ufficiale = p_conto_pdc
and pdc.fine_validita IS NULL
and pdc.gest_data_del IS NULL
and pdc_classe.fine_validita IS NULL
and pdc_classe.gest_data_del IS NULL;

--se non esiste restituisco un errore al report.
IF NOT FOUND THEN
    display_error='Il codice PDC indicato ('||p_conto_pdc||') e'' inesistente';
    return next;
    return;
END IF;
    
raise notice 'pdce_conto_id_in di % = % - codice classe = %', p_conto_pdc,pdce_conto_id_in, cod_pdc_classe;

conta_prime_note:=0;

--verifico se esistono prime note definitive per il conto indicato nel periodo sepcificato
--per segnalare l'eventuale mancanza prima di effettuare la query vera e propria.
select count(*)
into conta_prime_note  
from accrual_t_prima_nota_importo imp,
	accrual_t_prima_nota pn,
    accrual_r_prima_nota_stato rstato, 
    accrual_d_prima_nota_stato dstato
where pn.prima_nota_id = imp.prima_nota_id
  and rstato.prima_nota_id= pn.prima_nota_id
  and rstato.prima_nota_stato_id=dstato.prima_nota_stato_id
  and imp.ente_id = p_ente_id
  and imp.pdc_id = pdce_conto_id_in
  and pn.prima_nota_anno = p_anno_bilancio::integer
  and dstato.prima_nota_stato_codice = 'D' --solo le prime note definitive.
  and date_trunc('day',pn.prima_nota_dataregistrazionegiornale) 
    	between p_data_da and p_data_a
  and imp.gest_data_del is null
  and pn.gest_data_del is null
  and rstato.gest_data_del is null
  and rstato.fine_validita is null;
  
if conta_prime_note = 0 then
	display_error='Non ci sono prime note per il conto '||p_conto_pdc|| ' nel periodo indicato';
    return next;
    return;
END IF;

/*
Per importo INIZIALE date/avere, da Donatella:

Andrea dice che il saldo iniziale dovrebbe rappresentare la prima nota di Apertura Patrimoniale 
nell'anno, che in siac non si riusciva a estrarre però mi sembra strano, hai verificato che in 
siac c'era 0 schiantato?

1) classe AP-attivo patrimoniale: per il Dare/Avere del saldo iniziale devo cercare la prima nota 
definitiva con causale APE_ATT
 
2) classe PP-Passivo Patrimoniale: per il Dare/Avere del saldo iniziale devo cercare la prima nota 
definitiva con causale APE_PASS
 
3) classe CE-Costi E Oneri D'Esercizio: per il Dare/Avere del saldo iniziale devo cercare la prima 
nota definitiva con causale APE_CE
 
4) classe RE-Ricavi E Proventi D'Esercizio: per il Dare/Avere del saldo iniziale devo cercare la 
prima nota definitiva con causale APE_RE
 
*/

--in base alla classe del conto in input imposto la relativa causale per la ricerca della 
--prima nota di Apertura Patrimoniale che serve per avere l'importo iniziale dare/avere.
--La causale e' configurata sulla tabella accrual_t_parametro con il parametro CAUSALI_APERTURA_118.
--I valori sono in un ordine preciso:
--1 APE_ATT per classe AP
--2 APE_PASS per classe PP
--3 APE_CE per classe CE
--4 APE_RE per classe RE.
select string_to_array(parametro_valore,',')
	into parametri_causali
from accrual_t_parametro a
where a.ente_id=p_ente_id
and a.parametro_nome='CAUSALI_APERTURA_118'
and a.gest_data_del IS NULL;

IF NOT FOUND THEN
    display_error='Per questo ente non è stato configuarato il parametro CAUSALI_APERTURA_118';
    return next;
    return;
END IF;

--raise notice 'valore 1 = %', parametri_causali[1];
--raise notice 'valore 2 = %', parametri_causali[2];
--raise notice 'valore 3 = %', parametri_causali[3];
--raise notice 'valore 4 = %', parametri_causali[4];

if cod_pdc_classe = 'AP' then
	cod_pdc_causale = parametri_causali[1];--'APE_ATT';
elsif cod_pdc_classe = 'PP' then
		cod_pdc_causale = parametri_causali[2];-- 'APE_PASS';
elsif cod_pdc_classe = 'CE' then
		cod_pdc_causale = parametri_causali[3];--'APE_CE'; 
elsif cod_pdc_classe = 'RE' then
		cod_pdc_causale = parametri_causali[4]; --'APE_RE';    
else cod_pdc_causale = '';
end if;     

raise notice 'Codice causale = %', cod_pdc_causale;

importo_ini_dare:=0;
importo_ini_avere:=0;

--cerco gli importi DARE/AVERE della prima nota DEFINITIVA della classe impostata prima
--legata al conto in input.
select COALESCE(sum(importi_dare.importi_dare),0), COALESCE(sum(importi_avere.importo_avere),0)
	into importo_ini_dare, importo_ini_avere
from accrual_t_causale cau,
	accrual_t_prima_nota pn
    	left join (select sum(pnimp.prima_nota_importo_importo) importo_avere, pnimp.prima_nota_id
                      from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
                      where pdc.pdc_id=pnimp.pdc_id
                          and pnimp.ente_id = p_ente_id
                          and pnimp.prima_nota_importo_segno='AVERE' 
                          and pdc.pdc_id = pdce_conto_id_in                         
                          and pnimp.gest_data_del is null
                          and pdc.gest_data_del is null
                      group by pnimp.prima_nota_id) importi_avere
        	on pn.prima_nota_id = importi_avere.prima_nota_id
        left join (select sum(pnimp.prima_nota_importo_importo) importi_dare, pnimp.prima_nota_id
                    from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
                    where pdc.pdc_id=pnimp.pdc_id
                          and pnimp.ente_id = p_ente_id
                          and pnimp.prima_nota_importo_segno='DARE' 
                          and pdc.pdc_id = pdce_conto_id_in  
                          and pnimp.gest_data_del is null
                          and pdc.gest_data_del is null
                    group by pnimp.prima_nota_id) importi_dare  
        	on pn.prima_nota_id = importi_dare.prima_nota_id,
    accrual_r_prima_nota_stato rstato, 
    accrual_d_prima_nota_stato dstato  
where cau.causale_id=pn.causale_id
    and rstato.prima_nota_id=pn.prima_nota_id
	and dstato.prima_nota_stato_id=rstato.prima_nota_stato_id
	and pn.ente_id=p_ente_id
    and pn.prima_nota_anno = p_anno_bilancio::integer
    and dstato.prima_nota_stato_codice = 'D'
    and cau.causale_codice = cod_pdc_causale
    and pn.gest_data_del is null
    and rstato.gest_data_del is null
    and rstato.fine_validita is null
	and dstato.gest_data_del is null;

raise notice 'importo_ini_dare = % - importo_ini_avere = %', importo_ini_dare, importo_ini_avere;

--estrazione dei dati.
return query 
--my_tree_unico serve per raggruppare in un'unica riga i dati dell'albero dei conti.
--Viene restituito anche l'id del conto in input (pdc_id_foglia) in modo che tutti i dati delle
--prime note di quel conto siano agganciati al record creato con my_tree_unico.
--In pratica per ogni prima nota avremo sullo stesso record i dati dell'albero dei conti e questo
--facilita la gestione della presentazione dei dati dell'albero nel report.
with my_tree_unico as(
  WITH RECURSIVE my_tree AS (
    -- Seed
    SELECT 
      a.pdc_id,
      a.pdc_id_padre,
      a.pdc_codice_ufficiale,
      a.pdc_codice,
      a.pdc_descrizione,
      a.pdc_livello,
      1 AS livellotree
    FROM accrual_t_pdc a
    WHERE a.ente_id = p_ente_id
    	and a.pdc_id = pdce_conto_id_in
        and a.gest_data_del is null
    UNION ALL
    -- Recursive Term
    SELECT 
      b.pdc_id,
      b.pdc_id_padre,
      b.pdc_codice_ufficiale,
      b.pdc_codice,
      b.pdc_descrizione,
      b.pdc_livello,
      t.livellotree + 1
    FROM my_tree t
    INNER JOIN accrual_t_pdc b
      ON t.pdc_id_padre = b.pdc_id
  )
  SELECT
    MAX(CASE WHEN pdc_livello = 1 THEN pdc_id END) AS pdc_id1,
    MAX(CASE WHEN pdc_livello = 1 THEN pdc_codice_ufficiale END) AS codice_pdc1,
    MAX(CASE WHEN pdc_livello = 1 THEN pdc_descrizione END) AS descr_pdc1,
    MAX(CASE WHEN pdc_livello = 2 THEN pdc_id END) AS pdc_id2,
    MAX(CASE WHEN pdc_livello = 2 THEN pdc_codice_ufficiale END) AS codice_pdc2,
    MAX(CASE WHEN pdc_livello = 2 THEN pdc_descrizione END) AS descr_pdc2,
    MAX(CASE WHEN pdc_livello = 3 THEN pdc_id END) AS pdc_id3,
    MAX(CASE WHEN pdc_livello = 3 THEN pdc_codice_ufficiale END) AS codice_pdc3,
    MAX(CASE WHEN pdc_livello = 3 THEN pdc_descrizione END) AS descr_pdc3,
    MAX(CASE WHEN pdc_livello = 4 THEN pdc_id END) AS pdc_id4,
    MAX(CASE WHEN pdc_livello = 4 THEN pdc_codice_ufficiale END) AS codice_pdc4,
    MAX(CASE WHEN pdc_livello = 4 THEN pdc_descrizione END) AS descr_pdc4,
    MAX(CASE WHEN pdc_livello = 5 THEN pdc_id END) AS pdc_id5,
    MAX(CASE WHEN pdc_livello = 5 THEN pdc_codice_ufficiale END) AS codice_pdc5,
    MAX(CASE WHEN pdc_livello = 5 THEN pdc_descrizione END) AS descr_pdc5,
    MAX(CASE WHEN pdc_livello = 6 THEN pdc_id END) AS pdc_id6,
    MAX(CASE WHEN pdc_livello = 6 THEN pdc_codice_ufficiale END) AS codice_pdc6,
    MAX(CASE WHEN pdc_livello = 6 THEN pdc_descrizione END) AS descr_pdc6,
    MAX(CASE WHEN pdc_livello = 7 THEN pdc_id END) AS pdc_id7,
    MAX(CASE WHEN pdc_livello = 7 THEN pdc_codice_ufficiale END) AS codice_pdc7,
    MAX(CASE WHEN pdc_livello = 7 THEN pdc_descrizione END) AS descr_pdc7,
    MAX(CASE WHEN pdc_livello = 8 THEN pdc_id END) AS pdc_id8,
    MAX(CASE WHEN pdc_livello = 8 THEN pdc_codice_ufficiale END) AS codice_pdc8,
    MAX(CASE WHEN pdc_livello = 8 THEN pdc_descrizione END) AS descr_pdc8,
    MAX(CASE WHEN pdc_livello = 9 THEN pdc_id END) AS pdc_id9,
    MAX(CASE WHEN pdc_livello = 9 THEN pdc_codice_ufficiale END) AS codice_pdc9,
    MAX(CASE WHEN pdc_livello = 9 THEN pdc_descrizione END) AS descr_pdc9,
    MAX(CASE WHEN pdc_livello = 10 THEN pdc_id END) AS pdc_id10,
    MAX(CASE WHEN pdc_livello = 10 THEN pdc_codice_ufficiale END) AS codice_pdc10,
    MAX(CASE WHEN pdc_livello = 10 THEN pdc_descrizione END) AS descr_pdc10,
    max(pdc_livello) livello,
    pdce_conto_id_in  pdc_id_foglia
  FROM my_tree),
prime_note as(select pn.prima_nota_id, pn.prima_nota_numero, pn.prima_nota_data, pn.prima_nota_progressivogiornale,
  pn.prima_nota_dataregistrazionegiornale, fonte.fonte_codice, fonte.fonte_descrizione, 
  evento.evento_codice, evento.evento_descrizione, cau.causale_codice, cau.causale_descrizione,
  dstato.prima_nota_stato_codice, dstato.prima_nota_stato_descrizione, 
  tipopn.prima_nota_tipo_codice codice_tipo_prima_nota, 
  tipopn.prima_nota_tipo_descrizione desc_tipo_prima_nota,
  --gli attributi possono essere legati alla prima nota direttamente dalla tabella
  --accrual_r_prima_nota_attributo oppure tramite la tabella degli importi delle prima nota 
  --accrual_t_prima_nota_importo.
  --Per questo motivo devo fare una UNION.
  (select string_agg(attributo_descrizione||'-'||prima_nota_attributo_valore,', ' order by prima_nota_attributo_valore) 
 	from (select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
 		from accrual_d_attributo attr, 
            accrual_r_prima_nota_attributo rattr,
            accrual_t_prima_nota_importo imp
            left join accrual_t_pdc pdc 
                    on pdc.pdc_id = imp.pdc_id
          where attr.attributo_id=rattr.attributo_id
          and rattr.prima_nota_importo_id = imp.prima_nota_importo_id
          and rattr.prima_nota_id=pn.prima_nota_id 
          and rattr.ente_id = p_ente_id          
          and pdc.pdc_id = pdce_conto_id_in
          and rattr.gest_data_del is null
          and attr.gest_data_del is null
    union 
      select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
        from accrual_d_attributo attr, 
          accrual_r_prima_nota_attributo rattr,
          accrual_t_prima_nota_importo imp
          left join accrual_t_pdc pdc 
                  on pdc.pdc_id = imp.pdc_id
        where attr.attributo_id=rattr.attributo_id
            and imp.prima_nota_id=rattr.prima_nota_id
            and rattr.prima_nota_id=pn.prima_nota_id
            and rattr.ente_id = p_ente_id             
            and pdc.pdc_id = pdce_conto_id_in
            and rattr.gest_data_del is null
            and attr.gest_data_del is null
            and   rattr.prima_nota_importo_id IS NULL) tutto   ) elenco_attributi,
 pdce_conto_id_in pdc_id
from accrual_d_fonte fonte, accrual_t_evento evento, accrual_t_causale cau,
  accrual_t_prima_nota pn, accrual_d_prima_nota_tipo tipopn,
  accrual_r_prima_nota_stato rstato, accrual_d_prima_nota_stato dstato,
  accrual_r_causale_stato r_cau_stato, accrual_d_causale_stato d_cau_stato
where cau.evento_id=evento.evento_id
	and pn.causale_id=cau.causale_id
	and pn.fonte_id=fonte.fonte_id
	and pn.prima_nota_tipo_id=tipopn.prima_nota_tipo_id
	and rstato.prima_nota_id=pn.prima_nota_id
	and dstato.prima_nota_stato_id=rstato.prima_nota_stato_id
    and r_cau_stato.causale_id=cau.causale_id
    and r_cau_stato.causale_stato_id=d_cau_stato.causale_stato_id
    and pn.ente_id = p_ente_id
    and pn.prima_nota_anno = p_anno_bilancio::integer
    and dstato.prima_nota_stato_codice = 'D' --solo le prime note definitive.
    and date_trunc('day',pn.prima_nota_dataregistrazionegiornale) 
    	between p_data_da and p_data_a
        --devo escludere le prime note inziali, cioe' quelle che danno origine agli importi iniziali.
    and cau.causale_codice <> cod_pdc_causale
    and d_cau_stato.causale_stato_codice='V'
	and fonte.gest_data_del is null
	and evento.gest_data_del is null
	and cau.gest_data_del is null
	and pn.gest_data_del is null
	and tipopn.gest_data_del is null
	and rstato.gest_data_del is null
    and rstato.fine_validita is null
	and dstato.gest_data_del is null
    and r_cau_stato.fine_validita IS NULL
    and r_cau_stato.gest_data_del IS NULL
    	--verifico che sulle prime note esista almeno 1 importo valido per il conto in input.
    and exists (select 1  
    			from accrual_t_prima_nota_importo imp
                where imp.prima_nota_id=pn.prima_nota_id
                  and imp.ente_id = p_ente_id
                  and imp.pdc_id = pdce_conto_id_in 
                  and imp.gest_data_del is null)),
	importi_avere as (    				  
    	select sum(pnimp.prima_nota_importo_importo) importo_avere, pnimp.prima_nota_id
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
        where pdc.pdc_id=pnimp.pdc_id
            and pnimp.ente_id = p_ente_id
            and pnimp.prima_nota_importo_segno='AVERE'
            and pdc.pdc_id = pdce_conto_id_in 
            and pnimp.gest_data_del is null
            and pdc.gest_data_del is null
        group by pdc.pdc_codice, pdc.pdc_id, pnimp.prima_nota_id),
    importi_dare as (
    	select sum(pnimp.prima_nota_importo_importo) importo_dare, pnimp.prima_nota_id
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
        where pdc.pdc_id=pnimp.pdc_id
              and pnimp.ente_id = p_ente_id
              and pnimp.prima_nota_importo_segno='DARE'
              and pdc.pdc_id = pdce_conto_id_in 
              and pnimp.gest_data_del is null
              and pdc.gest_data_del is null
        group by pdc.pdc_codice, pdc.pdc_id, pnimp.prima_nota_id)    
select  COALESCE(my_tree_unico.pdc_id1,0)::integer pdc_id1,
	COALESCE(my_tree_unico.codice_pdc1,'')::varchar codice_pdc1,
    COALESCE(my_tree_unico.descr_pdc1,'')::varchar descr_pdc1,
	COALESCE(my_tree_unico.pdc_id2,0)::integer pdc_id2,
	COALESCE(my_tree_unico.codice_pdc2,'')::varchar codice_pdc2,
    COALESCE(my_tree_unico.descr_pdc2,'')::varchar descr_pdc2, 
    COALESCE(my_tree_unico.pdc_id3,0)::integer pdc_id3,
	COALESCE(my_tree_unico.codice_pdc3,'')::varchar codice_pdc3,
    COALESCE(my_tree_unico.descr_pdc3,'')::varchar descr_pdc3,   
    COALESCE(my_tree_unico.pdc_id4,0)::integer pdc_id1,
	COALESCE(my_tree_unico.codice_pdc4,'')::varchar codice_pdc1,
    COALESCE(my_tree_unico.descr_pdc4,'')::varchar descr_pdc4,
    COALESCE(my_tree_unico.pdc_id5,0)::integer pdc_id5,
	COALESCE(my_tree_unico.codice_pdc5,'')::varchar codice_pdc5,
    COALESCE(my_tree_unico.descr_pdc5,'')::varchar descr_pdc5,
    COALESCE(my_tree_unico.pdc_id6,0)::integer pdc_id6,
	COALESCE(my_tree_unico.codice_pdc6,'')::varchar codice_pdc6,
    COALESCE(my_tree_unico.descr_pdc6,'')::varchar descr_pdc6,
    COALESCE(my_tree_unico.pdc_id7,0)::integer pdc_id17,
	COALESCE(my_tree_unico.codice_pdc7,'')::varchar codice_pdc7,
    COALESCE(my_tree_unico.descr_pdc7,'')::varchar descr_pdc7,
    COALESCE(my_tree_unico.pdc_id8,0)::integer pdc_id8,
	COALESCE(my_tree_unico.codice_pdc8,'')::varchar codice_pdc8,
    COALESCE(my_tree_unico.descr_pdc8,'')::varchar descr_pdc8,
    COALESCE(my_tree_unico.pdc_id9,0)::integer pdc_id9,
	COALESCE(my_tree_unico.codice_pdc9,'')::varchar codice_pdc9,
    COALESCE(my_tree_unico.descr_pdc9,'')::varchar descr_pdc9,
    COALESCE(my_tree_unico.pdc_id10,0)::integer pdc_id1,
	COALESCE(my_tree_unico.codice_pdc10,'')::varchar codice_pdc10,
    COALESCE(my_tree_unico.descr_pdc10,'')::varchar descr_pdc10,
	my_tree_unico.livello::integer livello,     
    prime_note.prima_nota_id::integer prima_nota_id,
    prime_note.prima_nota_numero::integer prima_nota_numero,
    prime_note.prima_nota_data::date prima_nota_data,
    prime_note.prima_nota_progressivogiornale::integer prima_nota_progressivogiornale,
    prime_note.prima_nota_dataregistrazionegiornale::date prima_nota_dataregistrazionegiornale,
    prime_note.fonte_codice::varchar fonte_codice ,
    prime_note.fonte_descrizione::varchar fonte_descrizione,
    prime_note.evento_codice::varchar evento_codice,
    prime_note.evento_descrizione::varchar evento_descrizione,
    prime_note.causale_codice::varchar causale_codice,
    prime_note.causale_descrizione::varchar causale_descrizione,
    prime_note.prima_nota_stato_codice::varchar prima_nota_stato_codice,
    prime_note.prima_nota_stato_descrizione::varchar prima_nota_stato_descrizione,
    prime_note.codice_tipo_prima_nota::varchar codice_tipo_prima_nota,
    prime_note.desc_tipo_prima_nota::varchar desc_tipo_prima_nota,
    COALESCE(prime_note.elenco_attributi,'')::varchar elenco_attributi,
    COALESCE(importi_dare.importo_dare,0)::numeric importo_dare ,
    COALESCE(importi_avere.importo_avere,0)::numeric importo_avere,
    --Gli importi relativi al saldo iniziale sono impostati per tutti i record ma nel report
    --ne sara' preso solo 1 tramite FIRST invece che tramite SUM.
    importo_ini_dare::numeric importo_ini_dare,
    importo_ini_avere::numeric importo_ini_avere,	
    ''::varchar display_error
from my_tree_unico
	left join prime_note
    	on prime_note.pdc_id = my_tree_unico.pdc_id_foglia
    left join importi_avere
    	on importi_avere.prima_nota_id=prime_note.prima_nota_id
	left join importi_dare
    	on importi_dare.prima_nota_id=prime_note.prima_nota_id 
--where prime_note.prima_nota_id IS NOT NULL         
order by livello;



EXCEPTION
when no_data_found THEN
    raise notice 'Nessun dato trovato' ;
    display_error='Non ci sono prime note per il conto '||p_conto_pdc|| ' nel periodo indicato';
    return next;

END;
$function$
;

CREATE OR REPLACE FUNCTION accrual.accrual_rep002_stampa_libro_giornale(p_ente_id integer, p_anno_bilancio character varying, p_code_tipo character varying, p_code_fonte character varying, p_code_evento character varying, p_code_causale character varying, p_numero_provvisorio integer, p_numero_definitivo integer, p_code_stato character varying, p_conto_pdc character varying, p_data_da_provv date, p_data_a_provv date, p_data_da_def date, p_data_a_def date)
 RETURNS TABLE(pdc_id integer, codice_pdc character varying, descr_pdc character varying, livello integer, prima_nota_id integer, numero_riga integer, prima_nota_numero integer, prima_nota_data date, prima_nota_progressivogiornale integer, prima_nota_dataregistrazionegiornale date, prima_nota_desc character varying, fonte_codice character varying, fonte_descrizione character varying, evento_codice character varying, evento_descrizione character varying, causale_codice character varying, causale_descrizione character varying, prima_nota_stato_codice character varying, prima_nota_stato_descrizione character varying, codice_tipo_prima_nota character varying, desc_tipo_prima_nota character varying, elenco_attributi character varying, importo_dare numeric, importo_avere numeric, display_error character varying)
 LANGUAGE plpgsql
AS $function$
DECLARE
pdce_conto_id_in integer;
strQuery varchar;
strQueryParam varchar;
strQueryParamAttributi varchar;

BEGIN

/*
	27/02/2026.
    Versione 1 della procedura che estrae i dati del libro contabile per ACCRUAL.
    
    Parametri in input:
    - p_ente_id; OBBLIGATORIO; id dell'ente;
    - p_anno_bilancio; OBBLIGATORIO; anno del bilancio;
  	- p_code_tipo; OBBLIGATORIO; tipologia della prima nota; valori possibili:
    	- T; Tutte; nessun filtro sullo tipologia;
        - D: AUTOMATICA; 
        - P: MANUALE.
  	- p_code_fonte; OBBLIGATORIO; fonte; valori possibili:
    	- T; Tutte; nessun filtro sulla fonte;
        - A; Sistema Accrual;
        - C; Sitema Contabile;
        - I; Sistema Inventariale;        
  	- p_code_evento OBBLIGATORIO; eventi configurati dipendenti dalla scelta fatta per p_code_fonte; 
    		valori possibili:
    	- T; Tutti; nessun filtro sugli eventi;
        - tutti i possibili codici presenti nella tabella  accrual_t_evento;
  	- p_code_causale; OBBLIGATORIO; causali configurate dipendenti dalla scelta fatta per p_code_evento;
      		valori possibili:
    	- T; Tutte; nessun filtro sulle causali;
        - tutti i possibili codici presenti nella tabella accrual_t_causale;
  	- p_numero_provvisorio; FACOLTATIVO; numero provvisorio della prima nota;
  	- p_numero_definitivo FACOLTATIVO; numero definitivo della prima nota;
  	- p_code_stato OBBLIGATORIO; stato della prima nota; valori possibili:
    	- T; Tutti; tutte le prime note in stato NON ANNULLATO;
        - D; Definitiva;
        - P; Provvisorio;
  	- p_conto_pdc; FACOLTATIVO; codice del conto;
  	- p_data_da_provv; FACOLTATIVO; data provvisoria a partire dalla quale cercare le prime note (>=);
  	- p_data_a_provv; FACOLTATIVO; data provvisoria fino alla quale cercare le prime note (<=);
  	- p_data_da_def; FACOLTATIVO; data definitiva a partire dalla quale cercare le prime note (>=);
  	- p_data_a_def; FACOLTATIVO; data definitiva fino alla quale cercare le prime note (<=).
    
*/

pdce_conto_id_in:=0;

--se il conto e' stato specificato cerco l'id del conto in input.
if p_conto_pdc IS NOT NULL and p_conto_pdc <> '' then
  select pdc.pdc_id
      into pdce_conto_id_in
  from accrual_t_pdc pdc, accrual_d_pdc_classe pdc_classe
  where pdc_classe.pdc_classe_id=pdc.pdc_classe_id
  and pdc.ente_id = p_ente_id
  and pdc.pdc_codice_ufficiale = p_conto_pdc
  and pdc.fine_validita IS NULL
  and pdc.gest_data_del IS NULL
  and pdc_classe.fine_validita IS NULL
  and pdc_classe.gest_data_del IS NULL;
  
    --se non esiste restituisco un errore al report.
  IF NOT FOUND THEN
      display_error='Il codice PDC indicato ('||p_conto_pdc||') e'' inesistente';
      return next;
      return;
  END IF;
  
end if;


    
raise notice 'pdce_conto_id_in di % = %', p_conto_pdc,pdce_conto_id_in;

--controllo delle date se valorizzate.
if p_data_da_provv is not NULL and p_data_a_provv IS NOT NULL then
	if p_data_da_provv > p_data_a_provv then
      display_error='La data da provvisoria ('||to_char(p_data_da_provv,'dd/MM/yyyy') ||') non puo'' essere maggiore della data a ('||to_char(p_data_a_provv,'dd/MM/yyyy')||')';
      return next;
      return;
	END IF;	
end if;
    
if p_data_da_def is not NULL and p_data_a_def IS NOT NULL then
	if p_data_da_def > p_data_a_def then
      display_error='La data da definitiva ('||to_char(p_data_da_def,'dd/MM/yyyy') ||') non puo'' essere maggiore della data a ('||to_char(p_data_a_def,'dd/MM/yyyy')||')';
      return next;
      return;
	END IF;	
end if;

strQueryParam:='';
strQueryParamAttributi:='';

-- Visto che i parametri sono tanti, e' piu' comodo costrutire una query dinamica caricando 
--una stringa che conterra' la query completa da eseguire.

--costruisco la stringa dei parametri per il filtro della query in base a quali parametri sono stati 
--passati alla procedura.
--la stringa "E'\n" serve per mettere un ritorno a capo prima di ogni riga in modo da rendere
--piu' leggibile la query e non avere tutti i parametri in una sola riga.
if p_code_tipo <> 'T' THEN
	strQueryParam:= E'\n'||' and tipopn.prima_nota_tipo_codice ='''||p_code_tipo||'''';
end if;
if p_code_fonte <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and fonte.fonte_codicee ='''||p_code_fonte||'''';
end if;
if p_code_tipo <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and evento.evento_codice ='''||p_code_tipo||'''';
end if;
if p_code_causale <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and cau.causale_codice ='''||p_code_causale||'''';
end if;
if p_numero_provvisorio IS NOT NULL AND p_numero_provvisorio <> 0 THEN
	strQueryParam:= strQueryParam ||E'\n'||' and pn.prima_nota_numero ='||p_numero_provvisorio;
end if;
if p_numero_definitivo IS NOT NULL AND p_numero_definitivo <> 0 THEN
	strQueryParam:= strQueryParam ||E'\n'||' and pn.prima_nota_progressivogiornale ='||p_numero_definitivo;
end if;
if p_code_stato <> 'T' THEN
	strQueryParam:= strQueryParam ||E'\n'||' and dstato.prima_nota_stato_codice ='''||p_code_stato||'''';
else     
	strQueryParam:= strQueryParam ||E'\n'||' and dstato.prima_nota_stato_codice <> ''A''';
end if;
if p_data_da_def IS NOT NULL  THEN
	strQueryParam:= strQueryParam ||E'\n'||' and date_trunc(''day'',pn.prima_nota_dataregistrazionegiornale) >='''||p_data_da_def||'''';
end if;
if p_data_a_def IS NOT NULL  THEN
	strQueryParam:= strQueryParam ||E'\n'||' and date_trunc(''day'',pn.prima_nota_dataregistrazionegiornale) <='''||p_data_a_def||'''';
end if;
if p_data_da_provv IS NOT NULL  THEN
	strQueryParam:= strQueryParam ||E'\n'||' and date_trunc(''day'',pn.prima_nota_data) >='''||p_data_da_provv||'''';
end if;
if p_data_a_provv IS NOT NULL  THEN
	strQueryParam:= strQueryParam ||E'\n'||' and date_trunc(''day'',pn.prima_nota_data) <='''||p_data_a_provv||'''';
end if;
if pdce_conto_id_in <> 0 THEN
	--se e' stato specificato il conto devo SOLO estrarre le prime note che hanno quel conto
    --pero' NON devo mettere il filtro su quel conto perche' la prima nota deve essere
    --estratta completa con tutti i suoi conti.
	strQueryParam:= strQueryParam ||E'\n'||'
	--verifico che sulle prime note esista almeno 1 importo valido per il conto in input.
    and exists (select 1  
    			from accrual_t_prima_nota_importo imp
                where imp.prima_nota_id=pn.prima_nota_id
                  and imp.ente_id ='|| p_ente_id||E'\n'
                  ||'and imp.pdc_id ='|| pdce_conto_id_in ||
                  ' and imp.gest_data_del is null)';
                  
   	--filtro sul conto per i paramentri.
   strQueryParamAttributi:= E'\n'||' and pdc.pdc_id =' ||pdce_conto_id_in;
else 
   strQueryParamAttributi:='';
end if;

                  
raise notice 'Parametri = % ', strQueryParam;

               
strQuery:='';

--costruisco la query aggiungendo anche la strQueryParam con i parametri per il filtro.
strQuery:='with prime_note as(select pn.prima_nota_id, pn.prima_nota_numero, pn.prima_nota_data, 
  pn.prima_nota_progressivogiornale, pn.prima_nota_dataregistrazionegiornale, pn.prima_nota_descrizione,
  fonte.fonte_codice, fonte.fonte_descrizione, 
  evento.evento_codice, evento.evento_descrizione, cau.causale_codice, cau.causale_descrizione,
  dstato.prima_nota_stato_codice, dstato.prima_nota_stato_descrizione, 
  tipopn.prima_nota_tipo_codice codice_tipo_prima_nota, 
  tipopn.prima_nota_tipo_descrizione desc_tipo_prima_nota,
  --gli attributi possono essere legati alla prima nota direttamente dalla tabella
  --accrual_r_prima_nota_attributo oppure tramite la tabella degli importi delle prima nota 
  --accrual_t_prima_nota_importo.
  --Per questo motivo devo fare una UNION.
  (select string_agg(attributo_descrizione||''-''||prima_nota_attributo_valore,'', '' order by prima_nota_attributo_valore) 
 	from (select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
 		from accrual_d_attributo attr, 
            accrual_r_prima_nota_attributo rattr,
            accrual_t_prima_nota_importo imp
            left join accrual_t_pdc pdc 
                    on pdc.pdc_id = imp.pdc_id
          where attr.attributo_id=rattr.attributo_id
          and rattr.prima_nota_importo_id = imp.prima_nota_importo_id
          and rattr.prima_nota_id=pn.prima_nota_id 
          and rattr.ente_id ='|| p_ente_id ||strQueryParamAttributi||'                   
          and rattr.gest_data_del is null
          and attr.gest_data_del is null
    union 
      select attr.attributo_descrizione,rattr.prima_nota_attributo_valore 
        from accrual_d_attributo attr, 
          accrual_r_prima_nota_attributo rattr,
          accrual_t_prima_nota_importo imp
          left join accrual_t_pdc pdc 
                  on pdc.pdc_id = imp.pdc_id
        where attr.attributo_id=rattr.attributo_id
            and imp.prima_nota_id=rattr.prima_nota_id
            and rattr.prima_nota_id=pn.prima_nota_id
            and rattr.ente_id = '|| p_ente_id ||'             
            --and pdc.pdc_id = pdce_conto_id_in
            and rattr.gest_data_del is null
            and attr.gest_data_del is null
            and   rattr.prima_nota_importo_id IS NULL) tutto   ) elenco_attributi            
from accrual_d_fonte fonte, accrual_t_evento evento, accrual_t_causale cau,
accrual_t_prima_nota pn, accrual_d_prima_nota_tipo tipopn,
accrual_r_prima_nota_stato rstato, accrual_d_prima_nota_stato dstato,
accrual_r_causale_stato r_cau_stato, accrual_d_causale_stato d_cau_stato
where cau.evento_id=evento.evento_id
	and pn.causale_id=cau.causale_id
	and pn.fonte_id=fonte.fonte_id
	and pn.prima_nota_tipo_id=tipopn.prima_nota_tipo_id
	and rstato.prima_nota_id=pn.prima_nota_id
	and dstato.prima_nota_stato_id=rstato.prima_nota_stato_id
    and r_cau_stato.causale_id=cau.causale_id
    and r_cau_stato.causale_stato_id=d_cau_stato.causale_stato_id
    and pn.ente_id = '|| p_ente_id ||'
    and pn.prima_nota_anno ='''|| p_anno_bilancio||'''::integer '||strQueryParam||'   
	and d_cau_stato.causale_stato_codice=''V''
    and fonte.gest_data_del is null
	and evento.gest_data_del is null
	and cau.gest_data_del is null
	and pn.gest_data_del is null
	and tipopn.gest_data_del is null
	and rstato.gest_data_del is null
    and rstato.fine_validita is null
	and dstato.gest_data_del is null
    and r_cau_stato.fine_validita IS NULL
    and r_cau_stato.gest_data_del IS NULL),
importi as (    				  
    	select pnimp.prima_nota_id, pdc.pdc_id, pdc.pdc_codice_ufficiale, pdc.pdc_descrizione,
        	pdc.pdc_livello livello, pnimp.prima_nota_importo_segno segno, 
            --sum(pnimp.prima_nota_importo_importo) importo  
        	pnimp.prima_nota_importo_importo importo       	
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc
        where pdc.pdc_id=pnimp.pdc_id
            and pnimp.ente_id = '|| p_ente_id ||'               
            and pnimp.gest_data_del is null
            and pdc.gest_data_del is null
        order by pnimp.prima_nota_id, pdc.pdc_codice_ufficiale)
        --group by pnimp.prima_nota_id, pdc.pdc_id, --pdc.pdc_codice_ufficiale, 
        		--pdc.pdc_descrizione, 
         --       pdc.pdc_livello, pnimp.prima_nota_importo_segno)           
select 
	importi.pdc_id::integer pdc_id, 
    importi.pdc_codice_ufficiale::varchar codice_pdc, 
    importi.pdc_descrizione::varchar descr_pdc,
  	importi.livello::integer livello,
    prime_note.prima_nota_id::integer,
    ((ROW_NUMBER() OVER(PARTITION BY prime_note.prima_nota_id))-1)::integer numero_riga,
    prime_note.prima_nota_numero::integer prima_nota_numero,
    prime_note.prima_nota_data::date prima_nota_data,
    COALESCE(prime_note.prima_nota_progressivogiornale,0)::integer prima_nota_progressivogiornale,    
    prime_note.prima_nota_dataregistrazionegiornale::date prima_nota_dataregistrazionegiornale,
    prime_note.prima_nota_descrizione::varchar prima_nota_desc,
    prime_note.fonte_codice::varchar fonte_codice,
    prime_note.fonte_descrizione::varchar fonte_descrizione,
    prime_note.evento_codice::varchar evento_codice,
    prime_note.evento_descrizione::varchar evento_descrizione,
    prime_note.causale_codice::varchar causale_codice,
    prime_note.causale_descrizione::varchar causale_descrizione,
    prime_note.prima_nota_stato_codice::varchar prima_nota_stato_codice,
    prime_note.prima_nota_stato_descrizione::varchar prima_nota_stato_descrizione,
    prime_note.codice_tipo_prima_nota::varchar codice_tipo_prima_nota,
    prime_note.desc_tipo_prima_nota::varchar desc_tipo_prima_nota,
    COALESCE(prime_note.elenco_attributi,'''')::varchar elenco_attributi,
    case when importi.segno = ''DARE'' then COALESCE(importi.importo,0)::numeric
    	else 0::numeric end importo_dare,
    case when importi.segno = ''AVERE'' then COALESCE(importi.importo,0)::numeric
    	else 0::numeric end importo_avere,
    ''''::varchar display_error
from prime_note
	left join importi 
    	on importi.prima_nota_id=prime_note.prima_nota_id';
    	

raise notice 'strQuery = %', strQuery;

return query execute strQuery;


EXCEPTION
when no_data_found THEN
    raise notice 'Nessun dato trovato' ;
    display_error='Non ci sono prime note per i parametri specificati';
    return next;

END;
$function$
;

CREATE OR REPLACE FUNCTION accrual.accrual_rep003_stampa_bilancio_verifica(p_ente_id integer, p_anno_bilancio character varying, p_tipo_stampa character varying, p_stato_pn character varying)
 RETURNS TABLE(tipo_report character varying, tipo_conto_patrimoniale character varying, classe_conto character varying, pdce_id integer, pdce_id_padre integer, pdce_code character varying, pdce_desc character varying, pdce_livello integer, livello_mostrato character varying, pdc_collegamento_bil_itas character varying, importo_dare numeric, importo_avere numeric, livello_importo integer, prima_nota_id integer, tipo_prima_nota character varying, stato_prima_nota character varying, ordinamento character varying, ordinamento_num integer, display_error character varying)
 LANGUAGE plpgsql
AS $function$
DECLARE


BEGIN

/*
	06/03/2026.
    Versione 1 della procedura che estrae i dati del bilancio di verifica per ACCRUAL.
    
    Parametri in input:
    - p_ente_id; OBBLIGATORIO; id dell'ente;
    - p_anno_bilancio; OBBLIGATORIO; anno del bilancio;
    - p_tipo_stampa; OBBLIGATORIO; tipo di stampa; valori possibili:
    	- CE; stampa report dei Conti Economici;
        - CP; stampa report dei Conti Patrimoniali.
    - p_stato_pn; OBBLIGATORIO; stato della prima Nota; valori possibili:
    	- T; Tutti; nessun filtro sullo stato (sono escluse le annullate);
        - D: Definitiva; stampa solo dei dati delle prime note in stato definitivo;
        - P: Provvisorio; stampa solo dei dati delle prime note in stato Provvisorio.
        
    Per la stampa Conti Patrimoniali a procedura estrae i dati dei conti con classe:
    	- classe AP = ATTIVO CONTI PATRIMONIALI
		- classe PP = PASSIVO CONTI PATRIMONIALI.
    Per la stampa Conti Economici a procedura estrae i dati dei conti con classe:
    	- classe CE =  CONTI ECONOMICI
		- classe RE =  CONTI ECONOMICI

    Per entrambe le tipologie di report sono estratti i dati dei conti che appartengono alle famiglie:
    	- ACCRUAL-A; 
        - ACCRUAL-B;
        - ACCRUAL-UTENTE.
        
    Gli importi devono essere calcolati nel report secondo la seguente regola:
    	- Conti Patrimoniali:
        	- CONTI DI TIPO AP = DARE - AVERE;
            - CONTI DI TIPO PP = AVERE- DARE.
        - Conti Economici:
        	- CONTI DI TIPO CE = DARE -AVERE;
			- CONTI DI TIPO RE = AVERE - DARE.

*/


return query
	--estraggo in modo ricorsivo i dati dei conti
WITH RECURSIVE albero AS (
    -- RADICI
    SELECT
        pdc.pdc_id,
        pdc.pdc_codice_ufficiale,
        pdc.pdc_descrizione,
        pdc.pdc_id_padre,
        pdc.pdc_livello,
        classe.pdc_classe_codice,
        pdc.pdc_collegamento_bil_itas,
        	--e' necessario trattare pdc_codice_ufficiale in un array di numeri interi per
            --permettere l'ordinamento corretto.
        string_to_array(pdc.pdc_codice_ufficiale,'.')::int[] AS path
    FROM accrual_t_pdc pdc
    JOIN accrual_t_pdc_famiglia fam
        ON fam.pdc_famiglia_id = pdc.pdc_famiglia_id
    JOIN accrual_d_pdc_classe classe
        ON classe.pdc_classe_id = pdc.pdc_classe_id
    WHERE pdc.ente_id = p_ente_id
      AND pdc.pdc_livello = 1
      AND fam.pdc_famiglia_codice IN ('ACCRUAL-A','ACCRUAL-B','ACCRUAL-UTENTE')
      AND pdc.gest_data_del IS NULL
      AND fam.gest_data_del IS NULL
      AND classe.gest_data_del IS NULL
    UNION ALL
    -- FIGLI
    SELECT
        figli.pdc_id,
        figli.pdc_codice_ufficiale,
        figli.pdc_descrizione,
        figli.pdc_id_padre,
        figli.pdc_livello,
        classe.pdc_classe_codice,
        figli.pdc_collegamento_bil_itas,
        alb.path || string_to_array(figli.pdc_codice_ufficiale,'.')::int[]
    FROM accrual_t_pdc figli
    JOIN albero alb
        ON figli.pdc_id_padre = alb.pdc_id
    JOIN accrual_d_pdc_classe classe
        ON classe.pdc_classe_id = figli.pdc_classe_id
),
	--dati delle prime note e degli importi.
importi as (    				  
    	select pnimp.prima_nota_id, pdc.pdc_id, pdc.pdc_livello livello,
        	pnimp.prima_nota_importo_segno, pn_tipo.prima_nota_tipo_codice tipo_prima_nota,
            dstato.prima_nota_stato_codice,
            sum(pnimp.prima_nota_importo_importo) importo         	
        from accrual_t_prima_nota_importo pnimp, accrual_t_pdc pdc,
        	accrual_t_prima_nota pn, accrual_d_prima_nota_tipo pn_tipo,
            accrual_r_prima_nota_stato rstato, 
    		accrual_d_prima_nota_stato dstato
        where pdc.pdc_id=pnimp.pdc_id
        	and pn.prima_nota_id=pnimp.prima_nota_id
            and pn.prima_nota_tipo_id=pn_tipo.prima_nota_tipo_id
            and rstato.prima_nota_id=pn.prima_nota_id
            and rstato.prima_nota_stato_id=dstato.prima_nota_stato_id
            and pnimp.ente_id =  p_ente_id  
            and pn.prima_nota_anno = p_anno_bilancio::integer
            and ((p_stato_pn = 'T' and dstato.prima_nota_stato_codice <> 'A') OR
       			 (p_stato_pn <> 'T' and dstato.prima_nota_stato_codice = p_stato_pn))                    
            and pnimp.gest_data_del is null
            and pdc.gest_data_del is null
            and pn.gest_data_del is null 
            and rstato.gest_data_del is null
  			and rstato.fine_validita is null           
        group by pnimp.prima_nota_id, pdc.pdc_id, pdc.pdc_livello, dstato.prima_nota_stato_codice,
        	pn_tipo.prima_nota_tipo_codice, pnimp.prima_nota_importo_segno)    
SELECT case when albero.pdc_classe_codice in('AP','PP') 
    then 'CONTI_PATRIMONIALI'::varchar
    else 'CONTI_ECONOMICI'::varchar end tipo_report,
  case when albero.pdc_classe_codice in('AP')
      then 'ATTIVO'::varchar 
      else case when albero.pdc_classe_codice in('PP')
          then 'PASSIVO'::varchar 
              --nel caso dei CONTI_ECONOMICI il tipo_conto_patrimoniale non e' significativo.
        else ''::varchar end end tipo_conto_patrimoniale,            	
  albero.pdc_classe_codice::varchar classe_conto,
  albero.pdc_id::integer pdce_id,
  albero.pdc_id_padre::integer pdce_id_padre,
  albero.pdc_codice_ufficiale::varchar pdce_code,
  albero.pdc_descrizione::VARCHAR pdce_desc,
  albero.pdc_livello::integer pdce_livello,  
  CASE albero.pdc_livello
        WHEN 1 THEN 'I'::varchar
        WHEN 2 THEN 'II'::varchar
        WHEN 3 THEN 'III'::varchar
        WHEN 4 THEN 'IV'::varchar
        WHEN 5 THEN 'V'::varchar
        WHEN 6 THEN 'VI'::varchar
        WHEN 7 THEN 'VII'::varchar
        WHEN 8 THEN 'VIII'::varchar
        WHEN 9 THEN 'IX'::varchar
        WHEN 10 THEN 'X'::varchar end livello_mostrato,
  COALESCE(albero.pdc_collegamento_bil_itas,'')::varchar pdc_collegamento_bil_itas,
  case when importi.prima_nota_importo_segno ='DARE'
  	then COALESCE(importi.importo,0)::numeric
    else 0::numeric end importo_dare,
  case when importi.prima_nota_importo_segno ='AVERE'
  	then COALESCE(importi.importo,0)::numeric 
    else 0::numeric end importo_avere,
  COALESCE(importi.livello,0)::integer livello_importo,
  COALESCE(importi.prima_nota_id,0)::integer prima_nota_id,
  COALESCE(importi.tipo_prima_nota,'')::varchar tipo_prima_nota,
  COALESCE(importi.prima_nota_stato_codice,'')::varchar stato_prima_nota,
  albero.path::varchar ordinamento, 
  	--ordinamento_num serve per permettere l'ordinamento corretto nel report.
  ROW_NUMBER() OVER (order by albero.path)::integer ordinamento_num,
  ''::varchar display_error          
from albero
	left join importi
    	on importi.pdc_id = albero.pdc_id
where --se e' richiesta la stampa dei Conti Economici (CE) estraggo solo le classi 'CE','RE'
	  --se e' richiesta la stampa dei Conti Patrimoniali (CP) estraggo solo le classi 'AP','PP'
	((p_tipo_stampa = 'CE' and albero.pdc_classe_codice in('CE','RE')) OR
	 (p_tipo_stampa = 'CP' and albero.pdc_classe_codice in('AP','PP')))
ORDER BY albero.path; 
          
EXCEPTION
when no_data_found THEN
    raise notice 'Nessun dato trovato' ;
    display_error='Non ci sono prime note per i parametri specificati';
    return next;

END;
$function$
;

ALTER SEQUENCE accrual.accrual_r_etichetta_prima_nota_etichetta_prima_nota_r_id_seq
      INCREMENT BY 50;
 
  ALTER SEQUENCE accrual.accrual_r_prima_nota_attributo_prima_nota_attributo_r_id_seq
      INCREMENT BY 50;
 
  ALTER SEQUENCE accrual.accrual_t_etichetta_etichetta_id_seq
      INCREMENT BY 50;
-- Permissions
--ALTER FUNCTION accrual.accrual_rep001_stampa_mastrino(int4, varchar, date, date, varchar) OWNER TO accrual;
--GRANT ALL ON FUNCTION accrual.accrual_rep001_stampa_mastrino(int4, varchar, date, date, varchar) TO accrual;
--ALTER FUNCTION accrual.accrual_rep002_stampa_libro_giornale(int4, varchar, varchar, varchar, varchar, varchar, int4, int4, varchar, varchar, date, date, date, date) OWNER TO accrual;
--GRANT ALL ON FUNCTION accrual.accrual_rep002_stampa_libro_giornale(int4, varchar, varchar, varchar, varchar, varchar, int4, int4, varchar, varchar, date, date, date, date) TO accrual;
--ALTER FUNCTION accrual.accrual_rep003_stampa_bilancio_verifica(int4, varchar, varchar, varchar) OWNER TO accrual;
--GRANT ALL ON FUNCTION accrual.accrual_rep003_stampa_bilancio_verifica(int4, varchar, varchar, varchar) TO accrual;

GRANT TRIGGER, REFERENCES, DELETE, TRUNCATE, UPDATE, INSERT, SELECT ON ALL TABLES IN SCHEMA accrual TO accrual_rw;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA accrual TO accrual_rw;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA accrual TO accrual_rw;