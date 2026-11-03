/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { NgxsModule, Store } from '@ngxs/store';
import { Router } from '@angular/router';

import { AsyncReportState, AsyncReportStateModel } from './async-report.state';
import { ReportService } from '../services/report.service';
import { AsyncReportSearchResponse, AsyncReportDetail } from '../models/report.models';
import { of, throwError } from 'rxjs';
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

describe('AsyncReportState', () => {
  let store: Store;
  let reportServiceSpy: jasmine.SpyObj<ReportService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const initialState: AsyncReportStateModel = {
    searchRequest: null,
    searchResults: null,
    selectedReport: null,
    loading: false,
    downloading: false,
    error: null,
    message: null
  };

  beforeEach(() => {
    const spy = jasmine.createSpyObj('ReportService', [
      'searchAsyncReports',
      'getAsyncReportDetail',
      'downloadAsyncReport',
      'cancelAsyncReport',
      'deleteAsyncReport'
    ]);

    const routerMock = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [NgxsModule.forRoot([AsyncReportState])],
      providers: [
        { provide: ReportService, useValue: spy },
        { provide: Router, useValue: routerMock }
      ]
    });

    store = TestBed.inject(Store);
    reportServiceSpy = TestBed.inject(ReportService) as jasmine.SpyObj<ReportService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    store.reset(initialState);
  });

  it('should create an instance', () => {
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);
    expect(new AsyncReportState(reportServiceSpy, routerMock)).toBeTruthy();
  });

  describe('Selectors', () => {
    it('should select search results', () => {
      const mockResults: AsyncReportSearchResponse = {
        report: [],
        totale_elementi: 0,
        totale_pagine: 0,
        pagina_corrente: 0,
        dimensione_pagina: 20
      };

      store.reset({
        ...initialState,
        searchResults: mockResults
      });

      const results = store.selectSnapshot(AsyncReportState.getSearchResults);
      expect(results).toEqual(mockResults);
    });

    it('should select selected report', () => {
      const mockReport: AsyncReportDetail = {
        report_id: '123',
        formato: 'pdf',
        file_nome: 'test.pdf',
        stato: 'COMPLETED',
        data_creazione: '2025-01-01T10:00:00Z'
      };

      store.reset({
        ...initialState,
        selectedReport: mockReport
      });

      const report = store.selectSnapshot(AsyncReportState.getSelectedReport);
      expect(report).toEqual(mockReport);
    });

    it('should select loading state', () => {
      store.reset({
        ...initialState,
        loading: true
      });

      const loading = store.selectSnapshot(AsyncReportState.isLoading);
      expect(loading).toBe(true);
    });

    it('should select downloading state', () => {
      store.reset({
        ...initialState,
        downloading: true
      });

      const downloading = store.selectSnapshot(AsyncReportState.isDownloading);
      expect(downloading).toBe(true);
    });

    it('should select error', () => {
      const error = { message: 'Test error' };
      store.reset({
        ...initialState,
        error
      });

      const selectedError = store.selectSnapshot(AsyncReportState.getError);
      expect(selectedError).toEqual(error);
    });

    it('should select message', () => {
      const message = {
        type: 'success' as const,
        title: 'Success',
        text: 'Operation completed'
      };
      store.reset({
        ...initialState,
        message
      });

      const selectedMessage = store.selectSnapshot(AsyncReportState.getMessage);
      expect(selectedMessage).toEqual(message);
    });

    it('should select search request', () => {
      const request: AsyncReportSearchRequest = {
        data_elaborazione_da: '2025-01-01',
        data_elaborazione_a: '2025-01-31'
      };
      store.reset({
        ...initialState,
        searchRequest: request
      });

      const selectedRequest = store.selectSnapshot(AsyncReportState.getSearchRequest);
      expect(selectedRequest).toEqual(request);
    });
  });

  describe('Actions', () => {
    it('should handle ClearAsyncReportState', () => {
      store.dispatch(new ClearAsyncReportState());

      const state = store.selectSnapshot((state) => state.asyncReport);
      expect(state).toEqual(initialState);
    });

    it('should handle SetAsyncReportMessage', () => {
      const message = {
        type: 'error' as const,
        title: 'Error',
        text: 'Something went wrong'
      };

      store.dispatch(new SetAsyncReportMessage(message));

      const stateMessage = store.selectSnapshot(AsyncReportState.getMessage);
      expect(stateMessage).toEqual(message);
    });

    it('should handle ClearAsyncReportMessage', () => {
      store.dispatch(new SetAsyncReportMessage({
        type: 'success' as const,
        title: 'Success',
        text: 'Done'
      }));

      store.dispatch(new ClearAsyncReportMessage());

      const stateMessage = store.selectSnapshot(AsyncReportState.getMessage);
      expect(stateMessage).toBeNull();
    });

    it('should handle SearchAsyncReports success', () => {
      const request: AsyncReportSearchRequest = {
        data_elaborazione_da: '2025-01-01',
        data_elaborazione_a: '2025-01-31'
      };

      const mockResponse: AsyncReportSearchResponse = {
        report: [
          {
            report_id: '1',
            formato: 'pdf',
            file_nome: 'test.pdf',
            stato: 'COMPLETED',
            data_creazione: '2025-01-15T10:00:00Z'
          }
        ],
        totale_elementi: 1,
        totale_pagine: 1,
        pagina_corrente: 0,
        dimensione_pagina: 20
      };

      reportServiceSpy.searchAsyncReports.and.returnValue(of(mockResponse));

      store.dispatch(new SearchAsyncReports(request));

      const state = store.selectSnapshot((state) => state.asyncReport);
      expect(state.loading).toBe(false);
      expect(state.searchResults).toEqual(mockResponse);
      expect(state.searchRequest).toEqual(request);
    });

    it('should handle SearchAsyncReports error', () => {
      const request: AsyncReportSearchRequest = {};
      reportServiceSpy.searchAsyncReports.and.returnValue(throwError(() => new Error('Search failed')));

      store.dispatch(new SearchAsyncReports(request));

      const state = store.selectSnapshot((state) => state.asyncReport);
      expect(state.loading).toBe(false);
      expect(state.error).toBeTruthy();
      expect(state.message).toBeTruthy();
      expect(state.message?.type).toBe('error');
    });
  });
});

// Type definition for AsyncReportSearchRequest
interface AsyncReportSearchRequest {
  data_elaborazione_da?: string;
  data_elaborazione_a?: string;
  stato?: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'ERROR';
  tipo_report?: 'ACCRUAL-001' | 'ACCRUAL-002' | 'ACCRUAL-003';
  formato?: 'pdf' | 'xlsx';
  pagina?: number;
  dimensione_pagina?: number;
  ordinamento?: string;
  direzione_ordinamento?: 'ASC' | 'DESC';
}
