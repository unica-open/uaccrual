/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { State, Action, StateContext, Selector } from '@ngxs/store';
import { tap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { Router } from '@angular/router';

import { ReportService } from '../services/report.service';
import {
  SearchAsyncReports,
  ChangeAsyncReportPage,
  LoadAsyncReportDetail,
  DownloadAsyncReport,
  CancelAsyncReport,
  DeleteAsyncReport,
  ClearAsyncReportState,
  SetAsyncReportMessage,
  ClearAsyncReportMessage
} from './async-report.actions';
import {
  AsyncReportSearchRequest,
  AsyncReportSearchResponse,
  AsyncReportDetail
} from '../models/report.models';

/**
 * Modello dello stato NGXS
 */
export interface AsyncReportStateModel {
  /** Request di ricerca corrente */
  searchRequest: AsyncReportSearchRequest | null;
  /** Risultati ricerca (pagina corrente) */
  searchResults: AsyncReportSearchResponse | null;
  /** Dettaglio report selezionato */
  selectedReport: AsyncReportDetail | null;
  /** Stato di caricamento */
  loading: boolean;
  /** Stato di download in corso */
  downloading: boolean;
  /** Eventuale errore */
  error: any;
  /** Messaggio di notifica */
  message: {
    type: 'error' | 'warning' | 'info' | 'success';
    title: string;
    text: string;
  } | null;
}

/**
 * Valori default dello stato
 */
const DEFAULTS: AsyncReportStateModel = {
  searchRequest: null,
  searchResults: null,
  selectedReport: null,
  loading: false,
  downloading: false,
  error: null,
  message: null
};

/**
 * State NGXS per gestione report asincroni
 */
@State<AsyncReportStateModel>({
  name: 'asyncReport',
  defaults: DEFAULTS
})
@Injectable()
export class AsyncReportState {

  constructor(
    private reportService: ReportService,
    private router: Router
  ) { }

  // ==================== SELECTORS ====================

  @Selector()
  static getSearchResults(state: AsyncReportStateModel): AsyncReportSearchResponse | null {
    return state.searchResults;
  }

  @Selector()
  static getSelectedReport(state: AsyncReportStateModel): AsyncReportDetail | null {
    return state.selectedReport;
  }

  @Selector()
  static getSearchRequest(state: AsyncReportStateModel): AsyncReportSearchRequest | null {
    return state.searchRequest;
  }

  @Selector()
  static getCurrentFilters(state: AsyncReportStateModel): AsyncReportSearchRequest | null {
    return state.searchRequest;
  }

  @Selector()
  static isLoading(state: AsyncReportStateModel): boolean {
    return state.loading;
  }

  @Selector()
  static isDownloading(state: AsyncReportStateModel): boolean {
    return state.downloading;
  }

  @Selector()
  static getError(state: AsyncReportStateModel): any {
    return state.error;
  }

  @Selector()
  static getMessage(state: AsyncReportStateModel) {
    return state.message;
  }

  // ==================== ACTIONS ====================

  @Action(SearchAsyncReports)
  search(ctx: StateContext<AsyncReportStateModel>, action: SearchAsyncReports) {
    const state = ctx.getState();
    const request = action.request;

    ctx.patchState({
      loading: true,
      error: null,
      message: null, // Clear messaggio precedente
      searchRequest: request,
      selectedReport: null // Reset dettaglio quando si fa nuova ricerca
    });

    return this.reportService.searchAsyncReports(request).pipe(
      tap((response: AsyncReportSearchResponse) => {
        ctx.patchState({
          searchResults: response,
          loading: false
        });

        if (response.totale_elementi === 0) {
          ctx.dispatch(new SetAsyncReportMessage({
            type: 'info',
            title: 'Nessun Risultato',
            text: 'WEB_INF_0003 – Nessun dato trovato.'
          }));
        }
      }),
      catchError(err => {
        ctx.patchState({ loading: false, error: err });
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'error',
          title: 'Errore Ricerca',
          text: 'WEB_ERR_0001 - Errore durante la ricerca dei report'
        }));
        return of(null);
      })
    );
  }

  @Action(ChangeAsyncReportPage)
  changePage(ctx: StateContext<AsyncReportStateModel>, action: ChangeAsyncReportPage) {
    const state = ctx.getState();
    ctx.patchState({ loading: true, error: null });

    const request: AsyncReportSearchRequest = {
      ...state.searchRequest,
      pagina: action.page,
      dimensione_pagina: action.size
    };

    return this.reportService.searchAsyncReports(request).pipe(
      tap((response: AsyncReportSearchResponse) => {
        ctx.patchState({
          searchResults: response,
          loading: false
        });
      }),
      catchError(err => {
        ctx.patchState({ loading: false, error: err });
        return of(null);
      })
    );
  }

  @Action(LoadAsyncReportDetail)
  loadDetail(ctx: StateContext<AsyncReportStateModel>, action: LoadAsyncReportDetail) {
    ctx.patchState({ loading: true, error: null, selectedReport: null });

    return this.reportService.getAsyncReportDetail(action.reportId).pipe(
      tap((detail: AsyncReportDetail) => {
        ctx.patchState({
          selectedReport: detail,
          loading: false
        });
        this.router.navigate(['/report/search', action.reportId]);
      }),
      catchError(err => {
        ctx.patchState({ loading: false, error: err });
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'error',
          title: 'Errore Caricamento',
          text: 'WEB_ERR_0001 - Impossibile caricare il dettaglio del report'
        }));
        return of(null);
      })
    );
  }

  @Action(DownloadAsyncReport)
  download(ctx: StateContext<AsyncReportStateModel>, action: DownloadAsyncReport) {
    ctx.patchState({ downloading: true, error: null });

    return this.reportService.downloadAsyncReport(action.reportId).pipe(
      tap((blob: Blob) => {
        // Trigger download browser
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `report_${action.reportId}_${new Date().toISOString().split('T')[0]}.${action.formato}`;
        link.click();
        window.URL.revokeObjectURL(url);

        ctx.patchState({ downloading: false });
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'success',
          title: 'Download Completato',
          text: 'WEB_OK_0001 - Report scaricato con successo'
        }));
      }),
      catchError(err => {
        ctx.patchState({ downloading: false, error: err });
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'error',
          title: 'Errore Download',
          text: 'WEB_ERR_0001 - Impossibile scaricare il report'
        }));
        return of(null);
      })
    );
  }

  @Action(CancelAsyncReport)
  cancel(ctx: StateContext<AsyncReportStateModel>, action: CancelAsyncReport) {
    ctx.patchState({ loading: true, error: null });

    return this.reportService.cancelAsyncReport(action.reportId).pipe(
      tap(() => {
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'success',
          title: 'Annullamento',
          text: 'WEB_OK_0002 - Report annullato con successo'
        }));
        // Ricarica la lista
        const state = ctx.getState();
        if (state.searchRequest) {
          ctx.dispatch(new SearchAsyncReports({ ...state.searchRequest, pagina: 0 }));
        }
      }),
      catchError(err => {
        ctx.patchState({ loading: false, error: err });
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'error',
          title: 'Errore Annullamento',
          text: 'WEB_ERR_0001 - Impossibile annullare il report'
        }));
        return of(null);
      })
    );
  }

  @Action(DeleteAsyncReport)
  delete(ctx: StateContext<AsyncReportStateModel>, action: DeleteAsyncReport) {
    ctx.patchState({ loading: true, error: null });

    return this.reportService.deleteAsyncReport(action.reportId).pipe(
      tap(() => {
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'success',
          title: 'Eliminazione',
          text: 'WEB_OK_0004 - Report eliminato con successo'
        }));
        // Ricarica la lista e torna alla ricerca
        const state = ctx.getState();
        if (state.searchRequest) {
          ctx.dispatch(new SearchAsyncReports({ ...state.searchRequest, pagina: 0 }));
        }
        this.router.navigate(['/report/search']);
      }),
      catchError(err => {
        ctx.patchState({ loading: false, error: err });
        ctx.dispatch(new SetAsyncReportMessage({
          type: 'error',
          title: 'Errore Eliminazione',
          text: 'WEB_ERR_0001 - Impossibile eliminare il report'
        }));
        return of(null);
      })
    );
  }

  @Action(ClearAsyncReportState)
  clear(ctx: StateContext<AsyncReportStateModel>) {
    ctx.setState(DEFAULTS);
  }

  @Action(SetAsyncReportMessage)
  setMessage(ctx: StateContext<AsyncReportStateModel>, action: SetAsyncReportMessage) {
    ctx.patchState({ message: action.message });
  }

  @Action(ClearAsyncReportMessage)
  clearMessage(ctx: StateContext<AsyncReportStateModel>) {
    ctx.patchState({ message: null });
  }
}
