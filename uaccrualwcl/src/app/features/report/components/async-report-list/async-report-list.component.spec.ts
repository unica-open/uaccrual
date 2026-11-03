/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Store } from '@ngxs/store';
import { Router } from '@angular/router';

import { AsyncReportListComponent } from './async-report-list.component';
import { LoadAsyncReportDetail, DownloadAsyncReport, ChangeAsyncReportPage } from '../../state/async-report.actions';
import { AsyncReportSearchResponse } from '../../models/report.models';

describe('AsyncReportListComponent', () => {
  let component: AsyncReportListComponent;
  let fixture: ComponentFixture<AsyncReportListComponent>;
  let storeSpy: jasmine.SpyObj<Store>;
  let routerSpy: jasmine.SpyObj<Router>;

  const mockSearchResults: AsyncReportSearchResponse = {
    report: [
      {
        report_id: '1',
        formato: 'pdf',
        file_nome: 'test1.pdf',
        stato: 'COMPLETED',
        data_creazione: '2025-01-15T10:00:00Z'
      },
      {
        report_id: '2',
        formato: 'xlsx',
        file_nome: 'test2.xlsx',
        stato: 'PROCESSING',
        data_creazione: '2025-01-16T11:00:00Z',
        progress_percentage: 50
      }
    ],
    totale_elementi: 2,
    totale_pagine: 1,
    pagina_corrente: 0,
    dimensione_pagina: 20
  };

  beforeEach(async () => {
    const storeMock = jasmine.createSpyObj('Store', ['dispatch', 'select']);
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        AsyncReportListComponent
      ],
      providers: [
        { provide: Store, useValue: storeMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    storeSpy = TestBed.inject(Store) as jasmine.SpyObj<Store>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AsyncReportListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle results when available via state', () => {
    // Il componente riceve i dati dallo store tramite select
    // Qui verifichiamo che le proprietà siano inizializzate correttamente
    expect(component.reports).toEqual([]);
    expect(component.totalElements).toBe(0);
    expect(component.currentPage).toBe(0);
    expect(component.pageSize).toBe(20);
  });

  it('should handle empty results', () => {
    expect(component.reports).toEqual([]);
    expect(component.totalElements).toBe(0);
  });

  it('should dispatch LoadAsyncReportDetail on view detail', () => {
    const reportId = '1';

    component.onViewDetail(reportId);

    expect(storeSpy.dispatch).toHaveBeenCalledWith(jasmine.any(LoadAsyncReportDetail));
  });

  it('should dispatch DownloadAsyncReport on download', () => {
    const reportId = '1';

    component.onDownload(reportId);

    expect(storeSpy.dispatch).toHaveBeenCalledWith(jasmine.any(DownloadAsyncReport));
  });

  it('should dispatch ChangeAsyncReportPage on page change', () => {
    const event = { first: 20, rows: 20 };

    component.onPageChange(event);

    expect(storeSpy.dispatch).toHaveBeenCalledWith(jasmine.any(ChangeAsyncReportPage));
  });

  it('should check if report is downloadable', () => {
    const report: any = {
      report_id: '1',
      stato: 'COMPLETED'
    };

    expect(component.isDownloadable(report)).toBe(true);
  });

  it('should check if report is cancelable', () => {
    const pendingReport: any = {
      report_id: '1',
      stato: 'PENDING'
    };

    const processingReport: any = {
      report_id: '2',
      stato: 'PROCESSING'
    };

    const completedReport: any = {
      report_id: '3',
      stato: 'COMPLETED'
    };

    expect(component.isCancelable(pendingReport)).toBe(true);
    expect(component.isCancelable(processingReport)).toBe(true);
    expect(component.isCancelable(completedReport)).toBe(false);
  });

  it('should calculate total pages', () => {
    component.totalElements = 100;
    component.pageSize = 20;

    expect(component.totalPages).toBe(5);
  });

  it('should get pages for paginator', () => {
    component.totalElements = 100;
    component.pageSize = 20;

    const pages = component.getPages();
    expect(pages).toBeDefined();
    expect(pages.length).toBeGreaterThan(0);
  });
});
