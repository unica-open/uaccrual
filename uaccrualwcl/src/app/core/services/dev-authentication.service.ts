/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, lastValueFrom, firstValueFrom } from 'rxjs';
import { Store } from '@ngxs/store';
import { LoadContext, SetCurrentEnte, SetCurrentEnteDenominazione, SetUserPermissions } from '../store/context/context.state';
import { UserInfo } from './user-info.interface';
import { JwtHelper } from '../utils/jwt.helper';
import { AuthStorageService } from './auth-storage.service';
import { DevProvider } from './providers/dev.provider';
import { LoginRequest } from '../models/auth.models';
import { AuthContextService } from './auth-context.service';

@Injectable({
  providedIn: 'root'
})
export class DevAuthenticationService {
  private currentUser: UserInfo | null = null;
  private currentEnte: string | null = null;
  private availableEnti: any[] = [];
  private annoBilancio: number | null = null;
  private faseBilancio: string | null = null;

  private enteSubject = new BehaviorSubject<string | null>(null);
  public ente$ = this.enteSubject.asObservable();

  constructor(
    private http: HttpClient,
    private store: Store,
    private jwtHelper: JwtHelper,
    private authStorage: AuthStorageService,
    private devProvider: DevProvider,
    private authContext: AuthContextService
  ) {
    // Imposta il tempo di inizio del caricamento
    this.loadStartTime = Date.now();
  }

  public async loadDevUserInfo(): Promise<void> {
    console.log('[DevAuthService] ===== loadDevUserInfo INIZIO =====');
    try {
      const token = this.authStorage.getToken();
      console.log('[DevAuthService] Token in sessionStorage:', token ? token.substring(0, 50) + '...' : 'null');

      if (token) {
        console.log('[DevAuthService] Trovato token in storage, decodifico JWT...');
        const decoded = this.jwtHelper.decodeToken(token);
        console.log('[DevAuthService] JWT decodificato:', JSON.stringify(decoded));

        if (!decoded?.utente_codice || !decoded?.cognome) {
          console.log('[DevAuthService] Token incompleto (manca utente_codice o cognome), rigenero da DevProvider...');
          this.authStorage.removeToken();
          await this.loadFromDevProvider();
          return;
        }

        this.currentUser = {
          username: decoded?.codice_fiscale || decoded?.codiceFiscale || decoded?.sub || 'dev-user',
          displayName: decoded?.cognome || decoded?.nome || 'Utente di Sviluppo',
          email: decoded?.email || 'dev@example.com',
          enti: [],
          permissions: decoded?.permissions || []
        };
        console.log('[DevAuthService] currentUser impostato da JWT:', JSON.stringify(this.currentUser));

        const codiceEnte = this.jwtHelper.getCodiceEnte(token);
        this.annoBilancio = this.jwtHelper.getAnnoBilancio(token);
        this.faseBilancio = this.jwtHelper.getFaseBilancio(token);
        console.log('[DevAuthService] Dati da JWT - codiceEnte:', codiceEnte, 'anno:', this.annoBilancio, 'fase:', this.faseBilancio);

        const denominazioneEnte = decoded?.denominazione_ente || null;
        this.availableEnti = codiceEnte ? [{ codice: codiceEnte, denominazione: denominazioneEnte }] : [];

        const contextData = {
          username: this.currentUser.username,
          displayName: this.currentUser.displayName,
          utenteCodice: decoded?.utente_codice || null,
          email: this.currentUser.email,
          ruolo: decoded?.ruolo || null,
          userPermissions: this.currentUser.permissions || [],
          faseBilancio: this.faseBilancio,
          annoBilancio: this.annoBilancio,
          currentEnte: codiceEnte,
          currentEnteDenominazione: denominazioneEnte,
          availableEnti: this.availableEnti
        };
        console.log('[DevAuthService] Dispatch LoadContext da JWT:', JSON.stringify(contextData));
        this.store.dispatch(new LoadContext(contextData));

        if (this.availableEnti.length > 0) {
          this.setEnte(codiceEnte!);
        }
        console.log('[DevAuthService] ===== loadDevUserInfo COMPLETATO (da token JWT) =====');
      } else {
        console.log('[DevAuthService] Nessun token in storage, chiamo loadFromDevProvider...');
        await this.loadFromDevProvider();
        console.log('[DevAuthService] ===== loadDevUserInfo COMPLETATO (da DevProvider) =====');
      }
    } catch (error) {
      console.error('[DevAuthService] ERRORE in loadDevUserInfo:', error);
    }
  }

  /**
   * Carica i dati utente usando DevProvider e salva il token
   */
  private async loadFromDevProvider(): Promise<void> {
    console.log('[DevAuthService] ===== loadFromDevProvider INIZIO =====');
    try {
      console.log('[DevAuthService] Chiamo DevProvider.login()...');
      const loginRequest: LoginRequest = { provider: 'Token' };
      const response$ = this.devProvider.login(loginRequest);
      const response = await firstValueFrom(response$);
      console.log('[DevAuthService] Risposta DevProvider.login():', JSON.stringify(response).substring(0, 200));

      const token = response.token || response.data?.token;
      console.log('[DevAuthService] Token ottenuto:', token ? token.substring(0, 80) + '...' : 'null');

      if (!token) {
        throw new Error('DevProvider non ha restituito un token');
      }

      console.log('[DevAuthService] Salvo token in sessionStorage...');
      this.authStorage.setToken(token);

      const decoded = this.jwtHelper.decodeToken(token);
      console.log('[DevAuthService] Token decodificato:', JSON.stringify(decoded));

      this.currentUser = {
        username: decoded?.codice_fiscale || decoded?.codiceFiscale || 'dev-user',
        displayName: decoded?.cognome || decoded?.nome || 'Utente di Sviluppo',
        email: decoded?.email || 'dev@example.com',
        enti: [],
        permissions: decoded?.permissions || []
      };
      console.log('[DevAuthService] currentUser:', JSON.stringify(this.currentUser));

      const codiceEnte = this.jwtHelper.getCodiceEnte(token);
      this.annoBilancio = this.jwtHelper.getAnnoBilancio(token);
      this.faseBilancio = this.jwtHelper.getFaseBilancio(token);
      console.log('[DevAuthService] codiceEnte:', codiceEnte, 'anno:', this.annoBilancio, 'fase:', this.faseBilancio);

      const denominazioneEnte = decoded?.denominazione_ente || null;
      this.availableEnti = codiceEnte ? [{ codice: codiceEnte, denominazione: denominazioneEnte }] : [];

      this.authStorage.setUserInfo({
        codiceFiscale: this.currentUser.username,
        nome: this.currentUser.displayName,
        ente: codiceEnte || '',
        ruolo: decoded?.ruolo || 'DEVELOPER',
        utenteCodice: decoded?.utente_codice || undefined,
        source: 'DEV'
      });
      console.log('[DevAuthService] UserInfo salvato in storage:', JSON.stringify(this.authStorage.getUserInfo()));

      const contextData = {
        username: this.currentUser.username,
        displayName: this.currentUser.displayName,
        utenteCodice: decoded?.utente_codice || null,
        email: this.currentUser.email,
        ruolo: decoded?.ruolo || 'DEVELOPER',
        userPermissions: this.currentUser.permissions || [],
        faseBilancio: this.faseBilancio,
        annoBilancio: this.annoBilancio,
        currentEnte: codiceEnte,
        currentEnteDenominazione: denominazioneEnte,
        availableEnti: this.availableEnti
      };
      console.log('[DevAuthService] Dispatch LoadContext:', JSON.stringify(contextData));
      await firstValueFrom(this.store.dispatch(new LoadContext(contextData)));

      if (this.availableEnti.length > 0) {
        const firstCodice = typeof this.availableEnti[0] === 'object' ? this.availableEnti[0].codice : this.availableEnti[0];
        this.setEnte(firstCodice);
      }

      console.log('[DevAuthService] ===== loadFromDevProvider COMPLETATO =====');
    } catch (error) {
      console.error('[DevAuthService] ERRORE in loadFromDevProvider:', error);
    }
  }

  /**
   * Carica i dati utente da dev-info.json (deprecated, usa loadFromDevProvider)
   */
  private async loadFromDevInfoFile(): Promise<void> {
    try {
      const configData: any = await lastValueFrom(this.http.get('/accrualwcl/assets/config/dev-info.json'));

      // Estrai le informazioni utente
      this.currentUser = {
        username: configData.user.username,
        displayName: configData.user.displayName,
        email: configData.user.email,
        enti: (configData.availableEnti || []).map((e: any) => typeof e === 'object' ? e.codice : e),
        permissions: configData.user.permissions || []
      };
      this.availableEnti = configData.availableEnti || [];

      // Carica tutte le informazioni di contesto nello store
      const contextData = {
        username: configData.user.username,
        displayName: configData.user.displayName,
        email: configData.user.email,
        userPermissions: configData.user.permissions || [],
        faseBilancio: configData.faseBilancio || null,
        annoBilancio: configData.annoBilancio || null,
        currentEnte: configData.currentEnte || null,
        currentEnteDenominazione: configData.currentEnteDenominazione || null,
        availableEnti: configData.availableEnti || []
      };

      // Attendi che l'azione NGXS sia completata prima di procedere
      await firstValueFrom(this.store.dispatch(new LoadContext(contextData)));

      // Seleziona automaticamente il primo ente se presente
      if (this.availableEnti.length > 0) {
        const firstEnte = typeof this.availableEnti[0] === 'object' ? this.availableEnti[0].codice : this.availableEnti[0];
        this.setEnte(firstEnte);
      }
    } catch (error) {
      console.error('Errore durante il caricamento da dev-info.json:', error);
      // Fallback a setDevUserDefault() rimosso
    }
  }



  /**
   * Imposta l'ente corrente
   */
  setEnte(codiceFiscaleEnte: string): void {
    const ente = this.availableEnti.find((e: any) => (typeof e === 'object' ? e.codice : e) === codiceFiscaleEnte);

    if (ente) {
      this.currentEnte = codiceFiscaleEnte;
      this.enteSubject.next(codiceFiscaleEnte);

      // Aggiorna lo store NGXS con codice
      this.store.dispatch(new SetCurrentEnte(codiceFiscaleEnte));

      // E anche con denominazione se disponibile
      if (typeof ente === 'object' && ente.denominazione) {
        this.store.dispatch(new SetCurrentEnteDenominazione(ente.denominazione));
      }

      // Aggiorna anche AuthContextService (usato per la gestione del contesto)
      const enteDenominazione = (typeof ente === 'object' && ente.denominazione) ? ente.denominazione : null;
      this.authContext.setContext(
        {
          codice: codiceFiscaleEnte,
          codiceFiscale: codiceFiscaleEnte,
          denominazione: enteDenominazione,
          descrizione: enteDenominazione
        },
        this.annoBilancio ?? undefined,
        this.faseBilancio ?? undefined
      );
    } else {
      throw new Error(`Utente non ha accesso all'ente: ${codiceFiscaleEnte}`);
    }
  }

  /**
   * Ottiene il codice fiscale ente corrente
   */
  getCurrentEnte(): string | null {
    return this.currentEnte;
  }

  /**
   * Ottiene gli enti disponibili per l'utente
   */
  getAvailableEnti(): any[] {
    return [...this.availableEnti];
  }

  /**
   * Ottiene le informazioni sull'utente
   */
  getCurrentUser(): UserInfo | null {
    return this.currentUser;
  }

  /**
   * Verifica se l'utente è autenticato
   */
  isAuthenticated(): boolean {
    return this.currentUser !== null;
  }

  /**
   * Verifica se l'utente è autenticato in modo asincrono
   * Utile quando si vuole attendere che il servizio sia completamente inizializzato
   */
  async isAuthenticatedAsync(): Promise<boolean> {
    // Se l'utente è già autenticato, ritorna immediatamente
    if (this.currentUser !== null) {
      return true;
    }

    // Altrimenti, attendi un breve periodo e controlla di nuovo
    return new Promise<boolean>((resolve) => {
      const checkInterval = setInterval(() => {
        if (this.currentUser !== null) {
          clearInterval(checkInterval);
          resolve(true);
        } else {
          // Controlla per un massimo di 5 secondi
          const now = Date.now();
          if (now - this.loadStartTime > 5000) { // 5 secondi
            clearInterval(checkInterval);
            resolve(false);
          }
        }
      }, 50); // Controlla ogni 50ms
    });
  }

  private loadStartTime: number = Date.now();
}