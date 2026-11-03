/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { CausaleSearchCriteria, Causale } from '../../../features/causale-accrual/model/causale.model';

// Azione per avviare una ricerca delle causali.
export class SearchCausali {
  static readonly type = '[Causale] Search Causali';
  constructor(public filter: CausaleSearchCriteria) { }
}

// Azione per recuperare il dettaglio di una singola causale.
export class GetCausaleDetail {
  static readonly type = '[Causale] Get Causale Detail';
  constructor(public id: number) { }
}

// Azione per gestire la paginazione dei risultati di ricerca.
export class ChangeCausalePage {
  static readonly type = '[Causale] Change Page';
  constructor(public page: number, public size: number) { }
}

// Azione per inserire una nuova causale.
export class InsertCausale {
  static readonly type = '[Causale] Insert Causale';
  constructor(public payload: Causale) { }
}

// Azione per aggiornare una causale esistente.
export class UpdateCausale {
  static readonly type = '[Causale] Update Causale';
  constructor(public id: number, public payload: Causale) { }
}

// Azione per eliminare una causale.
export class DeleteCausale {
  static readonly type = '[Causale] Delete Causale';
  constructor(public id: number) { }
}

// Azione per validare una causale.
export class ValidaCausale {
  static readonly type = '[Causale] Valida Causale';
  constructor(public id: number) { }
}

// Azione per pulire lo stato delle causali.
export class ClearCausaleState {
  static readonly type = '[Causale] Clear State';
}
