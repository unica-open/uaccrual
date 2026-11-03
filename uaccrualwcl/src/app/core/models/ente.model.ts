/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Modello per un Ente (organizzazione) nel sistema multi-ente.
 *
 * @see AuthContextService per la gestione del contesto ente
 */
export interface Ente {
  /** Codice identificativo interno */
  codice: string;

  /** Codice fiscale univoco dell'ente */
  codiceFiscale: string;

  /** Denominazione/descrizione dell'ente */
  denominazione?: string;

  /** Descrizione dell'ente (alias di denominazione) */
  descrizione?: string;

  /** Indica se l'ente è attivo */
  attivo?: boolean;
}

/**
 * Modello per le informazioni utente complete.
 * Estende le informazioni base con dati specifici del dominio.
 */
export interface UserInfoComplete {
  /** Codice fiscale dell'utente */
  codiceFiscale: string;

  /** Nome completo dell'utente */
  nome: string;

  /** Email dell'utente */
  email?: string;

  /** Lista degli enti a cui l'utente ha accesso */
  enti: Ente[];

  /** Ruolo dell'utente */
  ruolo?: string;

  /** Permessi dell'utente */
  permissions: string[];

  /** Provider di autenticazione usato */
  source?: string;
}
