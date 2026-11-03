/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

/**
 * Interfaccia per le informazioni sugli attributi
 */
export interface AttributoInfo {
  codice: string;
  descrizione: string;
}

/**
 * Servizio per la gestione degli attributi
 */
@Injectable({
  providedIn: 'root'
})
export class AttributoService {

  private readonly baseUrl = '/accrualsbsrv/api/v1';
  private readonly apiUrl = `${this.baseUrl}/attributi`;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * Recupera la lista degli attributi disponibili per un ente
   * @returns Observable con la lista degli attributi
   */
  getAttributiByEnte(): Observable<AttributoInfo[]> {
    return this.http.get<AttributoInfo[]>(this.apiUrl).pipe(
      catchError(error => {
        console.error('Errore nel recupero attributi:', error);
        throw error;
      })
    );
  }
}
