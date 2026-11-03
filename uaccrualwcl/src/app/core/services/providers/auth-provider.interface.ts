/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse } from '../../models/auth.models';

/**
 * Interfaccia per i provider di autenticazione.
 * Implementa il pattern Strategy per permettere provider intercambiabili.
 * 
 * @see CredentialsProvider
 * @see SessionProvider
 * @see DevProvider
 */
export interface AuthProvider {
  /**
   * Nome del provider per identificazione
   * @returns Il nome del provider (es. 'Shibboleth', 'SpringSecurity')
   */
  getName(): string;
  
  /**
   * Verifica se questo provider è disponibile nell'ambiente corrente
   * @returns true se disponibile, false altrimenti
   * 
   * @example
   * // Shibboleth: controlla se URL ha ?token=
   * // Dev: controlla se environment.isDev
   * // Session: controlla se JWT valido in sessionStorage
   */
  isAvailable(): boolean | Observable<boolean>;
  
  /**
   * Esegue il login utilizzando il provider specifico
   * @param request Richiesta di login con credenziali o token
   * @returns Observable di LoginResponse
   */
  login(request: LoginRequest): Observable<LoginResponse>;
  
  /**
   * Esegue il logout
   * @returns Observable void
   */
  logout(): Observable<void>;
  
  /**
   * Priorità del provider per la selezione automatica
   * Valori più alti = priorità maggiore
   * 
   * @returns Numero di priorità
   * 
   * @example
    * SessionProvider: 100 (massima priorità se sessione attiva)
    * CredentialsProvider: 10 (fallback per login manuale)
    * DevProvider: 1 (solo in sviluppo)
    */
  getPriority(): number;
}
