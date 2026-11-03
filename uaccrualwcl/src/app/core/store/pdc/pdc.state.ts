/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { State, Action, StateContext, Selector } from '@ngxs/store';
import { PdcService } from '../../services/pdc.service';
import { SearchPdcConti, GetPdcDetail, ClearPdcState, ChangePdcPage, DeletePdc, SetMessage, ClearMessage } from './pdc.actions';
import { tap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';

// TODO: Definire modelli forti per lo stato
export interface PdcStateModel {
  searchFilter: any | null;
  searchResults: any | null; // Page<Pdc>
  selectedConto: any | null; // Pdc
  loading: boolean;
  message: {
    type: 'error' | 'warning' | 'info' | 'success';
    title: string;
    text: string;
  } | null;
}

@State<PdcStateModel>({
  name: 'pdc',
  defaults: {
    searchFilter: null,
    searchResults: null,
    selectedConto: null,
    loading: false,
    message: null
  }
})
@Injectable()
export class PdcState {

  constructor(
    private pdcService: PdcService,
    private router: Router,
    private notificationService: NotificationService
  ) { }

  @Selector()
  static getSearchResults(state: PdcStateModel) {
    return state.searchResults;
  }

  @Selector()
  static isLoading(state: PdcStateModel) {
    return state.loading;
  }

  @Selector()
  static getMessage(state: PdcStateModel) {
    return state.message;
  }

  @Action(SearchPdcConti)
  searchConti(ctx: StateContext<PdcStateModel>, action: SearchPdcConti) {
    console.log('Inizio ricerca PDC con filtri:', action.filter);
    ctx.patchState({
      loading: true,
      searchFilter: action.filter
    });

    return this.pdcService.ricercaPdc(action.filter, 0, 10, 'codice,asc', true).pipe(
      tap(response => {
        console.log('Risposta ricevuta dal servizio:', response);
        // Aggiorniamo lo stato NGXS con i risultati ottenuti
        ctx.patchState({
          searchResults: {
            content: this.pdcService.pdcListValue,
            totalElements: this.pdcService.totalElementsValue,
            size: 10, // valore di default o ottenuto da qualche parte
            number: 0, // valore di default o ottenuto da qualche parte
            totalPages: Math.ceil(this.pdcService.totalElementsValue / 10) // calcolo approssimativo
          },
          loading: false
        });
        console.log('Stato aggiornato con nuovi risultati:', ctx.getState());

        // Controlliamo comunque se ci sono risultati per decidere se navigare o meno
        console.log('Ricerca completata, risultati:', this.pdcService.totalElementsValue);
      }),
      catchError(err => {
        console.error('Errore durante la ricerca:', err);
        ctx.patchState({ loading: false });
        // L'errore viene già gestito a livello di servizio/interceptor
        return of(null);
      })
    );
  }

  @Action(ChangePdcPage)
  changePage(ctx: StateContext<PdcStateModel>, action: ChangePdcPage) {
    const state = ctx.getState();
    ctx.patchState({ loading: true });

    return this.pdcService.ricercaPdc(state.searchFilter, action.page, action.size, 'codice,asc', true).pipe(
      tap(response => {
        // Aggiorniamo lo stato NGXS con i risultati ottenuti
        ctx.patchState({
          searchResults: {
            content: this.pdcService.pdcListValue,
            totalElements: this.pdcService.totalElementsValue,
            size: action.size,
            number: action.page,
            totalPages: Math.ceil(this.pdcService.totalElementsValue / action.size) // calcolo approssimativo
          },
          loading: false
        });
      }),
      catchError(err => {
        ctx.patchState({ loading: false });
        return of(null);
      })
    );
  }

  @Action(DeletePdc)
  deletePdc(ctx: StateContext<PdcStateModel>, action: DeletePdc) {
    ctx.patchState({ loading: true });
    console.log('Inizio cancellazione PDC:', action.codiceUfficiale, action.codiceClasse, action.codiceFamiglia);

    return this.pdcService.deletePdc(action.codiceUfficiale, action.codiceClasse, action.codiceFamiglia).pipe(
      tap(() => {
        console.log('Cancellazione completata con successo');
        // Imposta il messaggio invece di mostrarlo direttamente
        ctx.patchState({
          message: {
            type: 'success',
            title: 'Successo',
            text: 'WEB_OK_0004 - Oggetto eliminato correttamente'
          }
        });

        // Dopo l'eliminazione, riesegui la ricerca con i criteri precedenti
        const state = ctx.getState();
        console.log('Stato prima della ricerca:', state);

        const searchFilter = state.searchFilter;
        console.log('Filtri di ricerca da utilizzare:', searchFilter);

        const currentPage = state.searchResults?.number || 0;
        const pageSize = state.searchResults?.size || 10;

        console.log('currentPage:', currentPage, 'pageSize:', pageSize);

        // Riesegui la ricerca con i filtri precedenti per mantenere lo stesso contesto
        ctx.dispatch(new SearchPdcConti(searchFilter));
        console.log('Dispatch SearchPdcConti completato');
      }),
      catchError(err => {
        console.error('Errore durante la cancellazione:', err);
        ctx.patchState({
          loading: false,
          message: {
            type: 'error',
            title: 'Errore',
            text: 'WEB_ERR_0001 - Si è verificato un errore durante la cancellazione'
          }
        });
        // L'errore specifico viene notificato dal servizio, qui resettiamo solo il loading
        return of(null);
      })
    );
  }

  // TODO: Implementare l'azione GetPdcDetail

  @Action(ClearPdcState)
  clear(ctx: StateContext<PdcStateModel>) {
    ctx.setState({
      searchFilter: null,
      searchResults: null,
      selectedConto: null,
      loading: false,
      message: null
    });
  }

  @Action(SetMessage)
  setMessage(ctx: StateContext<PdcStateModel>, action: SetMessage) {
    ctx.patchState({
      message: action.message
    });
  }

  @Action(ClearMessage)
  clearMessage(ctx: StateContext<PdcStateModel>) {
    ctx.patchState({
      message: null
    });
  }
}
