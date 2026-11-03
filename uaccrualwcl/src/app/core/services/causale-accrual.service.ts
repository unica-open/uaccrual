/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { Causale, CausaleSearchCriteria, CausaleTipo, CausaleStato, Evento, Paged, ContoCollegato, CausaleRequest } from '../../features/causale-accrual/model/causale.model';
import { Pdc } from '../../features/pdc/model/pdc.model';

@Injectable({
  providedIn: 'root'
})
export class CausaleAccrualService {

  private baseUrl = '/accrualsbsrv/api/v1';

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private searchCriteriaSubject = new BehaviorSubject<CausaleSearchCriteria>({});
  public searchCriteria$ = this.searchCriteriaSubject.asObservable();

  constructor(private http: HttpClient) { }

  ricercaCausali(
    filters: CausaleSearchCriteria,
    page: number = 0,
    size: number = 10
  ): Observable<Paged<Causale>> {
    let params = new HttpParams()
      .set('offset', (page * size).toString())
      .set('limit', size.toString());

    const backendFilters: any = {};

    if (filters.tipoCausale && filters.tipoCausale !== '--- tutti i valori ---') {
      backendFilters['causaleTipo.codice'] = { eq: filters.tipoCausale };
    }
    if (filters.eventoCausale && filters.eventoCausale !== '--- tutti i valori ---') {
      backendFilters['evento.codice'] = { eq: filters.eventoCausale };
    }
    if (filters.codice) {
      // Ricerca per codice parziale case-insensitive (starts-with)
      backendFilters['codice'] = { si: filters.codice };
    }
    if (filters.descrizione) {
      // Ricerca per descrizione parziale case-insensitive (contains)
      backendFilters['descrizione'] = { ci: filters.descrizione };
    }
    if (filters.stato && filters.stato !== '--- tutti i valori ---') {
      backendFilters['stato.codice'] = { eq: filters.stato };
    }

    if (Object.keys(backendFilters).length > 0) {
      params = params.set('filter', JSON.stringify(backendFilters));
    }

    return this.http.get<any>(`${this.baseUrl}/causali`, { params }).pipe(
      tap(results => {
        console.log('CausaleAccrualService received results from backend:', results);
      }),
      map(response => {
        return {
          data: response.data || [],
          page: response.page,
          page_size: response.page_size,
          total_elements: response.total_elements
        };
      })
    );
  }

  /**
   * Restituisce tutte le causali che soddisfano i filtri, senza paginazione.
   * Usa la convenzione limit=-1 supportata dal backend, la stessa di
   * PdcService.getAllPdcResults. Serve all'export CSV, che deve contenere
   * l'intero risultato della ricerca e non la sola pagina visualizzata.
   */
  getAllCausaliResults(filters: CausaleSearchCriteria): Observable<Causale[]> {
    const maxSize = -1; // Valore speciale per ottenere tutti i record possibili
    return this.ricercaCausali(filters, 0, maxSize).pipe(
      map(response => response.data || [])
    );
  }

  getCausaleById(id: number): Observable<Causale> {
    return this.http.get<Causale>(`${this.baseUrl}/causali/${id}`);
  }

  getTipiCausale(): Observable<CausaleTipo[]> {
    return this.http.get<CausaleTipo[]>(`${this.baseUrl}/causali/tipi`);
  }

  getEventiCausale(): Observable<Evento[]> {
    return this.http.get<Evento[]>(`${this.baseUrl}/eventi`);
  }

  getStati(): Observable<CausaleStato[]> {
    return this.http.get<CausaleStato[]>(`${this.baseUrl}/causali/stati`);
  }

  createCausale(causale: CausaleRequest): Observable<Causale> {
    return this.http.post<Causale>(`${this.baseUrl}/causali`, causale);
  }

  updateCausale(id: number, causale: CausaleRequest): Observable<Causale> {
    return this.http.put<Causale>(`${this.baseUrl}/causali/${id}`, causale);
  }

  deleteCausale(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/causali/${id}`);
  }

  validaCausale(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/causali/${id}/valida`, {});
  }

  getContiCollegati(id: number): Observable<ContoCollegato[]> {
    return this.http.get<ContoCollegato[]>(`${this.baseUrl}/causali/${id}/conti-collegati`).pipe(
      tap(data => console.log('Dati conti collegati ricevuti dal backend:', data))
    );
  }

  searchConti(codice: string, descrizione: string): Observable<Pdc[]> {
    let params = new HttpParams();
    if (codice) {
      params = params.set('codice', codice);
    }
    if (descrizione) {
      params = params.set('descrizione', descrizione);
    }

    return this.http.get<Paged<Pdc>>(`${this.baseUrl}/pdc/ricerca`, { params }).pipe(
      map(response => response.data || [])
    );
  }
}
