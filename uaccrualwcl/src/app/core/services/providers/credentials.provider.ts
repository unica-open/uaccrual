/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthProvider } from './auth-provider.interface';
import { LoginRequest, LoginResponse } from '../../models/auth.models';
import { AuthProviderUtils } from '../../models/auth-provider.type';
import { environment } from '../../../../environments/environment';

/**
 * Provider di autenticazione per credenziali (username/password).
 * 
 * Gestisce il flusso di login tradizionale con username e password.
 * È sempre disponibile e funge da fallback per gli altri provider.
 * 
 * @see AuthProvider
 */
@Injectable({
  providedIn: 'root'
})
export class CredentialsProvider implements AuthProvider {
  private readonly apiUrl = environment.services.accrualAuthApiUrl;

  constructor(private http: HttpClient) {}

  /**
   * Nome del provider
   */
  getName(): string {
    return 'SpringSecurity';
  }

  /**
   * Il provider credenziali è sempre disponibile.
   * Funge da fallback quando gli altri provider non sono disponibili.
   * 
   * @returns true (sempre disponibile)
   */
  isAvailable(): boolean {
    return true;
  }

  /**
   * Esegue il login con username e password.
   * Invia le credenziali al backend e riceve un JWT.
   * 
   * @param request Richiesta di login con codiceFiscale e password
   * @returns Observable di LoginResponse
   * 
   * @example
   * // Da LoginComponent
   * credentialsProvider.login({
   *   codiceFiscale: 'RSSMRA80A01H501U',
   *   password: 'password123',
   *   provider: 'SpringSecurity'
   * });
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    // Normalizza il provider per il backend
    let providerString = 'SpringSecurity';
    if (request.provider) {
      try {
        const normalized = AuthProviderUtils.normalize(
          typeof request.provider === 'string' ? request.provider : 'SPRING_SECURITY'
        );
        providerString = AuthProviderUtils.toBackendString(normalized);
      } catch {
        // Ignora errori di normalizzazione, usa SpringSecurity come fallback
      }
    }

    // Costruisci la richiesta per il backend
    // Il backend si aspetta snake_case (come definito in openapi.yaml)
    const loginRequest: any = {
      codice_fiscale: request.codiceFiscale,
      password: request.password,
      token: request.token,
      provider: providerString,
      codice_ente: request.enteCodiceUnivoco,
      anno_bilancio: request.annoBilancio,
      fase_bilancio: request.faseBilancio
    };

    // Rimuovi i campi undefined
    Object.keys(loginRequest).forEach(key => {
      if (loginRequest[key] === undefined) {
        delete loginRequest[key];
      }
    });

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, loginRequest);
  }

  /**
   * Esegue il logout.
   * Chiama il backend per invalidare la sessione.
   * 
   * @returns Observable void
   */
  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {});
  }

  /**
   * Priorità del provider credenziali.
   * Priorità bassa (10) poiché è il fallback.
   * 
   * @returns 10
   */
  getPriority(): number {
    return 10;
  }
}
