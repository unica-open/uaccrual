/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Observable, of, map, switchMap, from } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthProvider } from './auth-provider.interface';
import { LoginRequest, LoginResponse } from '../../models/auth.models';
import { environment } from '../../../../environments/environment';

/**
 * Provider di autenticazione per ambiente di sviluppo.
 *
 * Attivo solo se environment.isDev === true.
 * Carica il contesto da dev-info.json o usa valori default.
 *
 * Il token dev è un JWT con struttura header.payload.signature
 * (alg: none, signature vuota) compatibile con JwtHelper.
 * Il backend DevAuthFilter accetta token con alg: none in modalità dev.
 *
 * @see AuthProvider
 */
@Injectable({
  providedIn: 'root'
})
export class DevProvider implements AuthProvider {
  private readonly devInfoUrl = (environment.authentication as any)?.devFileUrl || '/accrualwcl/assets/config/dev-info.json';

  constructor(private http: HttpClient) {}

  /**
   * Nome del provider
   */
  getName(): string {
    return 'Dev';
  }

  /**
   * Verifica se il provider Dev è disponibile.
   * È disponibile solo se environment.isDev === true.
   *
   * @returns true se in modalità sviluppo
   */
  isAvailable(): boolean {
    // Controlla se siamo in modalità sviluppo
    return !environment.production;
  }

  /**
   * Esegue il login in modalità sviluppo.
   * Carica il contesto da dev-info.json o usa valori default.
   *
   * @param request Richiesta di login (ignorata in dev mode)
   * @returns Observable di LoginResponse con token simulato
   *
   * @example
   * // In APP_INITIALIZER per dev mode
   * if (devProvider.isAvailable()) {
   *   devProvider.login({}).subscribe(response => {
   *     // Contesto caricato da dev-info.json
   *   });
   * }
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    console.log('[DevProvider] ===== login INIZIO =====');
    console.log('[DevProvider] URL richiesta:', this.devInfoUrl);
    return this.http.get<any>(this.devInfoUrl).pipe(
      map((devInfo) => {
        console.log('[DevProvider] Risposta HTTP da dev-info.json:', JSON.stringify(devInfo));
        return this.createLoginResponse(devInfo);
      }),
      switchMap((responsePromise) => {
        return from(responsePromise);
      }),
    );
  }

  /**
   * Esegue il logout in modalità sviluppo.
   * Pulisce il contesto locale.
   *
   * @returns Observable void
   */
  logout(): Observable<void> {
    // In dev mode, il logout è solo locale
    console.log('[DevProvider] Logout in modalità sviluppo');
    return of(undefined);
  }

  /**
   * Priorità del provider Dev.
   * Priorità bassa (1) poiché è usato solo in sviluppo.
   *
   * @returns 1 se in dev mode, 0 altrimenti
   */
  getPriority(): number {
    return this.isAvailable() ? 1 : 0;
  }

  /**
   * Crea una risposta LoginResponse dai dati dev-info.json.
   *
   * @param devInfo Dati da dev-info.json
   * @returns LoginResponse con token base64
   */
  private async createLoginResponse(devInfo: any): Promise<LoginResponse> {
    const token = this.createDevToken(devInfo);

    return {
      token: token,
      data: {
        token: token,
        primoAccesso: false,
        utenteCodice: devInfo.user?.username,
        userInfo: {
          codiceFiscale: devInfo.user?.username,
          nome: devInfo.user?.displayName,
          ente: devInfo.enteCodice,
          ruolo: devInfo.ruolo // Assuming ruolo is directly from devInfo
        }
      }
    };
  }

  /**
   * Crea un token dev per lo sviluppo con struttura JWT (header.payload.signature).
   * La signature è finta (non validata) ma la struttura è compatibile con JwtHelper.
   *
   * Struttura del payload (claim snake_case secondo RFC 7519 per custom claims):
   * {
   *   sub: string,
   *   codice_fiscale: string,
   *   nome: string,
   *   cognome: string,
   *   codice_ente: string,
   *   denominazione_ente: string,
   *   ruolo: string,
   *   source: 'DEV',
   *   anno_bilancio: number,
   *   fase_bilancio: string
   * }
   *
   * @param devInfo Dati da dev-info.json
   * @returns Token JWT con signature finta (solo per dev)
   */
  private createDevToken(devInfo: any): string {
    console.log('[DevProvider] ===== createDevToken =====');
    console.log('[DevProvider] Dati devInfo ricevuti:', JSON.stringify(devInfo));

    const header = {
      alg: 'none',
      typ: 'JWT'
    };

    if (!devInfo.annoBilancio) {
      console.warn('[DevProvider] devInfo.annoBilancio mancante');
    }
    if (!devInfo.faseBilancio) {
      console.warn('[DevProvider] devInfo.faseBilancio mancante');
    }

    const payload = {
      sub: devInfo.user?.username,
      codice_fiscale: devInfo.user?.username,
      utente_codice: devInfo.user?.utenteCodice || ('COD-' + devInfo.user?.username),
      nome: devInfo.user?.utenteCodice || ('COD-' + devInfo.user?.username),
      cognome: devInfo.user?.cognome || devInfo.user?.displayName,
      codice_ente: devInfo.enteCodice,
      denominazione_ente: devInfo.enteDenominazione,
      ruolo: devInfo.user?.ruolo || devInfo.ruolo,
      source: devInfo.source,
      anno_bilancio: devInfo.annoBilancio,
      fase_bilancio: devInfo.faseBilancio,
      exp: Math.floor(Date.now() / 1000) + (24 * 60 * 60),
      iat: Math.floor(Date.now() / 1000)
    };

    console.log('[DevProvider] Payload JWT:', JSON.stringify(payload));

    const encodedHeader = btoa(JSON.stringify(header));
    const encodedPayload = btoa(JSON.stringify(payload));
    const signature = '';

    return `${encodedHeader}.${encodedPayload}.${signature}`;
  }
}
