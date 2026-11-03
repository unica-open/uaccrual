/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError, Subscription, timer } from 'rxjs';
import { catchError, tap, switchMap, takeWhile } from 'rxjs/operators';
import { DatePipe } from '@angular/common';
import { NotificationService } from './notification.service';
import { environment } from '../../../environments/environment';
import { TipoFile, FlussoUploadRequest, FlussoUploadResponse, FlussoSearchFilter, PagedFlussoSearchResult, FlussoSearchResult, Fonte, TipoFlusso, TipoFileApi, FlussoDetail, ElaboraFlussoResponseDTO, ProcessStatusResponse } from '../../features/flusso/model/flusso.model';

@Injectable({
  providedIn: 'root'
})
export class FlussoService {

  private accrualsbsrBaseUrl = environment.services.accrualLegacyApiUrl;
  private accrualsapiBaseUrl = environment.services.accrualNewApiUrl;

  private pendingConfirmationSubject = new BehaviorSubject<ElaboraFlussoResponseDTO | null>(null);
  pendingConfirmation$ = this.pendingConfirmationSubject.asObservable();

  private lastFiltersSubject = new BehaviorSubject<FlussoSearchFilter | null>(null);
  get lastFiltersValue(): FlussoSearchFilter | null { return this.lastFiltersSubject.getValue(); }

  saveFilters(filters: FlussoSearchFilter | null): void {
    this.lastFiltersSubject.next(filters);
  }

  private elaborationProgressSubject = new BehaviorSubject<number | null>(null);
  elaborationProgress$ = this.elaborationProgressSubject.asObservable();

  private activeJobStatusSubject = new BehaviorSubject<ProcessStatusResponse | null>(null);
  activeJobStatus$ = this.activeJobStatusSubject.asObservable();

  private statusPollSub: Subscription | null = null;
  private simIntervalId: ReturnType<typeof setInterval> | null = null;
  private progressCeiling = 0;

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService,
    private datePipe: DatePipe
  ) { }

  // Get the appropriate base URL based on the endpoint
  private getBaseUrl(endpoint: string): string {
    // CDU-011: Use new API ONLY for upload and elaboration
    if (endpoint.includes('upload') || endpoint.includes('elabora')) {
      return this.accrualsapiBaseUrl || this.accrualsbsrBaseUrl;
    }
    // Use legacy API for all other endpoints (search, download, etc.)
    return this.accrualsbsrBaseUrl;
  }

  uploadFlusso(flussoRequest: FlussoUploadRequest): Observable<FlussoUploadResponse> {
    const formData = new FormData();
    formData.append('codice', flussoRequest.codice);
    formData.append('tipoFile', flussoRequest.tipoFile);
    if (flussoRequest.note) {
      formData.append('note', flussoRequest.note);
    }
    formData.append('file', flussoRequest.file, flussoRequest.file.name);

    // CDU-011: Il percorso corretto per la nuova API è /flussi/upload (POST)
    return this.http.post<FlussoUploadResponse>(`${this.getBaseUrl('upload')}/flussi/upload`, formData);
  }

  getTipiFile(): Observable<TipoFile[]> {
    // MODIFICATO: Sostituito mock con chiamata HTTP reale
    return this.http.get<TipoFileApi[]>(`${this.accrualsbsrBaseUrl}/tipi-file`).pipe(
      catchError(error => {
        console.error('Errore durante il recupero dei tipi di file:', error);
        this.notificationService.showError('Errore durante il recupero dei tipi di file.', 'Errore di sistema');
        return throwError(error);
      })
    );
  }

  searchFlussi(filters: FlussoSearchFilter, page: number, pageSize: number): Observable<PagedFlussoSearchResult> {
    console.log('FlussoService: Searching flussi with filters:', filters);

    let params = new HttpParams();
    if (filters.codiceFile) {
      params = params.set('codiceFile', filters.codiceFile);
    }
    if (filters.fonte) {
      params = params.set('fonte', filters.fonte);
    }
    if (filters.tipoFile) {
      params = params.set('tipoFile', filters.tipoFile);
    }
    if (filters.dataCaricamento) {
      // Assuming filters.dataCaricamento is a Date object, format it to YYYY-MM-DD
      const formattedDate = this.datePipe.transform(filters.dataCaricamento, 'yyyy-MM-dd');
      if (formattedDate) {
        params = params.set('dataCaricamento', formattedDate);
      }
    }

    params = params.set('limit', pageSize.toString());
    params = params.set('offset', (page * pageSize).toString());

    return this.http.get<PagedFlussoSearchResult>(`${this.accrualsbsrBaseUrl}/flussi/search`, { params }).pipe(
      tap(result => console.log('FlussoService: Search results received:', result))
    );
  }

  getFonti(): Observable<Fonte[]> {
    return this.http.get<Fonte[]>(`${this.accrualsbsrBaseUrl}/flussi/fonti`).pipe(
      catchError(error => {
        console.error('Errore durante il recupero delle fonti:', error);
        this.notificationService.showError('Errore durante il recupero delle fonti.', 'Errore di sistema');
        return throwError(error);
      })
    );
  }

  getFlussoById(id: string): Observable<FlussoDetail> {
    return this.http.get<FlussoDetail>(`${this.accrualsbsrBaseUrl}/flussi/${id}`).pipe(
      tap(result => console.log(`FlussoService: Flusso ${id} details received:`, result))
    );
  }

  elaboraFlusso(id: string, confirmedOverride: boolean = false): Observable<ElaboraFlussoResponseDTO> {
    const body = { confirmedOverride };
    return this.http.post<ElaboraFlussoResponseDTO>(`${this.getBaseUrl('elabora')}/flussi/${id}/elabora`, body).pipe(
      tap(response => {
        if (response?.needsConfirmation) {
          this.pendingConfirmationSubject.next(response);
        }
      }),
      catchError(error => {
        console.error(`Errore durante l'elaborazione del flusso ${id}:`, error);
        return throwError(() => error);
      })
    );
  }

  resumeElaborazioneFlusso(resumeToken: string, userConfirmed: boolean): Observable<ElaboraFlussoResponseDTO> {
    return this.http.post<ElaboraFlussoResponseDTO>(
      `${this.getBaseUrl('elabora')}/flussi/resume-elaborazione`,
      { userConfirmed },
      { params: new HttpParams().set('resumeToken', resumeToken) }
    ).pipe(
      tap(response => {
        if (response?.needsConfirmation) {
          this.pendingConfirmationSubject.next(response);
        } else {
          this.pendingConfirmationSubject.next(null);
        }
      }),
      catchError(error => {
        console.error('Errore durante la ripresa dell\'elaborazione:', error);
        this.pendingConfirmationSubject.next(null);
        return throwError(() => error);
      })
    );
  }

  clearPendingConfirmation(): void {
    this.pendingConfirmationSubject.next(null);
  }

  /** Apre la barra senza polling reale — per elaborazioni sincrone lato backend. */
  forceProgressStart(id: string): void {
    this.stopStatusPolling();
    this.progressCeiling = 15;
    this.elaborationProgressSubject.next(5);
    this.activeJobStatusSubject.next({ jobId: id, status: 'IN_PROGRESS', progressPercentage: 15, currentStep: 'Elaborazione in corso...' });
    this.simIntervalId = setInterval(() => {
      const cur = this.elaborationProgressSubject.getValue() ?? 0;
      if (this.progressCeiling < 82) this.progressCeiling += 0.04;
      if (cur < this.progressCeiling) {
        this.elaborationProgressSubject.next(cur + Math.max(0.3, (this.progressCeiling - cur) * 0.06));
      }
    }, 200);
  }

  /** Chiude la barra forzando COMPLETED o FAILED — da chiamare alla risposta sincrona. */
  forceProgressEnd(jobId: string, outcome: 'COMPLETED' | 'FAILED', errorMessage?: string): void {
    this.stopStatusPolling();
    const finalStatus: ProcessStatusResponse = {
      jobId,
      status: outcome,
      progressPercentage: outcome === 'COMPLETED' ? 100 : 0,
      currentStep: outcome === 'COMPLETED' ? 'Elaborazione completata' : 'Errore',
      message: outcome === 'COMPLETED' ? 'Completato con successo.' : (errorMessage || 'Si è verificato un errore.')
    };
    this.activeJobStatusSubject.next(finalStatus);
    this.elaborationProgressSubject.next(outcome === 'COMPLETED' ? 100 : 0);
    setTimeout(() => {
      this.elaborationProgressSubject.next(null);
      this.activeJobStatusSubject.next(null);
    }, 1800);
  }

  startStatusPolling(id: string): void {
    this.stopStatusPolling();
    this.progressCeiling = 15;
    this.elaborationProgressSubject.next(5);
    this.activeJobStatusSubject.next({ jobId: id, status: 'IN_PROGRESS', progressPercentage: 15, currentStep: 'Avvio elaborazione...' });

    // Smooth animation: creeps progress toward ceiling every 200ms
    this.simIntervalId = setInterval(() => {
      const cur = this.elaborationProgressSubject.getValue() ?? 0;
      if (cur < this.progressCeiling) {
        this.elaborationProgressSubject.next(cur + Math.max(0.3, (this.progressCeiling - cur) * 0.06));
      }
    }, 200);

    // Real polling: timer(0, 2500) fires immediately then every 2.5s
    this.statusPollSub = timer(0, 2500).pipe(
      switchMap(() => this.getProcessStatus(id)),
      takeWhile(resp => resp.status !== 'COMPLETED' && resp.status !== 'FAILED', true)
    ).subscribe({
      next: (resp: ProcessStatusResponse) => {
        this.activeJobStatusSubject.next(resp);
        // Advance ceiling based on real state; never go backwards
        if (resp.status === 'PENDING')      this.progressCeiling = Math.max(this.progressCeiling, 20);
        if (resp.status === 'IN_PROGRESS')  this.progressCeiling = Math.max(this.progressCeiling, 85);
        if (resp.status === 'COMPLETED') {
          this.progressCeiling = 100;
          this.stopStatusPolling();
          this.elaborationProgressSubject.next(100);
          setTimeout(() => {
            this.elaborationProgressSubject.next(null);
            this.activeJobStatusSubject.next(null);
          }, 1800);
        }
        if (resp.status === 'FAILED') {
          this.stopStatusPolling();
          setTimeout(() => {
            this.elaborationProgressSubject.next(null);
            this.activeJobStatusSubject.next(null);
          }, 1800);
        }
      },
      error: () => {
        this.stopStatusPolling();
        this.elaborationProgressSubject.next(null);
        this.activeJobStatusSubject.next(null);
      }
    });
  }

  stopStatusPolling(): void {
    if (this.statusPollSub) {
      this.statusPollSub.unsubscribe();
      this.statusPollSub = null;
    }
    if (this.simIntervalId) {
      clearInterval(this.simIntervalId);
      this.simIntervalId = null;
    }
  }

  /** Annulla la progress bar senza animazione (usare quando la richiesta non ha avviato elaborazione). */
  cancelProgress(): void {
    this.stopStatusPolling();
    this.elaborationProgressSubject.next(null);
    this.activeJobStatusSubject.next(null);
  }

  processFlusso(id: string): Observable<any> {
    return this.elaboraFlusso(id);
  }

  getProcessStatus(id: string): Observable<ProcessStatusResponse> {
    return this.http.get<ProcessStatusResponse>(`${this.getBaseUrl('elabora')}/flussi/${id}/process-status`);
  }

  downloadFlusso(id: string): Observable<Blob> {
    return this.http.get(`${this.getBaseUrl('download')}/flussi/${id}/download`, { responseType: 'blob' }).pipe(
      tap(() => this.notificationService.showSuccess(`Download del file originale del flusso ${id} avviato.`, 'Successo')),
      catchError(error => {
        console.error(`Errore durante il download del flusso ${id}:`, error);
        this.notificationService.showError(`Errore durante il download del flusso ${id}.`, 'Errore di sistema');
        return throwError(error);
      })
    );
  }

  // NEW: exportResults to download processing results ZIP
  exportResults(id: string): Observable<Blob> {
    return this.http.get(`${this.getBaseUrl('download')}/flussi/${id}/export-results`, { responseType: 'blob' }).pipe(
      tap(() => this.notificationService.showSuccess(`Export dei risultati dell'elaborazione per il flusso ${id} avviato.`, 'Successo')),
      catchError(error => {
        console.error(`Errore durante l'export dei risultati per il flusso ${id}:`, error);
        this.notificationService.showError(`Errore durante l'export dei risultati per il flusso ${id}.`, 'Errore di sistema');
        return throwError(error);
      })
    );
  }

  // NOW USING REAL API CALL: deleteFlusso
  deleteFlusso(id: string): Observable<void> {
    return this.http.delete<void>(`${this.accrualsbsrBaseUrl}/flussi/${id}`).pipe(
      tap(() => this.notificationService.showSuccess(`Flusso ${id} eliminato con successo.`, 'Successo'))
    );
  }
}
