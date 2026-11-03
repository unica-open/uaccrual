/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { PrimaNotaSearchFilter } from '../models/primanota.model';

/**
 * Triggers the search query based on the provided filters.
 */
export class SearchPrimeNote {
  static readonly type = '[PrimaNota] Search';
  constructor(public filter: Partial<PrimaNotaSearchFilter>) {}
}

/**
 * Handles pagination changes for the search results.
 */
export class ChangePage {
  static readonly type = '[PrimaNota] Change Page';
  constructor(public page: number, public size: number) {}
}

/**
 * Fetches the details of a single 'prima nota' by its anno and numero.
 */
export class GetPrimaNotaDetail {
  static readonly type = '[PrimaNota] Get Detail';
  constructor(public anno: number, public numero: number) {}
}

/**
 * Checks for the presence of incomplete 'prime note'.
 * This is typically dispatched when the search view is initialized.
 */
export class CheckIncomplete {
  static readonly type = '[PrimaNota] Check Incomplete';
}

/**
 * Updates the incomplete warning status after checking for incomplete 'prime note'.
 */
export class CheckIncompleteSuccess {
  static readonly type = '[PrimaNota] Check Incomplete Success';
  constructor(public hasIncomplete: boolean) {}
}

/**
 * Resets the search filters, results, and related state to its initial values.
 */
export class ClearSearch {
  static readonly type = '[PrimaNota] Clear Search';
}

/**
 * An event action dispatched when an API call for searching fails.
 */
export class SearchFailed {
  static readonly type = '[PrimaNota] Search Failed';
  constructor(public error: any) {}
}

/**
 * Action per annullare una prima nota
 */
export class AnnullaPrimaNota {
  static readonly type = '[PrimaNota] Annulla';
  constructor(public anno: number, public numero: number) {}
}

/**
 * Action per il successo dell'annullamento
 */
export class AnnullaPrimaNotaSuccess {
  static readonly type = '[PrimaNota] Annulla Success';
  constructor(public messaggio: string) {}
}

/**
 * Action per il fallimento dell'annullamento
 */
export class AnnullaPrimaNotaFailed {
  static readonly type = '[PrimaNota] Annulla Failed';
  constructor(public error: any) {}
}

/**
 * Action per riaprire una prima nota
 */
export class RiapriPrimaNota {
  static readonly type = '[PrimaNota] Riapri';
  constructor(
    public anno: number,
    public numero: number,
    public page?: number,
    public size?: number
  ) {}
}

/**
 * Action per il successo della riapertura
 */
export class RiapriPrimaNotaSuccess {
  static readonly type = '[PrimaNota] Riapri Success';
  constructor(public messaggio: string) {}
}

/**
 * Action per il fallimento della riapertura
 */
export class RiapriPrimaNotaFailed {
  static readonly type = '[PrimaNota] Riapri Failed';
  constructor(public error: any) {}
}

/**
 * Action per validare una prima nota singola
 */
export class ValidaPrimaNota {
  static readonly type = '[PrimaNota] Valida';
  constructor(public anno: number, public numero: number) {}
}

/**
 * Action per il successo della validazione
 */
export class ValidaPrimaNotaSuccess {
  static readonly type = '[PrimaNota] Valida Success';
  constructor(public messaggio: string) {}
}

/**
 * Action per il fallimento della validazione
 */
export class ValidaPrimaNotaFailed {
  static readonly type = '[PrimaNota] Valida Failed';
  constructor(public error: any) {}
}

/**
 * Action per validare massivamente prime note
 */
export class ValidaPrimeNoteMassiva {
  static readonly type = '[PrimaNota] Valida Massiva';
  constructor(public primaNoteIds: { anno: number, numero: number }[]) {}
}

/**
 * Action per il successo della validazione massiva
 */
export class ValidaPrimeNoteMassivaSuccess {
  static readonly type = '[PrimaNota] Valida Massiva Success';
  constructor(public messaggio: string) {}
}

/**
 * Action per il fallimento della validazione massiva
 */
export class ValidaPrimeNoteMassivaFailed {
  static readonly type = '[PrimaNota] Valida Massiva Failed';
  constructor(public error: any) {}
}

/**
 * Action per confermare la validazione con rinumerazione
 * Dispatchata quando l'utente conferma la validazione
 * mantieniNumero:
 *  - true: mantiene il numero definitivo esistente (WEB_CON_033, pulsante Si)
 *  - false: richiede nuova numerazione (WEB_CON_033, pulsante No)
 *  - null: conferma generica senza rinumerazione (WEB_CON_0016, pulsante Si)
 */
export class ConfermaValidazioneConRinumerazione {
  static readonly type = '[PrimaNota] Conferma Validazione con Rinumerazione';
  constructor(
    public anno: number,
    public numero: number,
    public mantieniNumero: boolean | null
  ) {}
}

/**
 * Action per confermare la validazione massiva con rinumerazione
 * mantieniNumeri:
 *  - true: mantiene i numeri definitivi esistenti (WEB_CON_034, pulsante Si)
 *  - false: richiede nuova numerazione (WEB_CON_034, pulsante No)
 *  - null: conferma generica senza rinumerazione (WEB_CON_0017, pulsante Si)
 */
export class ConfermaValidazioneMassivaConRinumerazione {
  static readonly type = '[PrimaNota] Conferma Validazione Massiva con Rinumerazione';
  constructor(
    public primaNoteIds: { anno: number, numero: number }[],
    public mantieniNumeri: boolean | null
  ) {}
}
