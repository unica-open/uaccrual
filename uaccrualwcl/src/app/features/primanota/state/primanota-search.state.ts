/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { State, Action, StateContext, Selector } from '@ngxs/store';
import { Injectable } from '@angular/core';
import { tap, catchError, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { Router } from '@angular/router';

import { PrimanotaService } from '../../../core/services/primanota.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Page } from '../../../shared/models/page.model';
import { PrimaNotaDetail, PrimaNotaSearchFilter, PrimaNotaSearchResult } from '../models/primanota.model';
import {
  ChangePage,
  CheckIncomplete,
  ClearSearch,
  GetPrimaNotaDetail,
  SearchFailed,
  SearchPrimeNote,
  AnnullaPrimaNota,
  AnnullaPrimaNotaSuccess,
  AnnullaPrimaNotaFailed,
  RiapriPrimaNota,
  RiapriPrimaNotaSuccess,
  RiapriPrimaNotaFailed,
  ValidaPrimaNota,
  ValidaPrimaNotaSuccess,
  ValidaPrimaNotaFailed,
  ValidaPrimeNoteMassiva,
  ValidaPrimeNoteMassivaSuccess,
  ValidaPrimeNoteMassivaFailed
} from './primanota-search.actions';

export interface PrimanotaSearchStateModel {
  filter: Partial<PrimaNotaSearchFilter>;
  results: PrimaNotaSearchResult[];
  totalElements: number;
  page: number;
  size: number;
  selectedPrimaNota: PrimaNotaDetail | null;
  loading: boolean;
  error: any;
}

const DEFAULTS: PrimanotaSearchStateModel = {
    filter: {},
    results: [],
    totalElements: 0,
    page: 0,
    size: 20,
    selectedPrimaNota: null,
    loading: false,
    error: null
};

@State<PrimanotaSearchStateModel>({
  name: 'primanotaSearch',
  defaults: DEFAULTS
})
@Injectable()
export class PrimanotaSearchState {
  constructor(
    private router: Router,
    private primanotaService: PrimanotaService,
    private notificationService: NotificationService
  ) {}

  @Selector() static filter(state: PrimanotaSearchStateModel): Partial<PrimaNotaSearchFilter> { return state.filter; }
  @Selector() static results(state: PrimanotaSearchStateModel): PrimaNotaSearchResult[] { return state.results; }
  @Selector() static totalElements(state: PrimanotaSearchStateModel): number { return state.totalElements; }
  @Selector() static currentPage(state: PrimanotaSearchStateModel): number { return state.page; }
  @Selector() static pageSize(state: PrimanotaSearchStateModel): number { return state.size; }
  @Selector() static selectedPrimaNota(state: PrimanotaSearchStateModel): PrimaNotaDetail | null { return state.selectedPrimaNota; }
  @Selector() static loading(state: PrimanotaSearchStateModel): boolean { return state.loading; }
  @Selector() static error(state: PrimanotaSearchStateModel): any { return state.error; }

  @Action(SearchPrimeNote)
  search(ctx: StateContext<PrimanotaSearchStateModel>, action: SearchPrimeNote) {
    const state = ctx.getState();
    const filter = { ...state.filter, ...action.filter };
    // Resetta i risultati e totalElements quando inizia una nuova ricerca
    ctx.patchState({
      loading: true,
      error: null,
      filter: filter,
      page: 0,
      results: [],
      totalElements: 0
    });

    return this.primanotaService.ricercaPrimaNote(filter, 0, state.size, undefined, true).pipe(
      tap((page: Page<PrimaNotaSearchResult>) => {
        // Ottieni i dati dal servizio come fa PDC
        const results = this.primanotaService.primaNotaListValue;
        const totalElements = this.primanotaService.totalElementsValue;

        ctx.patchState({
          results: results,
          totalElements: totalElements,
          loading: false
        });

        console.log('Ricerca prima nota completata, risultati:', totalElements);
      }),
      catchError(err => ctx.dispatch(new SearchFailed(err)))
    );
  }

  @Action(ChangePage)
  changePage(ctx: StateContext<PrimanotaSearchStateModel>, action: ChangePage) {
    const state = ctx.getState();
    ctx.patchState({ loading: true, error: null, page: action.page, size: action.size });

    return this.primanotaService.ricercaPrimaNote(state.filter, action.page, action.size, undefined, true).pipe(
        tap((page: Page<PrimaNotaSearchResult>) => {
          // Ottieni i dati dal servizio come fa PDC
          const results = this.primanotaService.primaNotaListValue;
          const totalElements = this.primanotaService.totalElementsValue;

          ctx.patchState({
            results: results,
            totalElements: totalElements,
            loading: false
          });
        }),
        catchError(err => ctx.dispatch(new SearchFailed(err)))
      );
  }

  @Action(GetPrimaNotaDetail)
  getById(ctx: StateContext<PrimanotaSearchStateModel>, action: GetPrimaNotaDetail) {
    ctx.patchState({ loading: true, error: null, selectedPrimaNota: null });
    return this.primanotaService.getByAnnoAndNumero(action.anno, action.numero).pipe(
      tap(detail => ctx.patchState({ selectedPrimaNota: detail, loading: false })),
      catchError(err => ctx.dispatch(new SearchFailed(err)))
    );
  }


  @Action(SearchFailed)
  searchFailed(ctx: StateContext<PrimanotaSearchStateModel>, action: SearchFailed) {
      ctx.patchState({ loading: false, error: action.error });
  }

  @Action(ClearSearch)
  clearState(ctx: StateContext<PrimanotaSearchStateModel>) {
    const state = ctx.getState();
    // Mantieni il filtro ma resetta i risultati
    ctx.patchState({
      results: [],
      totalElements: 0,
      page: 0,
      selectedPrimaNota: null,
      loading: false,
      error: null
    });
  }

  @Action(AnnullaPrimaNota)
  annulla(ctx: StateContext<PrimanotaSearchStateModel>, action: AnnullaPrimaNota) {
    ctx.patchState({ loading: true, error: null });

    const currentPage = this.primanotaService.currentPageValue;
    const currentSize = this.primanotaService.pageSizeValue;
    const currentFilters = this.primanotaService.filtersValue;

    return this.primanotaService.annullaPrimaNota(action.anno, action.numero).pipe(
      switchMap((response) => {
        ctx.dispatch(new AnnullaPrimaNotaSuccess(response.messaggio));
        return this.primanotaService.ricercaPrimaNote(currentFilters, currentPage, currentSize, undefined, true).pipe(
          tap(() => {
            const results = this.primanotaService.primaNotaListValue;
            const totalElements = this.primanotaService.totalElementsValue;
            ctx.patchState({
              results: results,
              totalElements: totalElements,
              loading: false,
              page: currentPage,
              size: currentSize
            });
          })
        );
      }),
      catchError(err => ctx.dispatch(new AnnullaPrimaNotaFailed(err)))
    );
  }

  @Action(AnnullaPrimaNotaSuccess)
  annullaSuccess(ctx: StateContext<PrimanotaSearchStateModel>, action: AnnullaPrimaNotaSuccess) {
    this.notificationService.showMessageByCode(action.messaggio);
    ctx.patchState({ loading: false });
  }

  @Action(AnnullaPrimaNotaFailed)
  annullaFailed(ctx: StateContext<PrimanotaSearchStateModel>, action: AnnullaPrimaNotaFailed) {
    const errorMessage = action.error?.error?.detail || 'Errore durante l\'annullamento della prima nota';
    this.notificationService.showError(errorMessage, 'Errore Annullamento');
    ctx.patchState({ loading: false, error: action.error });
  }

  @Action(RiapriPrimaNota)
  riapri(ctx: StateContext<PrimanotaSearchStateModel>, action: RiapriPrimaNota) {
    ctx.patchState({ loading: true, error: null });

    // Uso pagina e size dal servizio (sempre aggiornati con l'UI)
    // Se l'action li ha passati esplicitamente, hanno precedenza
    const currentPage = action.page !== undefined ? action.page : this.primanotaService.currentPageValue;
    const currentSize = action.size !== undefined ? action.size : this.primanotaService.pageSizeValue;
    // Uso i filtri correnti dal servizio (sempre aggiornati)
    const currentFilters = this.primanotaService.filtersValue;

    return this.primanotaService.riapriPrimaNota(action.anno, action.numero).pipe(
      switchMap((response) => {
        ctx.dispatch(new RiapriPrimaNotaSuccess(response.messaggio));
        // Ricarica la lista con i filtri correnti mantenendo la pagina
        return this.primanotaService.ricercaPrimaNote(currentFilters, currentPage, currentSize, undefined, true).pipe(
          tap(() => {
            const results = this.primanotaService.primaNotaListValue;
            const totalElements = this.primanotaService.totalElementsValue;
            ctx.patchState({
              results: results,
              totalElements: totalElements,
              loading: false,
              page: currentPage,
              size: currentSize
            });
          })
        );
      }),
      catchError(err => ctx.dispatch(new RiapriPrimaNotaFailed(err)))
    );
  }

  @Action(RiapriPrimaNotaSuccess)
  riapriSuccess(ctx: StateContext<PrimanotaSearchStateModel>, action: RiapriPrimaNotaSuccess) {
    this.notificationService.showMessageByCode(action.messaggio);
    ctx.patchState({ loading: false });
  }

  @Action(RiapriPrimaNotaFailed)
  riapriFailed(ctx: StateContext<PrimanotaSearchStateModel>, action: RiapriPrimaNotaFailed) {
    const errorMessage = action.error?.error?.detail || 'Errore durante la riapertura della prima nota';
    this.notificationService.showError(errorMessage, 'Errore Riapertura');
    ctx.patchState({ loading: false, error: action.error });
  }

  @Action(ValidaPrimaNota)
  valida(ctx: StateContext<PrimanotaSearchStateModel>, action: ValidaPrimaNota) {
    ctx.patchState({ loading: true, error: null });

    const currentPage = this.primanotaService.currentPageValue;
    const currentSize = this.primanotaService.pageSizeValue;
    const currentFilters = this.primanotaService.filtersValue;

    return this.primanotaService.validaPrimaNota(action.anno, action.numero).pipe(
      switchMap((response) => {
        ctx.dispatch(new ValidaPrimaNotaSuccess(response.messaggio));
        return this.primanotaService.ricercaPrimaNote(currentFilters, currentPage, currentSize, undefined, true).pipe(
          tap(() => {
            const results = this.primanotaService.primaNotaListValue;
            const totalElements = this.primanotaService.totalElementsValue;
            ctx.patchState({
              results: results,
              totalElements: totalElements,
              loading: false,
              page: currentPage,
              size: currentSize
            });
          })
        );
      }),
      catchError(err => {
        // Se l'utente ha annullato la conferma, non mostro errore
        if (err?.confirmationCancelled) {
          ctx.patchState({ loading: false });
          return of(undefined);
        }
        return of(ctx.dispatch(new ValidaPrimaNotaFailed(err)));
      })
    );
  }

  @Action(ValidaPrimaNotaSuccess)
  validaSuccess(ctx: StateContext<PrimanotaSearchStateModel>, action: ValidaPrimaNotaSuccess) {
    this.notificationService.showMessageByCode(action.messaggio);
    ctx.patchState({ loading: false });
  }

  @Action(ValidaPrimaNotaFailed)
  validaFailed(ctx: StateContext<PrimanotaSearchStateModel>, action: ValidaPrimaNotaFailed) {
    // Se l'errore è una conferma annullata, non mostro messaggio di errore
    if (action.error?.confirmationCancelled) {
      ctx.patchState({ loading: false });
      return;
    }

    const errorMessage = action.error?.error?.detail || 'Errore durante la validazione della prima nota';
    this.notificationService.showError(errorMessage, 'Errore Validazione');
    ctx.patchState({ loading: false, error: action.error });
  }

  @Action(ValidaPrimeNoteMassiva)
  validaMassiva(ctx: StateContext<PrimanotaSearchStateModel>, action: ValidaPrimeNoteMassiva) {
    ctx.patchState({ loading: true, error: null });

    const currentPage = this.primanotaService.currentPageValue;
    const currentSize = this.primanotaService.pageSizeValue;
    const currentFilters = this.primanotaService.filtersValue;

    return this.primanotaService.validaPrimeNoteMassiva(action.primaNoteIds).pipe(
      switchMap((response) => {
        ctx.dispatch(new ValidaPrimeNoteMassivaSuccess(response.messaggio));
        return this.primanotaService.ricercaPrimaNote(currentFilters, currentPage, currentSize, undefined, true).pipe(
          tap(() => {
            const results = this.primanotaService.primaNotaListValue;
            const totalElements = this.primanotaService.totalElementsValue;
            ctx.patchState({
              results: results,
              totalElements: totalElements,
              loading: false,
              page: currentPage,
              size: currentSize
            });
          })
        );
      }),
      catchError(err => {
        // Se l'utente ha annullato la conferma, non mostro errore
        if (err?.confirmationCancelled) {
          ctx.patchState({ loading: false });
          return of(undefined);
        }
        return of(ctx.dispatch(new ValidaPrimeNoteMassivaFailed(err)));
      })
    );
  }

  @Action(ValidaPrimeNoteMassivaSuccess)
  validaMassivaSuccess(ctx: StateContext<PrimanotaSearchStateModel>, action: ValidaPrimeNoteMassivaSuccess) {
    this.notificationService.showMessageByCode(action.messaggio);
    ctx.patchState({ loading: false });
  }

  @Action(ValidaPrimeNoteMassivaFailed)
  validaMassivaFailed(ctx: StateContext<PrimanotaSearchStateModel>, action: ValidaPrimeNoteMassivaFailed) {
    // Se l'errore è una conferma annullata, non mostro messaggio di errore
    if (action.error?.confirmationCancelled) {
      ctx.patchState({ loading: false });
      return;
    }

    const errorMessage = action.error?.error?.detail || 'Errore durante la validazione massiva delle prime note';
    this.notificationService.showError(errorMessage, 'Errore Validazione Massiva');
    ctx.patchState({ loading: false, error: action.error });
  }
}
