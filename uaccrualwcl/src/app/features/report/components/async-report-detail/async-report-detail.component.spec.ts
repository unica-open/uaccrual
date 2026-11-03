/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Store } from '@ngxs/store';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { AsyncReportDetailComponent } from './async-report-detail.component';
import { DownloadAsyncReport, ClearAsyncReportMessage } from '../../state/async-report.actions';
import { AsyncReportDetail as AsyncReportDetailModel } from '../../models/report.models';

describe('AsyncReportDetailComponent', () => {
  let component: AsyncReportDetailComponent;
  let fixture: ComponentFixture<AsyncReportDetailComponent>;
  let storeSpy: jasmine.SpyObj<Store>;

  const mockReportDetail: AsyncReportDetailModel = {
    report_id: '550e8400-e29b-41d4-a716-446655440000',
    formato: 'pdf',
    file_nome: 'test-report.pdf',
    stato: 'COMPLETED',
    data_creazione: '2025-01-15T10:00:00Z',
    data_completamento: '2025-01-15T10:05:00Z',
    dimensione_file: 1048576,
    progress_percentage: 100,
    parametri: '{"dataInizio": "2025-01-01", "dataFine": "2025-01-31"}',
    esiti: [
      {
        esitoId: '1',
        reportId: '550e8400-e29b-41d4-a716-446655440000',
        esito: 'SUCCESS',
        messaggio: 'Report generato con successo',
        elaboratoAt: '2025-01-15T10:05:00Z'
      }
    ]
  };

  beforeEach(async () => {
    const storeMock = jasmine.createSpyObj('Store', ['dispatch', 'select']);
    const activatedRouteMock = {
      snapshot: {
        paramMap: {
          get: () => '550e8400-e29b-41d4-a716-446655440000'
        }
      },
      params: of({ id: '550e8400-e29b-41d4-a716-446655440000' })
    };

    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        AsyncReportDetailComponent
      ],
      providers: [
        { provide: Store, useValue: storeMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ]
    }).compileComponents();

    storeSpy = TestBed.inject(Store) as jasmine.SpyObj<Store>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AsyncReportDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set report', () => {
    component.report = mockReportDetail;
    fixture.detectChanges();

    expect(component.report).toEqual(mockReportDetail);
  });

  it('should dispatch DownloadAsyncReport on download', () => {
    component.report = mockReportDetail;
    component.onDownload();

    expect(storeSpy.dispatch).toHaveBeenCalledWith(jasmine.any(DownloadAsyncReport));
  });

  it('should handle null report', () => {
    component.report = null;
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should format date correctly', () => {
    const dateStr = '2025-01-15T10:00:00Z';
    const formatted = component.formatDate(dateStr);

    expect(formatted).toBeTruthy();
  });

  it('should return param keys', () => {
    component.report = mockReportDetail;
    const keys = component.getParamKeys();

    expect(keys).toContain('dataInizio');
    expect(keys).toContain('dataFine');
  });

  it('should return param value', () => {
    component.report = mockReportDetail;
    const value = component.getParamValue('dataInizio');

    expect(value).toBe('2025-01-01');
  });

  it('should check if downloadable', () => {
    component.report = mockReportDetail;
    expect(component.isDownloadable()).toBe(true);
  });

  it('should check if deletable', () => {
    component.report = mockReportDetail;
    expect(component.isDeletable()).toBe(true);
  });

  it('should get status class', () => {
    const statusClass = component.getStatusClass('COMPLETED');
    expect(statusClass).toContain('bg-green');
  });

  it('should get esito class', () => {
    const esitoClass = component.getEsitoClass('SUCCESS');
    expect(esitoClass).toContain('bg-green');
  });

  it('should get status label', () => {
    const label = component.getStatusLabel('COMPLETED');
    expect(label).toBe('Completato');
  });

  it('should get severity', () => {
    const severity = component.getSeverity('COMPLETED');
    expect(severity).toBe('success');
  });
});
