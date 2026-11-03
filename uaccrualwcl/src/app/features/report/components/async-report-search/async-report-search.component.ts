/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Store } from '@ngxs/store';
import { CommonModule } from '@angular/common';

// Modelli e Actions
import { AsyncReportSearchRequest, AsyncReportStatus, AsyncReportType, AsyncReportStatusLabels, AsyncReportStatusSeverity } from '../../models/report.models';
import { SearchAsyncReports, ClearAsyncReportState, ClearAsyncReportMessage } from '../../state/async-report.actions';
import { AsyncReportState } from '../../state/async-report.state';
import { AsyncReportListComponent } from '../async-report-list/async-report-list.component';

const searchAnimations = [
  trigger('collapseExpand', [
    transition(':enter', [
      style({ height: '0px', opacity: 0, overflow: 'hidden' }),
      animate('300ms ease-out', style({ height: '*', opacity: 1 }))
    ]),
    transition(':leave', [
      style({ overflow: 'hidden' }),
      animate('250ms ease-in', style({ height: '0px', opacity: 0 }))
    ])
  ]),
  trigger('fadeSlideIn', [
    transition(':enter', [
      style({ opacity: 0, transform: 'translateY(-10px)' }),
      animate('350ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
    ]),
    transition(':leave', [
      animate('200ms ease-in', style({ opacity: 0 }))
    ])
  ])
];

@Component({
  selector: 'app-async-report-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AsyncReportListComponent
  ],
  templateUrl: './async-report-search.component.html',
  styleUrls: ['./async-report-search.component.scss'],
  animations: searchAnimations
})
export class AsyncReportSearchComponent implements OnInit {

  searchForm!: FormGroup;
  loading = false;
  message: { type: string; title: string; text: string } | null = null;
  appliedFilterParams: {label: string, value: string}[] = [];
  searchDone = false;
  filtersCollapsed = false;
  searching = false;

  // Opzioni dropdown
  statiOptions: { label: string; value: string }[] = [];
  tipoReportOptions: { label: string; value: string }[] = [];
  formatoOptions: { label: string; value: string }[] = [];

  // Enum per template
  AsyncReportStatusSeverity = AsyncReportStatusSeverity;

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.initStatiOptions();
    this.initTipoReportOptions();
    this.initFormatoOptions();
    this.subscribeToState();

    const prevUrl = this.router.lastSuccessfulNavigation?.previousNavigation?.finalUrl?.toString() ?? '';
    const results = this.store.selectSnapshot(AsyncReportState.getSearchResults);
    if (prevUrl.startsWith('/report') && results && results.totale_elementi > 0) {
      this.searchDone = true;
      this.filtersCollapsed = true;
    }
  }

  private initForm(): void {
    this.searchForm = this.fb.group({
      dateFrom: [null],
      dateTo: [null],
      stato: [null],
      tipoReport: [null],
      formato: [null]
    });
  }

  private initStatiOptions(): void {
    this.statiOptions = (Object.keys(AsyncReportStatusLabels) as AsyncReportStatus[]).map(value => ({
      label: AsyncReportStatusLabels[value],
      value
    }));
  }

  private initTipoReportOptions(): void {
    this.tipoReportOptions = [
      { label: 'Mastrino', value: 'ACCRUAL-001' },
      { label: 'Libro Giornale', value: 'ACCRUAL-002' },
      { label: 'Bilancio', value: 'ACCRUAL-003' }
    ];
  }

  private initFormatoOptions(): void {
    this.formatoOptions = [
      { label: 'PDF', value: 'pdf' },
      { label: 'Excel (XLSX)', value: 'xlsx' }
    ];
  }

  private subscribeToState(): void {
    this.store.select(AsyncReportState.isLoading).subscribe(loading => {
      this.loading = loading;
    });

    // Messaggi
    this.store.select(AsyncReportState.getMessage).subscribe(message => {
      this.message = message;
    });
  }

  private buildFilterParams(): {label: string, value: string}[] {
    const params: {label: string, value: string}[] = [];
    const form = this.searchForm.value;
    if (form.dateFrom) params.push({ label: 'Data Da', value: form.dateFrom });
    if (form.dateTo) params.push({ label: 'Data A', value: form.dateTo });
    if (form.stato) {
      const found = this.statiOptions.find(s => s.value === form.stato);
      params.push({ label: 'Stato', value: found ? found.label : form.stato });
    }
    if (form.tipoReport) {
      const found = this.tipoReportOptions.find(t => t.value === form.tipoReport);
      params.push({ label: 'Tipo Report', value: found ? found.label : form.tipoReport });
    }
    if (form.formato) {
      const found = this.formatoOptions.find(f => f.value === form.formato);
      params.push({ label: 'Formato', value: found ? found.label : form.formato });
    }
    return params;
  }

  toggleFilters(): void {
    this.filtersCollapsed = !this.filtersCollapsed;
  }

  /**
   * Esegue la ricerca
   */
  onSearch(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    if (!this.dateRangeValid) {
      this.searchForm.get('dateTo')?.setErrors({ 'dateRangeInvalid': true });
      return;
    }

    const formValue = this.searchForm.value;
    const filter: AsyncReportSearchRequest = {
      data_elaborazione_da: formValue.dateFrom || undefined,
      data_elaborazione_a: formValue.dateTo || undefined,
      stato: formValue.stato || undefined,
      tipo_report: formValue.tipoReport || undefined,
      formato: formValue.formato || undefined,
      pagina: 0,
      dimensione_pagina: 20,
      ordinamento: 'completedAt',
      direzione_ordinamento: 'DESC'
    };

    this.searching = true;
    this.filtersCollapsed = true;
    this.store.dispatch(new SearchAsyncReports(filter)).subscribe(() => {
      this.searching = false;
      const results = this.store.selectSnapshot(AsyncReportState.getSearchResults);
      if (results && results.totale_elementi > 0) {
        this.searchDone = true;
        this.appliedFilterParams = this.buildFilterParams();
      } else {
        this.searchDone = false;
        this.filtersCollapsed = false;
        this.appliedFilterParams = [];
      }
    });
  }

  /**
   * Resetta i filtri
   */
  onReset(): void {
    this.searchForm.reset();
    this.store.dispatch(new ClearAsyncReportState());
    this.searching = false;
    this.searchDone = false;
    this.filtersCollapsed = false;
    this.appliedFilterParams = [];
  }

  /**
   * Chiude il messaggio corrente
   */
  onDismissMessage(): void {
    this.store.dispatch(new ClearAsyncReportMessage());
  }

  onBack(): void {
    if (this.searchDone) {
      this.searchForm.reset();
      this.store.dispatch(new ClearAsyncReportState());
      this.searching = false;
      this.searchDone = false;
      this.filtersCollapsed = false;
      this.appliedFilterParams = [];
    } else {
      this.router.navigate(['/home']);
    }
  }

  /**
   * Validazione custom: dateTo >= dateFrom
   */
  get dateRangeValid(): boolean {
    const dateFrom = this.searchForm.get('dateFrom')?.value;
    const dateTo = this.searchForm.get('dateTo')?.value;

    if (dateFrom && dateTo) {
      return new Date(dateTo) >= new Date(dateFrom);
    }
    return true;
  }

  /**
   * Ottiene la severità per uno stato
   */
  getSeverity(stato: string): 'success' | 'info' | 'warn' | 'danger' {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return (AsyncReportStatusSeverity as any)[stato] || 'info';
  }
}
