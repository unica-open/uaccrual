/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { AsyncReportSearchRequest, AsyncReportSearchResponse, AsyncReportDetail } from '../models/report.models';

/**
 * Avvia ricerca report asincroni
 */
export class SearchAsyncReports {
  static readonly type = '[AsyncReport] Search';
  constructor(
    public request: AsyncReportSearchRequest
  ) { }
}

/**
 * Cambia pagina risultati
 */
export class ChangeAsyncReportPage {
  static readonly type = '[AsyncReport] Change Page';
  constructor(
    public page: number,
    public size: number
  ) { }
}

/**
 * Carica dettaglio report
 */
export class LoadAsyncReportDetail {
  static readonly type = '[AsyncReport] Load Detail';
  constructor(public reportId: string) { }
}

/**
 * Scarica report
 */
export class DownloadAsyncReport {
  static readonly type = '[AsyncReport] Download';
  constructor(
    public reportId: string,
    public formato: string = 'pdf'
  ) { }
}

/**
 * Annulla report in coda/elaborazione
 */
export class CancelAsyncReport {
  static readonly type = '[AsyncReport] Cancel';
  constructor(public reportId: string) { }
}

/**
 * Elimina report completato/fallito
 */
export class DeleteAsyncReport {
  static readonly type = '[AsyncReport] Delete';
  constructor(public reportId: string) { }
}

/**
 * Pulisce lo stato (navigazione away)
 */
export class ClearAsyncReportState {
  static readonly type = '[AsyncReport] Clear State';
}

/**
 * Imposta messaggio di notifica
 */
export class SetAsyncReportMessage {
  static readonly type = '[AsyncReport] Set Message';
  constructor(
    public message: {
      type: 'error' | 'warning' | 'info' | 'success';
      title: string;
      text: string;
    }
  ) { }
}

/**
 * Pulisce messaggio
 */
export class ClearAsyncReportMessage {
  static readonly type = '[AsyncReport] Clear Message';
}
