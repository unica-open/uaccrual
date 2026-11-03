/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject, Subscription, interval } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';
import { ReportService } from './report.service';
import { AsyncReportDetail, AsyncReportStatus } from '../models/report.models';

/**
 * Servizio per il polling automatico dello stato dei report asincroni
 */
@Injectable({
  providedIn: 'root'
})
export class AsyncReportPollingService implements OnDestroy {

  private pollingSubject: Subject<AsyncReportDetail> = new Subject();
  private pollingSubscription: Subscription | null = null;

  constructor(private reportService: ReportService) { }

  /**
   * Avvia il polling per un report specifico
   * @param reportId ID del report da monitorare
   * @param intervalMs Intervallo di polling in millisecondi (default: 5000)
   * @returns Observable con lo stato aggiornato del report
   */
  startPolling(reportId: string, intervalMs: number = 5000): Observable<AsyncReportDetail> {
    // Cleanup eventuale polling precedente
    this.stopPolling();

    this.pollingSubscription = interval(intervalMs)
      .pipe(
        switchMap(() => this.reportService.getAsyncReportDetail(reportId)),
        takeWhile((detail: AsyncReportDetail) =>
          detail.stato === 'PROCESSING' || detail.stato === 'PENDING',
          true // Includi l'ultimo valore quando la condizione diventa falsa
        )
      )
      .subscribe({
        next: (detail: AsyncReportDetail) => {
          this.pollingSubject.next(detail);
        },
        error: (err) => {
          this.pollingSubject.error(err);
          this.stopPolling();
        }
      });

    return this.pollingSubject.asObservable();
  }

  /**
   * Ferma il polling in corso
   */
  stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
  }

  /**
   * Pulisce le risorse quando il servizio viene distrutto
   */
  ngOnDestroy(): void {
    this.stopPolling();
  }
}
