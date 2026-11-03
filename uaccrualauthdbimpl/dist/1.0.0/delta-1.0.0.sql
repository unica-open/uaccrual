--
-- SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
-- SPDX-License-Identifier: EUPL-1.2
--

-- DROP vincolo UNIQUE
ALTER TABLE accrualauth.accrualauth_t_utente
DROP CONSTRAINT IF EXISTS accrualauth_t_utente_utente_codice_fiscale_key;

-- CREATE INDEX (solo se non esiste)
CREATE INDEX IF NOT EXISTS idx_utente_cf_ente
ON accrualauth.accrualauth_t_utente (utente_codice_fiscale, ente_id);