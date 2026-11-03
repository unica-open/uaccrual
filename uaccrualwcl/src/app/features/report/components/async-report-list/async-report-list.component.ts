/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngxs/store';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// Modelli e Actions
import { AsyncReportSummary, AsyncReportStatus, AsyncReportStatusLabels, AsyncReportStatusSeverity } from '../../models/report.models';
import { LoadAsyncReportDetail, DownloadAsyncReport, ChangeAsyncReportPage, CancelAsyncReport } from '../../state/async-report.actions';
import { AsyncReportState } from '../../state/async-report.state';
import { getReportNameFromRptdesign, REPORT_NAME_MAP } from '../../constants/report.constants';

@Component({
  selector: 'app-async-report-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ],
  templateUrl: './async-report-list.component.html',
  styleUrls: ['./async-report-list.component.scss']
})
export class AsyncReportListComponent implements OnInit, OnDestroy {

  reports: AsyncReportSummary[] = [];
  loading = false;
  totalElements = 0;
  currentPage = 0;
  pageSize = 20;
  pageSizes = [10, 20, 50, 100];
  currentFilters: any = null;

  private subscriptions: Subscription[] = [];

  // Per il template
  Math = Math;
  readonly reportNameMap = REPORT_NAME_MAP;
  readonly formatoLabelMap: Record<string, string> = {
    'pdf': 'PDF',
    'xlsx': 'Excel (XLSX)'
  };

  // Getter per totalPages
  get totalPages(): number {
    return Math.ceil(this.totalElements / this.pageSize);
  }

  constructor(
    private store: Store,
    public router: Router
  ) { }

  ngOnInit(): void {
    this.subscribeToState();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private subscribeToState(): void {
    // Risultati ricerca
    this.subscriptions.push(
      this.store.select(AsyncReportState.getSearchResults).subscribe(results => {
        if (results) {
          this.reports = results.report || [];
          this.totalElements = results.totale_elementi || 0;
          this.currentPage = results.pagina_corrente || 0;
          this.pageSize = results.dimensione_pagina || 20;
        } else {
          this.reports = [];
        }
      })
    );

    // Loading
    this.subscriptions.push(
      this.store.select(AsyncReportState.isLoading).subscribe(loading => {
        this.loading = loading;
      })
    );

    // Filtri correnti
    this.subscriptions.push(
      this.store.select(AsyncReportState.getCurrentFilters).subscribe(filters => {
        this.currentFilters = filters;
      })
    );
  }

  getReportName(report: AsyncReportSummary): string {
    return getReportNameFromRptdesign(report.rptdesign_file_name || '');
  }

  getStatusLabel(stato: string): string {
    return (AsyncReportStatusLabels as Record<string, string>)[stato] || stato;
  }

  /**
   * Naviga al dettaglio report
   */
  onViewDetail(reportId: string): void {
    this.store.dispatch(new LoadAsyncReportDetail(reportId));
  }

  /**
   * Scarica il report
   */
  onDownload(reportId: string, formato: string = 'pdf'): void {
    this.store.dispatch(new DownloadAsyncReport(reportId, formato));
  }

  /**
   * Annulla il report
   */
  onCancel(reportId: string): void {
    if (confirm('Sei sicuro di voler annullare l\'elaborazione di questo report?')) {
      this.store.dispatch(new CancelAsyncReport(reportId));
    }
  }

  /**
   * Cambia pagina
   */
  onPageChange(event: any): void {
    this.store.dispatch(new ChangeAsyncReportPage(event.first, event.rows));
  }

  /**
   * Controlla se report è scaricabile
   */
  isDownloadable(report: AsyncReportSummary): boolean {
    return report.stato === 'COMPLETED';
  }

  /**
   * Controlla se report è annullabile
   */
  isCancelable(report: AsyncReportSummary): boolean {
    return (report.stato === 'PENDING' || report.stato === 'PROCESSING');
  }

  /**
   * Naviga indietro
   */
  onBack(): void {
    this.router.navigate(['/report/search']);
  }

  /**
   * Ottiene i numeri di pagina per il paginator
   */
  getPages(): number[] {
    const totalPages = Math.ceil(this.totalElements / this.pageSize);
    const pages: number[] = [];
    const maxVisible = 5;
    
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);
    
    if (end - start < maxVisible - 1) {
      start = Math.max(0, end - maxVisible + 1);
    }
    
    if (start > 0) {
      pages.push(0);
      if (start > 1) {
        pages.push(-1);
      }
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    if (end < totalPages - 1) {
      if (end < totalPages - 2) {
        pages.push(-1);
      }
      pages.push(totalPages - 1);
    }
    
    return pages;
  }
}
