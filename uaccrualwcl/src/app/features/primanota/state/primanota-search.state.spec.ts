/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { Router } from '@angular/router';

import { PrimanotaSearchState, PrimanotaSearchStateModel } from './primanota-search.state';
import { PrimanotaService } from '../../../core/services/primanota.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Page } from '../../../shared/models/page.model';
import { PrimaNotaSearchResult, PrimaNotaDetail } from '../models/primanota.model';
import { of } from 'rxjs';
import {
  SearchPrimeNote,
  ChangePage,
  GetPrimaNotaDetail,
  ClearSearch,
  SearchFailed,
  AnnullaPrimaNota,
  AnnullaPrimaNotaSuccess,
  RiapriPrimaNota
} from './primanota-search.actions';

describe('PrimanotaSearchState', () => {
  let store: Store;
  let primanotaServiceSpy: jasmine.SpyObj<PrimanotaService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const initialState: PrimanotaSearchStateModel = {
    filter: {},
    results: [],
    totalElements: 0,
    page: 0,
    size: 20,
    selectedPrimaNota: null,
    loading: false,
    error: null
  };

  beforeEach(() => {
    const primanotaSpy = jasmine.createSpyObj('PrimanotaService', [
      'ricercaPrimaNote',
      'getByAnnoAndNumero',
      'annullaPrimaNota',
      'riapriPrimaNota'
    ]);

    // Mock per le proprietà observable
    Object.defineProperty(primanotaSpy, 'primaNotaListValue', {
      get: () => []
    });
    Object.defineProperty(primanotaSpy, 'totalElementsValue', {
      get: () => 0
    });

    const notificationSpy = jasmine.createSpyObj('NotificationService', ['showMessageByCode', 'showError']);
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([PrimanotaSearchState])],
      providers: [
        { provide: PrimanotaService, useValue: primanotaSpy },
        { provide: NotificationService, useValue: notificationSpy },
        { provide: Router, useValue: routerMock }
      ]
    });

    store = TestBed.inject(Store);
    primanotaServiceSpy = TestBed.inject(PrimanotaService) as jasmine.SpyObj<PrimanotaService>;
    notificationServiceSpy = TestBed.inject(NotificationService) as jasmine.SpyObj<NotificationService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    store.reset(initialState);
  });

  it('should create an instance', () => {
    expect(new PrimanotaSearchState(routerSpy, primanotaServiceSpy, notificationServiceSpy)).toBeTruthy();
  });

  describe('Selectors', () => {
    it('should select filter', () => {
      const filter = { codiceStato: 'A' };
      store.reset({
        ...initialState,
        filter
      });

      const selectedFilter = store.selectSnapshot(PrimanotaSearchState.filter);
      expect(selectedFilter).toEqual(filter);
    });

    it('should select results', () => {
      const results: PrimaNotaSearchResult[] = [
        {
          id: 1,
          anno: 2025,
          tipo: 'TIPO1',
          fonte: 'FONTE1',
          evento: 'EVENTO1',
          causale: 'CAUS1',
          numeroProvvisorio: 123,
          dataRegProvvisoria: new Date('2025-01-15'),
          stato: 'A',
          numeroDefinitivo: 0,
          dataRegDefinitiva: new Date('2025-01-15'),
          descrizione: 'Test',
          isAggiornabile: true,
          isAnnullabile: false,
          isRiapribile: false,
          isValidabile: true
        }
      ];
      store.reset({
        ...initialState,
        results
      });

      const selectedResults = store.selectSnapshot(PrimanotaSearchState.results);
      expect(selectedResults).toEqual(results);
    });

    it('should select total elements', () => {
      store.reset({
        ...initialState,
        totalElements: 100
      });

      const total = store.selectSnapshot(PrimanotaSearchState.totalElements);
      expect(total).toBe(100);
    });

    it('should select current page', () => {
      store.reset({
        ...initialState,
        page: 2
      });

      const page = store.selectSnapshot(PrimanotaSearchState.currentPage);
      expect(page).toBe(2);
    });

    it('should select page size', () => {
      store.reset({
        ...initialState,
        size: 50
      });

      const size = store.selectSnapshot(PrimanotaSearchState.pageSize);
      expect(size).toBe(50);
    });

    it('should select selected prima nota', () => {
      const primaNota: PrimaNotaDetail = {
        id: 1,
        anno: 2025,
        tipo: 'TIPO1',
        tipoDescrizione: 'Tipo 1',
        fonte: 'FONTE1',
        fonteDescrizione: 'Fonte 1',
        evento: 'EVENTO1',
        eventoDescrizione: 'Evento 1',
        causale: 'CAUS1',
        causaleDescrizione: 'Causale 1',
        numeroProvvisorio: 123,
        dataRegProvvisoria: new Date('2025-01-15'),
        stato: 'A',
        statoDescrizione: 'Attivo',
        numeroDefinitivo: 0,
        dataRegDefinitiva: new Date('2025-01-15'),
        descrizione: 'Test',
        conti: [],
        attributi: [],
        primeNoteCollegate: []
      };
      store.reset({
        ...initialState,
        selectedPrimaNota: primaNota
      });

      const selected = store.selectSnapshot(PrimanotaSearchState.selectedPrimaNota);
      expect(selected).toEqual(primaNota);
    });

    it('should select loading state', () => {
      store.reset({
        ...initialState,
        loading: true
      });

      const loading = store.selectSnapshot(PrimanotaSearchState.loading);
      expect(loading).toBe(true);
    });

    it('should select error', () => {
      const error = { message: 'Test error' };
      store.reset({
        ...initialState,
        error
      });

      const selectedError = store.selectSnapshot(PrimanotaSearchState.error);
      expect(selectedError).toEqual(error);
    });
  });

  describe('Actions', () => {
    it('should handle ClearSearch', () => {
      store.reset({
        ...initialState,
        results: [{
          id: 1,
          anno: 2025,
          tipo: 'TIPO1',
          fonte: 'FONTE1',
          evento: 'EVENTO1',
          causale: 'CAUS1',
          numeroProvvisorio: 123,
          dataRegProvvisoria: new Date('2025-01-15'),
          stato: 'A',
          numeroDefinitivo: 0,
          dataRegDefinitiva: new Date('2025-01-15'),
          descrizione: 'Test',
          isAggiornabile: true,
          isAnnullabile: false,
          isRiapribile: false,
          isValidabile: true
        }],
        totalElements: 1
      });

      store.dispatch(new ClearSearch());

      const state = store.selectSnapshot((state) => state.primanotaSearch);
      expect(state.results).toEqual([]);
      expect(state.totalElements).toBe(0);
      expect(state.page).toBe(0);
      expect(state.selectedPrimaNota).toBeNull();
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('should handle SearchFailed', () => {
      const error = { message: 'Search failed' };

      store.dispatch(new SearchFailed(error));

      const state = store.selectSnapshot((state) => state.primanotaSearch);
      expect(state.loading).toBe(false);
      expect(state.error).toEqual(error);
    });

    it('should handle AnnullaPrimaNotaSuccess', () => {
      store.dispatch(new AnnullaPrimaNotaSuccess('Annullamento completato'));

      expect(notificationServiceSpy.showMessageByCode).toHaveBeenCalledWith('Annullamento completato');
      
      const state = store.selectSnapshot((state) => state.primanotaSearch);
      expect(state.loading).toBe(false);
    });

    it('should handle RiapriPrimaNota', () => {
      primanotaServiceSpy.riapriPrimaNota.and.returnValue(of({ messaggio: 'Riapertura completata' }));

      store.dispatch(new RiapriPrimaNota(2025, 123));

      expect(primanotaServiceSpy.riapriPrimaNota).toHaveBeenCalledWith(2025, 123);
    });
  });
});
