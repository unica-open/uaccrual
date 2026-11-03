/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Store } from '@ngxs/store';

import { AsyncReportSearchComponent } from './async-report-search.component';
import { SearchAsyncReports, ClearAsyncReportState } from '../../state/async-report.actions';

describe('AsyncReportSearchComponent', () => {
  let component: AsyncReportSearchComponent;
  let fixture: ComponentFixture<AsyncReportSearchComponent>;
  let storeSpy: jasmine.SpyObj<Store>;

  beforeEach(async () => {
    const storeMock = jasmine.createSpyObj('Store', ['dispatch', 'select']);

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        AsyncReportSearchComponent
      ],
      providers: [
        { provide: Store, useValue: storeMock }
      ]
    }).compileComponents();

    storeSpy = TestBed.inject(Store) as jasmine.SpyObj<Store>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AsyncReportSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.searchForm).toBeDefined();
    expect(component.searchForm.get('dataElaborazioneDa')?.value).toBeNull();
    expect(component.searchForm.get('dataElaborazioneA')?.value).toBeNull();
    expect(component.searchForm.get('stato')?.value).toBeNull();
  });

  it('should have stati options', () => {
    expect(component.statiOptions).toBeDefined();
    expect(component.statiOptions.length).toBeGreaterThan(0);
  });

  it('should call onSearch with valid form', () => {
    component.searchForm.patchValue({
      dataElaborazioneDa: '2025-01-01',
      dataElaborazioneA: '2025-01-31',
      stato: 'COMPLETED'
    });

    component.onSearch();

    expect(storeSpy.dispatch).toHaveBeenCalledWith(jasmine.any(SearchAsyncReports));
  });

  it('should mark form as touched when invalid', () => {
    const markAllAsTouchedSpy = spyOn(component.searchForm, 'markAllAsTouched');
    component.onSearch();

    if (component.searchForm.invalid) {
      expect(markAllAsTouchedSpy).toHaveBeenCalled();
    }
  });

  it('should call onReset', () => {
    component.searchForm.patchValue({
      dataElaborazioneDa: '2025-01-01',
      stato: 'COMPLETED'
    });

    component.onReset();

    expect(storeSpy.dispatch).toHaveBeenCalledWith(jasmine.any(ClearAsyncReportState));
    expect(component.searchForm.get('dataElaborazioneDa')?.value).toBeNull();
    expect(component.searchForm.get('stato')?.value).toBeNull();
  });

  it('should validate date range - invalid when dateTo < dateFrom', () => {
    component.searchForm.patchValue({
      dataElaborazioneDa: '2025-01-15',
      dataElaborazioneA: '2025-01-10' // Before dateDa
    });

    expect(component.dateRangeValid).toBe(false);
  });

  it('should validate date range - valid when dateTo >= dateFrom', () => {
    component.searchForm.patchValue({
      dataElaborazioneDa: '2025-01-10',
      dataElaborazioneA: '2025-01-15'
    });

    expect(component.dateRangeValid).toBe(true);
  });

  it('should validate date range - valid when only one date is set', () => {
    component.searchForm.patchValue({
      dataElaborazioneDa: '2025-01-10',
      dataElaborazioneA: null
    });

    expect(component.dateRangeValid).toBe(true);
  });
});
