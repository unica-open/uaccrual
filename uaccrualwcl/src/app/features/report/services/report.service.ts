/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  ReportInfo,
  ReportInfoRequest,
  GenerateRequest,
  AsyncReportInitResponse,
  AsyncReportOperationStatus,
  AsyncReportSearchFilter,
  AsyncReportSearchRequest,
  AsyncReportSearchResponse,
  AsyncReportDetail
} from '../models/report.models';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private readonly apiUrl = `${environment.services.accrualReportsApiUrl}/report`;

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  constructor(private http: HttpClient) { }

  /**
   * Ottiene i metadata di un report (parametri estratti dal template BIRT)
   * @param rptdesign Percorso del template .rptdesign
   */
  getReportInfo(rptdesign: string): Observable<ReportInfo> {
    const request: ReportInfoRequest = { rptdesign };
    return this.http.post<ReportInfo>(`${this.apiUrl}`, request, { headers: this.getHeaders() });
  }

  /**
   * Genera un report in modo sincrono
   * @param request Configurazione del report
   * @param parameterTypes Tipi dei parametri (opzionale, per conversione valori)
   * @returns Blob del report generato
   */
  generaReportSync(request: GenerateRequest, parameterTypes?: { [key: string]: string }): Observable<Blob> {
    // Assicura che parameters sia sempre un oggetto valido
    const convertedParams = this.convertParameterValues(request.parameters || {}, parameterTypes);
    const body = {
      ...request,
      parameters: convertedParams
    };
    return this.http.post(`${this.apiUrl}/generate`, body, {
      headers: this.getHeaders(),
      responseType: 'blob'
    });
  }

  /**
   * Genera un report in modo asincrono
   * @param request Configurazione del report
   * @param parameterTypes Tipi dei parametri (opzionale, per conversione valori)
   * @returns Response con ID del report asincrono
   */
  generaReportAsync(request: GenerateRequest, parameterTypes?: { [key: string]: string }): Observable<AsyncReportInitResponse> {
    // Assicura che parameters sia sempre un oggetto valido
    const convertedParams = this.convertParameterValues(request.parameters || {}, parameterTypes);
    const body = {
      ...request,
      parameters: convertedParams
    };
    return this.http.post<AsyncReportInitResponse>(`${this.apiUrl}/generate-async`, body, { headers: this.getHeaders() });
  }

  /**
   * Converte i valori dei parametri in base al tipo restituito dal backend.
   * Il backend BIRT è stringente sui tipi: INTEGER richiede un numero, non una stringa.
   */
  private convertParameterValues(params: { [key: string]: any }, parameterTypes?: { [key: string]: string }): { [key: string]: any } {
    if (!parameterTypes) {
      return params;
    }

    const converted: { [key: string]: any } = {};

    for (const key of Object.keys(params)) {
      const value = params[key];
      const type = parameterTypes[key];

      if (value === null || value === undefined || value === '') {
        converted[key] = null;
        continue;
      }

      // Converte in base al tipo
      switch (type) {
        case 'NUMBER':
        case 'INTEGER':
          converted[key] = typeof value === 'number' ? value : Number(value);
          break;
        case 'BOOLEAN':
          converted[key] = typeof value === 'boolean' ? value : value === 'true' || value === '1';
          break;
        case 'DATE':
          // Le date rimangono stringhe ISO, BIRT le gestisce correttamente
          converted[key] = value;
          break;
        case 'TEXT':
        default:
          converted[key] = String(value);
          break;
      }
    }

    return converted;
  }

  /**
   * Ottiene lo stato di un report asincrono
   * @param reportId ID del report
   */
  getStatoReport(reportId: string): Observable<AsyncReportInitResponse> {
    return this.http.get<AsyncReportInitResponse>(`${this.apiUrl}/generate-async/${reportId}`, { headers: this.getHeaders() });
  }

  // ============================================================================
  // METODI PER REPORT ASINCRONI (CDU-015)
  // ============================================================================

  /**
   * Ricerca report asincroni con filtri
   * @param request Request con filtri, paginazione e ordinamento
   * @returns Observable con response paginata
   */
  searchAsyncReports(request: AsyncReportSearchRequest): Observable<AsyncReportSearchResponse> {
    const headers = this.getHeaders();
    return this.http.post<AsyncReportSearchResponse>(
      `${this.apiUrl}/search`,
      request,
      { headers }
    );
  }

  /**
   * Ottiene il dettaglio di un report asincrono
   * @param reportId ID del report
   * @returns Observable con dettaglio completo
   */
  getAsyncReportDetail(reportId: string): Observable<AsyncReportDetail> {
    const headers = this.getHeaders();
    return this.http.get<AsyncReportDetail>(
      `${this.apiUrl}/${reportId}`,
      { headers }
    );
  }

  /**
   * Scarica un report asincrono completato
   * @param reportId ID del report
   * @returns Observable con Blob del file
   */
  downloadAsyncReport(reportId: string): Observable<Blob> {
    const headers = this.getHeaders();
    return this.http.get(
      `${this.apiUrl}/${reportId}/download`,
      {
        headers,
        responseType: 'blob'
      }
    );
  }

  /**
   * Annulla un report asincrono in coda o in elaborazione
   * @param reportId ID del report
   * @returns Observable con stato aggiornato
   */
  cancelAsyncReport(reportId: string): Observable<{ reportId: string; status: 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' }> {
    const headers = this.getHeaders();
    return this.http.post<{ reportId: string; status: 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED' }>(
      `${this.apiUrl}/${reportId}/cancel`,
      {},
      { headers }
    );
  }

  /**
   * Elimina un report asincrono completato o fallito
   * @param reportId ID del report
   * @returns Observable void
   */
  deleteAsyncReport(reportId: string): Observable<void> {
    const headers = this.getHeaders();
    return this.http.delete<void>(
      `${this.apiUrl}/${reportId}`,
      { headers }
    );
  }
}
