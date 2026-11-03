/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthStorageService {
  private readonly TOKEN_KEY = 'accrual_auth_token';
  private readonly USER_INFO_KEY = 'accrual_user_info';
  private readonly ENTE_CODICE_KEY = 'accrual_ente_codice';
  private readonly LOGIN_PROVIDER_KEY = 'accrual_login_provider';

  constructor() { }

  // ==================== TOKEN ====================

  /**
   * Salva il token JWT in sessionStorage
   */
  setToken(token: string): void {
    sessionStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Ottiene il token JWT da sessionStorage
   */
  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Rimuove il token JWT
   */
  removeToken(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
  }

  // ==================== USER INFO ====================

  /**
   * Salva le informazioni utente in sessionStorage
   */
  setUserInfo(userInfo: {
    codiceFiscale: string;
    nome: string;
    ente: string;
    ruolo: string;
    utenteCodice?: string;
    source?: string;
  }): void {
    sessionStorage.setItem(this.USER_INFO_KEY, JSON.stringify(userInfo));
  }

  /**
   * Ottiene le informazioni utente da sessionStorage
   */
  getUserInfo(): {
    codiceFiscale: string;
    nome: string;
    ente: string;
    ruolo: string;
    utenteCodice?: string;
    source?: string;
  } | null {
    const userInfo = sessionStorage.getItem(this.USER_INFO_KEY);
    return userInfo ? JSON.parse(userInfo) : null;
  }

  /**
   * Rimuove le informazioni utente
   */
  removeUserInfo(): void {
    sessionStorage.removeItem(this.USER_INFO_KEY);
  }

  // ==================== ENTE CODICE ====================

  /**
   * Salva il codice ente in sessionStorage
   */
  setEnteCodice(enteCodice: string): void {
    sessionStorage.setItem(this.ENTE_CODICE_KEY, enteCodice);
  }

  /**
   * Ottiene il codice ente da sessionStorage
   */
  getEnteCodice(): string | null {
    return sessionStorage.getItem(this.ENTE_CODICE_KEY);
  }

  /**
   * Rimuove il codice ente
   */
  removeEnteCodice(): void {
    sessionStorage.removeItem(this.ENTE_CODICE_KEY);
  }

  // ==================== LOGIN PROVIDER ====================

  /**
   * Salva il provider di login usato (SpringSecurity, Shibboleth, Token)
   */
  setLoginProvider(provider: string): void {
    sessionStorage.setItem(this.LOGIN_PROVIDER_KEY, provider);
  }

  /**
   * Ottiene il provider di login da sessionStorage
   */
  getLoginProvider(): string | null {
    return sessionStorage.getItem(this.LOGIN_PROVIDER_KEY);
  }

  /**
   * Rimuove il provider di login
   */
  removeLoginProvider(): void {
    sessionStorage.removeItem(this.LOGIN_PROVIDER_KEY);
  }

  // ==================== CLEAR ALL ====================

  /**
   * Pulisce tutti i dati di autenticazione
   */
  clear(): void {
    this.removeToken();
    this.removeUserInfo();
    this.removeEnteCodice();
    this.removeLoginProvider();
  }

  /**
   * Verifica se ci sono dati di autenticazione presenti
   */
  hasAuthData(): boolean {
    return this.getToken() !== null;
  }
}
