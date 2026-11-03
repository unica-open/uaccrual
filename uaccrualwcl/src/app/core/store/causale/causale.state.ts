/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { State, Action, StateContext, Selector } from '@ngxs/store';
import { tap, catchError } from 'rxjs/operators';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { CausaleAccrualService } from '../../services/causale-accrual.service'; // Il servizio modificato
import { Causale, CausaleSearchCriteria, Paged, CausaleRequest } from '../../../features/causale-accrual/model/causale.model'; // I modelli
import {
  SearchCausali,
  GetCausaleDetail,
  ChangeCausalePage,
  InsertCausale,
  UpdateCausale,
  DeleteCausale,
  ClearCausaleState,
  ValidaCausale
} from './causale.actions'; // Le azioni appena create
import { NotificationService } from '../../services/notification.service';

export interface CausaleStateModel {
  searchFilter: CausaleSearchCriteria | null;
  searchResults: Paged<Causale> | null;
  selectedCausale: Causale | null;
  loading: boolean;
}

@State<CausaleStateModel>({
  name: 'causale',
  defaults: {
    searchFilter: null,
    searchResults: null,
    selectedCausale: null,
    loading: false
  }
})
@Injectable()
export class CausaleState {

  constructor(
    private causaleAccrualService: CausaleAccrualService,
    private router: Router,
    private notificationService: NotificationService
  ) { }

  /**
   * Estrae il messaggio da mostrare a fronte di un errore HTTP.
   * Il BE risponde con il DTO Error ({ status, code, title, detail }): il testo
   * utile per l'utente sta in `detail`. Gli altri formati sono gestiti come fallback.
   */
  private extractErrorMessage(err: any, fallback: string): string {
    if (err?.error) {
      if (typeof err.error === 'string') {
        return err.error;
      }
      return err.error.detail || err.error.message || err.error.title || err.error.error || fallback;
    }
    return err?.message || fallback;
  }

  @Selector()
  static getSearchResults(state: CausaleStateModel) {
    console.log('state:',state);
    return state.searchResults;
  }

  @Selector()
  static isLoading(state: CausaleStateModel) {
    return state.loading;
  }

  @Selector()
  static getSelectedCausale(state: CausaleStateModel) {
    return state.selectedCausale;
  }

  @Selector()
  static getSearchFilter(state: CausaleStateModel) {
    return state.searchFilter;
  }

  @Action(SearchCausali)
  searchCausali(ctx: StateContext<CausaleStateModel>, action: SearchCausali) {
    ctx.patchState({
      loading: true,
      searchFilter: action.filter
    });

    return this.causaleAccrualService.ricercaCausali(action.filter, 0, 10).pipe(
      tap(results => {
        if (results && results.total_elements > 0) {
          ctx.patchState({
            searchResults: results,
            loading: false
          });
          // TODO: Potenziale navigazione a una pagina di risultati, come in PDC
          // this.router.navigate(['/causale/results']);
        } else {
          ctx.patchState({
            searchResults: { data: [], page: 0, page_size: 10, total_elements: 0 }, // Adattato a Paged<Causale>
            loading: false
          });
        }
      }),
      catchError(err => {
        ctx.patchState({ loading: false });
        // L'errore viene già gestito a livello di servizio/interceptor e notificato
        return throwError(() => err);
      })
    );
  }

  @Action(GetCausaleDetail)
  getCausaleDetail(ctx: StateContext<CausaleStateModel>, action: GetCausaleDetail) {
    ctx.patchState({ loading: true });

    return this.causaleAccrualService.getCausaleById(action.id).pipe(
      tap(causale => {
        ctx.patchState({
          selectedCausale: causale,
          loading: false
        });
      }),
      catchError(err => {
        ctx.patchState({ loading: false });
        this.notificationService.error('Errore durante il recupero del dettaglio della causale.');
        return throwError(() => err);
      })
    );
  }


  @Action(ChangeCausalePage)
  changePage(ctx: StateContext<CausaleStateModel>, action: ChangeCausalePage) {
    const state = ctx.getState();
    ctx.patchState({ loading: true });

    // Assumendo che il filtro di ricerca sia mantenuto nello stato
    const currentFilter = state.searchFilter || {};

    return this.causaleAccrualService.ricercaCausali(currentFilter, action.page, action.size).pipe(
      tap(results => {
        ctx.patchState({
          searchResults: results,
          loading: false
        });
      }),
      catchError(err => {
        ctx.patchState({ loading: false });
        return throwError(() => err);
      })
    );
  }

  @Action(InsertCausale)
  insertCausale(ctx: StateContext<CausaleStateModel>, action: InsertCausale) {
    ctx.patchState({ loading: true });

    const causale: Causale = action.payload;
    const causaleRequest: CausaleRequest = {
      codice: causale.codice,
      descrizione: causale.descrizione,
      tipo: causale.tipo ? { id: causale.tipo.id } : undefined,
      evento: causale.evento ? { id: causale.evento.id } : undefined,
      stato: causale.stato ? { id: causale.stato.id } : undefined,
      ente: causale.ente ? { id: causale.ente.id } : undefined,
      inizio_validita: causale.inizio_validita,
      fine_validita: causale.fine_validita,
      contiCollegati: causale.contiCollegati?.map(conto => ({
        pdcId: Number(conto.pdcId!),
        classe: conto.classe,
        codice: conto.codice,
        descrizione: conto.descrizione,
        segno: conto.segno,
        tipoImporto: conto.tipoImporto,
        inizioValidita: conto.inizio_validita!,
        fineValidita: conto.fine_validita
      })) || [],
      version: causale.version
    };

    return this.causaleAccrualService.createCausale(causaleRequest).pipe(
      catchError(err => {
        ctx.patchState({ loading: false });
        return throwError(() => err);
      })
    );
  }

  @Action(UpdateCausale)
  updateCausale(ctx: StateContext<CausaleStateModel>, action: UpdateCausale) {
    ctx.patchState({ loading: true });
    
    const causale: Causale = action.payload;
    const causaleRequest: CausaleRequest = {
      codice: causale.codice,
      descrizione: causale.descrizione,
      tipo: causale.tipo ? { id: causale.tipo.id } : undefined,
      evento: causale.evento ? { id: causale.evento.id } : undefined,
      stato: causale.stato ? { id: causale.stato.id } : undefined,
      ente: causale.ente ? { id: causale.ente.id } : undefined,
      inizio_validita: causale.inizio_validita,
      fine_validita: causale.fine_validita,
      contiCollegati: causale.contiCollegati?.map(conto => ({
        pdcId: Number(conto.pdcId!),
        classe: conto.classe,
        codice: conto.codice,
        descrizione: conto.descrizione,
        segno: conto.segno,
        tipoImporto: conto.tipoImporto,
        inizioValidita: conto.inizio_validita!,
        fineValidita: conto.fine_validita
      })) || [],
      version: causale.version
    };

    return this.causaleAccrualService.updateCausale(action.id, causaleRequest).pipe(
      tap(() => {
        ctx.patchState({ loading: false });
        this.notificationService.success('Causale aggiornata con successo.'); // Esempio di messaggio di successo
        // Dopo l'aggiornamento, potresti voler ricaricare la lista o navigare
      }),
      catchError(err => {
        ctx.patchState({ loading: false });
        this.notificationService.error(
          this.extractErrorMessage(err, "Errore durante l'aggiornamento della causale."));
        return throwError(() => err);
      })
    );
  }

  @Action(DeleteCausale)
  deleteCausale(ctx: StateContext<CausaleStateModel>, action: DeleteCausale) {
    ctx.patchState({ loading: true });
    return this.causaleAccrualService.deleteCausale(action.id).pipe(
      tap(() => {
        ctx.patchState({ loading: false });
        this.notificationService.success('Causale eliminata con successo.'); // Esempio di messaggio di successo per eliminazione
        // Dopo l'eliminazione, ricarica la pagina corrente per aggiornare la lista
        const state = ctx.getState();
        const currentPage = state.searchResults?.page || 0;
        const pageSize = state.searchResults?.page_size || 10;
        ctx.dispatch(new ChangeCausalePage(currentPage, pageSize));
      }),
      catchError(err => {
        ctx.patchState({ loading: false });
        this.notificationService.error(
          this.extractErrorMessage(err, "Errore durante l'eliminazione della causale."));
        return throwError(() => err);
      })
    );
  }

  @Action(ValidaCausale)
  validaCausale(ctx: StateContext<CausaleStateModel>, action: ValidaCausale) {
    ctx.patchState({ loading: true });
    return this.causaleAccrualService.validaCausale(action.id).pipe(
      tap(() => {
        ctx.patchState({ loading: false });
        this.notificationService.success('Salvataggio effettuato correttamente');
        const state = ctx.getState();
        const currentPage = state.searchResults?.page || 0;
        const pageSize = state.searchResults?.page_size || 10;
        ctx.dispatch(new ChangeCausalePage(currentPage, pageSize));
      }),
      catchError(err => {
        ctx.patchState({ loading: false });
        this.notificationService.error(
          this.extractErrorMessage(err, 'Errore durante la validazione della causale.'));
        return throwError(() => err);
      })
    );
  }


  @Action(ClearCausaleState)
  clear(ctx: StateContext<CausaleStateModel>) {
    ctx.setState({
      searchFilter: null,
      searchResults: null,
      selectedCausale: null,
      loading: false
    });
  }
}
