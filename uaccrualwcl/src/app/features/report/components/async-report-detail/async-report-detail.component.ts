/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngxs/store';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';

// Modelli e Actions
import { AsyncReportDetail, AsyncReportStatusLabels, AsyncReportStatusSeverity } from '../../models/report.models';
import { DownloadAsyncReport, ClearAsyncReportMessage, LoadAsyncReportDetail } from '../../state/async-report.actions';
import { AsyncReportState } from '../../state/async-report.state';
import { getReportNameFromRptdesign } from '../../constants/report.constants';

@Component({
  selector: 'app-async-report-detail',
  standalone: true,
  imports: [
    CommonModule,
  ],
  templateUrl: './async-report-detail.component.html',
  styleUrls: ['./async-report-detail.component.scss']
})
export class AsyncReportDetailComponent implements OnInit, OnDestroy {

  report: AsyncReportDetail | null = null;
  loading = false;
  downloading = false;

  private subscriptions: Subscription[] = [];

  // Enum per template
  AsyncReportStatusLabels = AsyncReportStatusLabels;
  AsyncReportStatusSeverity = AsyncReportStatusSeverity;

  get reportNome(): string {
    return getReportNameFromRptdesign(this.report?.rptdesign_file_name || '');
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private store: Store
  ) { }

  ngOnInit(): void {
    this.subscribeToState();
    this.loadFromRoute();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private subscribeToState(): void {
    // Dettaglio report
    this.subscriptions.push(
      this.store.select(AsyncReportState.getSelectedReport).subscribe(detail => {
        this.report = detail;
      })
    );

    // Loading
    this.subscriptions.push(
      this.store.select(AsyncReportState.isLoading).subscribe(loading => {
        this.loading = loading;
      })
    );

    // Downloading
    this.subscriptions.push(
      this.store.select(AsyncReportState.isDownloading).subscribe(downloading => {
        this.downloading = downloading;
      })
    );

    // Message
    this.subscriptions.push(
      this.store.select(AsyncReportState.getMessage).subscribe(message => {
        if (message) {
          // Qui si potrebbe mostrare un toast
          console.log('Message:', message);
          this.store.dispatch(new ClearAsyncReportMessage());
        }
      })
    );
  }

  private loadFromRoute(): void {
    const reportId = this.route.snapshot.paramMap.get('id');
    if (reportId && !this.report) {
      // Carica il dettaglio se non è già presente
      this.store.dispatch(new LoadAsyncReportDetail(reportId));
    }
  }

  /**
   * Scarica il report
   */
  onDownload(): void {
    if (this.report?.report_id) {
      const formato = this.report.formato || 'pdf';
      this.store.dispatch(new DownloadAsyncReport(this.report.report_id, formato));
    }
  }

  /**
   * Torna alla lista
   */
  onBack(): void {
    this.router.navigate(['/report/search']);
  }

  /**
   * Formatta data
   */
  formatDate(dateString: string): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('it-IT', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Ottiene la mappa nome → valore dei parametri parsata
   */
  private parseParametri(): Record<string, any> | null {
    if (!this.report?.parametri) return null;
    try {
      return typeof this.report.parametri === 'string'
        ? JSON.parse(this.report.parametri)
        : this.report.parametri;
    } catch {
      return null;
    }
  }

  /**
   * Ottieni chiavi parametri (solo quelli VISIBILI, con etichetta nel backend)
   * Usa l'ordine del BIRT template preservato in etichette_parametri (LinkedHashMap)
   */
  getParamKeys(): string[] {
    const parsed = this.parseParametri();
    if (!parsed) return [];

    const etichette = this.report?.etichette_parametri;
    if (etichette) {
      // Ordine del BIRT template, solo parametri con valore presente
      return Object.keys(etichette).filter(key => key in parsed);
    }

    // Fallback: se non ci sono etichette, mostra tutti
    return Object.keys(parsed);
  }

  /**
   * Ottieni etichetta leggibile per un parametro
   */
  etichettaParametro(key: string): string {
    return this.report?.etichette_parametri?.[key] || key;
  }

  /**
   * Ottieni valore parametro formattato
   */
  getParamValue(key: string): any {
    const parsed = this.parseParametri();
    return parsed?.[key] ?? null;
  }

  /**
   * Formatta il valore di un parametro: riconosce date/timestamp e le rende leggibili
   */
  formatParamValue(key: string, value: any): string {
    if (value == null || value === '') return '-';

    // Se è un timestamp numerico (epoch ms) e il parametro sembra una data
    if (typeof value === 'number' && this.isDateKey(key)) {
      return this.formatTimestamp(value);
    }

    // Se è una stringa potenziale data
    if (typeof value === 'string') {
      // ISO date string (yyyy-MM-dd)
      if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        return this.formatDate(value);
      }
      // ISO datetime string
      if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(value)) {
        return this.formatDate(value);
      }
    }

    return String(value);
  }

  /**
   * Rileva se un nome chiave sembra riferirsi a una data
   */
  private isDateKey(key: string): boolean {
    const lower = key.toLowerCase();
    return lower.includes('data') || lower.includes('date')
      || lower.startsWith('da_') || lower.startsWith('a_')
      || lower.endsWith('_da') || lower.endsWith('_a');
  }

  /**
   * Formatta un timestamp numerico (epoch ms) in data locale
   */
  private formatTimestamp(ts: number): string {
    const date = new Date(ts);
    return date.toLocaleDateString('it-IT', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  /**
   * Controlla se scaricabile
   */
  isDownloadable(): boolean {
    return (this.report?.stato as any) === 'COMPLETED';
  }

  /**
   * Ottiene la classe CSS per lo stato (sostituisce p-tag severity)
   */
  getStatusClass(stato: string): string {
    const severity = (AsyncReportStatusSeverity as any)[stato] || 'info';
    return this._severityToClass(severity);
  }

  /**
   * Ottiene la classe CSS per l'esito (sostituisce p-tag severity)
   */
  getEsitoClass(esito: string): string {
    return this._severityToClass(this.getEsitoSeverity(esito));
  }

  private _severityToClass(severity: string): string {
    switch (severity) {
      case 'success': return 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300';
      case 'warn':    return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300';
      case 'danger':  return 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300';
      default:        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300';
    }
  }

  /**
   * Ottiene la severità per l'esito
   */
  getEsitoSeverity(esito: string): 'success' | 'info' | 'warn' | 'danger' {
    switch (esito) {
      case 'SUCCESS':
        return 'success';
      case 'WARNING':
        return 'warn';
      case 'ERROR':
        return 'danger';
      default:
        return 'info';
    }
  }

  /**
   * Ottiene la label per uno stato
   */
  getStatusLabel(stato: string): string {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return (AsyncReportStatusLabels as any)[stato] || stato;
  }

  /**
   * Ottiene la severità per uno stato
   */
  getSeverity(stato: string): 'success' | 'info' | 'warn' | 'danger' {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return (AsyncReportStatusSeverity as any)[stato] || 'info';
  }
}
