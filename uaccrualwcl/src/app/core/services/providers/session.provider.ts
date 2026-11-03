/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AuthProvider } from './auth-provider.interface';
import { LoginRequest, LoginResponse } from '../../models/auth.models';
import { AuthStorageService } from '../auth-storage.service';
import { JwtHelper } from '../../utils/jwt.helper';

/**
 * Provider di autenticazione per sessione esistente.
 * 
 * Gestisce il ripristino della sessione da un JWT già presente in sessionStorage.
 * Utile per mantenere la sessione al refresh della pagina senza chiamare il backend.
 * 
 * @see AuthProvider
 */
@Injectable({
  providedIn: 'root'
})
export class SessionProvider implements AuthProvider {

  constructor(
    private authStorage: AuthStorageService,
    private jwtHelper: JwtHelper
  ) {}

  /**
   * Nome del provider
   */
  getName(): string {
    return 'Session';
  }

  /**
   * Verifica se esiste una sessione valida in sessionStorage.
   * Controlla se è presente un JWT non scaduto.
   * 
   * @returns true se sessione valida presente
   */
  isAvailable(): boolean {
    const token = this.authStorage.getToken();
    
    if (!token) {
      return false;
    }

    // Verifica che il token non sia scaduto
    return !this.jwtHelper.isTokenExpired(token);
  }

  /**
   * Esegue il "login" ricostruendo il contesto da un JWT esistente.
   * Non chiama il backend, ma restituisce una risposta simulata con il token esistente.
   * 
   * @param request Richiesta di login (ignorata, usa token da sessionStorage)
   * @returns Observable di LoginResponse con token esistente
   * 
   * @example
   * // In APP_INITIALIZER per ripristinare sessione
   * if (sessionProvider.isAvailable()) {
   *   sessionProvider.login({}).subscribe(response => {
   *     // Contesto ripristinato senza chiamata backend
   *   });
   * }
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    const token = this.authStorage.getToken();
    
    if (!token) {
      throw new Error('Nessun token presente in sessionStorage');
    }

    // Decodifica il token per estrarre informazioni
    const decodedToken = this.jwtHelper.decodeToken(token);
    
    // Costruisci una risposta simulata
    const response: LoginResponse = {
      token: token,
      data: {
        token: token,
        primoAccesso: false,
        userInfo: {
          codiceFiscale: decodedToken?.sub || decodedToken?.codiceFiscale || '',
          nome: decodedToken?.nome || decodedToken?.name || '',
          ente: this.jwtHelper.getCodiceEnte(token) || '',
          ruolo: decodedToken?.ruolo || ''
        }
      }
    };

    // Ritorna la risposta come Observable
    return of(response);
  }

  /**
   * Esegue il logout.
   * Per SessionProvider, il logout è locale (pulizia sessionStorage).
   * Non chiama il backend poiché la sessione è già gestita localmente.
   * 
   * @returns Observable void
   */
  logout(): Observable<void> {
    // Pulisce sessionStorage
    this.authStorage.clear();
    
    // Ritorna un Observable completato
    return of(undefined);
  }

  /**
   * Priorità del provider Session.
   * Priorità massima (100) poiché se esiste già una sessione valida,
   * non c'è bisogno di chiamare il backend.
   * 
   * @returns 100 se sessione valida, 0 altrimenti
   */
  getPriority(): number {
    return this.isAvailable() ? 100 : 0;
  }
}
