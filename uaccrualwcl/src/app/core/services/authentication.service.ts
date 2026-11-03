/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, throwError, of, map } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Store } from '@ngxs/store';
import { LoadContext, SetCurrentEnte, SetCurrentEnteDenominazione, SetFaseBilancio, SetUserInfo, SetAnnoBilancio } from '../store/context/context.state';
import { UserInfo } from './user-info.interface';
import { JwtHelper } from '../utils/jwt.helper';
import { AuthStorageService } from './auth-storage.service';
import { AuthContextService } from './auth-context.service';
import { AuthProvider } from './providers/auth-provider.interface';
import { CredentialsProvider } from './providers/credentials.provider';
import { SessionProvider } from './providers/session.provider';
import { DevProvider } from './providers/dev.provider';
import { AuthProviderType, AuthProviderUtils } from '../models/auth-provider.type';
import { LoginRequest, LoginResponse, UserInfoComplete } from '../models/auth.models';
import { Ente } from '../models/ente.model';
import { environment } from '../../../environments/environment';

export type { LoginRequest, LoginResponse } from '../models/auth.models';

export interface ResetPasswordRequest {
  codiceFiscale: string;
}

export interface ConfirmPasswordResetRequest {
  token: string;
  codiceFiscale: string;
  password: string;
  confirmPassword: string;
}

export interface ChangePasswordRequest {
  codiceFiscale: string;
  vecchiaPassword?: string;
  nuovaPassword: string;
  confermaPassword: string;
}

/**
 * Servizio di autenticazione refactored.
 * 
 * Agisce come orchestratore che:
 * - Inietta tutti i provider disponibili
 * - Seleziona il provider appropriato in base al contesto
 * - Delega login/logout al provider selezionato
 * - Coordina con AuthContextService per la gestione del contesto
 * 
 * @see AuthProvider - Interfaccia per i provider
 * @see AuthContextService - Gestione del contesto
 */
@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private currentUser: UserInfo | null = null;
  private currentEnte: string | null = null;
  private availableEnti: Ente[] = [];

  private enteSubject = new BehaviorSubject<string | null>(null);
  public ente$ = this.enteSubject.asObservable();

  private readonly apiUrl = `${environment.services.accrualAuthApiUrl}`;

  constructor(
    private http: HttpClient,
    private store: Store,
    private jwtHelper: JwtHelper,
    private authStorage: AuthStorageService,
    private authContext: AuthContextService,
    private credentialsProvider: CredentialsProvider,
    private sessionProvider: SessionProvider,
    private devProvider: DevProvider
  ) {}

    /**
     * Seleziona il provider di autenticazione più appropriato.
     * 
     * Priorità:
     * 1. SessionProvider (100) - se JWT valido in sessionStorage
     * 2. CredentialsProvider (10) - fallback per login manuale
     * 3. DevProvider (1) - solo in sviluppo
     * 
     * @param externalProvider Provider specifico richiesto (opzionale)
     * @returns AuthProvider selezionato
   * 
   * @example
   * // Selezione automatica
   * const provider = authService.selectProvider();
   * 
   * // Provider specifico da query param
   * const provider = authService.selectProvider('SHIBBOLETH');
   */
  selectProvider(externalProvider?: AuthProviderType): AuthProvider {
    // Se un provider esterno è specificato, usa quello
    if (externalProvider) {
      switch (externalProvider) {
        case 'SPRING_SECURITY':
          return this.credentialsProvider;
        case 'TOKEN':
          return this.sessionProvider;
        case 'DEV':
          return this.devProvider;
      }
    }

    // Altrimenti, seleziona in base alla priorità
    const providers: AuthProvider[] = [
      this.sessionProvider,
      this.credentialsProvider,
      this.devProvider
    ];

    // Ordina per priorità decrescente
    providers.sort((a, b) => b.getPriority() - a.getPriority());

    // Ritorna il primo provider disponibile
    for (const provider of providers) {
      const available = provider.isAvailable();
      
      // Gestisce sia boolean che Observable<boolean>
      if (typeof available === 'boolean' && available) {
        console.log('[AuthenticationService] Provider selezionato:', provider.getName());
        return provider;
      }
    }

    // Fallback a credentials provider
    console.log('[AuthenticationService] Fallback a CredentialsProvider');
    return this.credentialsProvider;
  }

  /**
   * Effettua il login con il provider selezionato.
   * 
   * @param loginRequest Richiesta di login
   * @returns Observable di LoginResponse
   * 
   * @example
   * // Login con credenziali
   * authService.login({
   *   codiceFiscale: 'RSSMRA80A01H501U',
   *   password: 'password123',
   *   provider: 'SpringSecurity'
   * });
   * 
   * // Login con token Shibboleth
   * authService.login({
   *   token: 'external-token',
   *   provider: 'Shibboleth'
   * });
   */
  login(loginRequest: LoginRequest): Observable<LoginResponse> {
    // Determina il provider da usare
    let providerType: AuthProviderType | undefined;
    
    if (loginRequest.provider) {
      try {
        // Normalizza il provider
        const providerString = typeof loginRequest.provider === 'string' 
          ? loginRequest.provider 
          : 'SPRING_SECURITY';
        providerType = AuthProviderUtils.normalize(providerString);
      } catch (error) {
        console.warn('[AuthenticationService] Provider non riconosciuto, uso default');
      }
    }

    // Seleziona il provider
    const provider = this.selectProvider(providerType);
    console.log('[AuthenticationService] Login con provider:', provider.getName());

    // Delega il login al provider
    return provider.login(loginRequest).pipe(
      tap(response => {
        const token = response.token || response.data?.token;

        if (token) {
          // Estrai TUTTI i dati direttamente dal JWT
          const decodedToken = this.jwtHelper.decodeToken(token);

          const codiceEnte = this.jwtHelper.getCodiceEnte(token);
          const annoBilancio = this.jwtHelper.getAnnoBilancio(token);
          const faseBilancio = this.jwtHelper.getFaseBilancio(token);

          // Estrai le informazioni utente dal JWT
          const userInfo: UserInfoComplete = {
            codiceFiscale: decodedToken?.sub || decodedToken?.codiceFiscale || '',
            nome: decodedToken?.cognome || decodedToken?.nome || decodedToken?.name || decodedToken?.displayName || '',
            email: '',
            enti: [{
              codice: codiceEnte || '',
              codiceFiscale: codiceEnte || '',
              denominazione: decodedToken?.denominazione_ente || ''
            }],
            ruolo: decodedToken?.ruolo || decodedToken?.role || '',
            permissions: [],
            source: AuthProviderUtils.toBackendString(providerType || 'SPRING_SECURITY')
          };

          // Usa AuthContextService per impostare il contesto
          // Passa il primo ente come Ente oggetto
          const ente: Ente = userInfo.enti[0];
          this.authContext.setContext(ente, annoBilancio ?? undefined, faseBilancio ?? undefined);

          // Aggiorna NGXS store con ente corrente, denominazione e fase bilancio
          this.store.dispatch(new SetCurrentEnte(ente.codiceFiscale));
          this.store.dispatch(new SetCurrentEnteDenominazione(ente.denominazione || ente.codiceFiscale));
          if (faseBilancio) this.store.dispatch(new SetFaseBilancio(faseBilancio));
          if (annoBilancio != null) this.store.dispatch(new SetAnnoBilancio(annoBilancio));
          const utenteCodice = decodedToken?.utente_codice || null;
          this.store.dispatch(new SetUserInfo(userInfo.codiceFiscale, userInfo.nome, userInfo.ruolo ?? null, utenteCodice));
          // Persiste info utente in sessionStorage per il ripristino al refresh
          this.authStorage.setUserInfo({
            codiceFiscale: userInfo.codiceFiscale,
            nome: userInfo.nome,
            ente: codiceEnte || '',
            ruolo: userInfo.ruolo ?? '',
            utenteCodice: utenteCodice ?? undefined
          });

          // Aggiorna stato locale per backward compatibility
          this.currentUser = {
            username: userInfo.codiceFiscale,
            displayName: userInfo.nome,
            email: '',
            enti: [userInfo.enti[0].codiceFiscale],
            permissions: []
          };

          this.currentEnte = userInfo.enti[0].codiceFiscale;
          this.availableEnti = userInfo.enti;
          this.enteSubject.next(this.currentEnte);
        }
      }),
      catchError(error => {
        console.error('[AuthenticationService] Login fallito:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Effettua il logout con il provider corrente.
   *
   * @returns Observable void
   */
  logout(): Observable<void> {
    // Seleziona il provider corrente (SessionProvider se token presente)
    const provider = this.selectProvider();

    console.log('[AuthenticationService] Logout con provider:', provider.getName());

    return provider.logout().pipe(
      tap(() => {
        this.clearAuth();
      }),
      catchError(error => {
        // Anche in caso di errore, pulisci l'auth locale
        this.clearAuth();
        return throwError(() => error);
      })
    );
  }

  /**
   * Richiede il reset della password
   */
  requestPasswordReset(codiceFiscale: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, {
      codiceFiscale: codiceFiscale
    } as ResetPasswordRequest, { responseType: 'text' });
  }

  /**
   * Conferma il reset della password
   */
  confirmPasswordReset(request: ConfirmPasswordResetRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/confirm-password-reset`, request, { responseType: 'text' });
  }

  /**
   * Cambia la password (primo accesso o cambio volontario)
   */
  changePassword(request: ChangePasswordRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/change-password`, request, { responseType: 'text' });
  }

  /**
   * Ottiene il token corrente
   */
  getToken(): string | null {
    return this.authContext.getToken() || this.authStorage.getToken();
  }

  /** memorizzo il token */
  saveToken(token: string): void {
  	this.authContext.setToken(token);
  	this.authStorage.setToken(token);
  }	

  /**
   * Ottiene le informazioni utente salvate
   */
  getUserInfo(): { codiceFiscale: string; nome: string; ente: string; ruolo: string; source?: string } | null {
    return this.authStorage.getUserInfo();
  }

  /**
   * Verifica se l'utente è autenticato
   */
  isAuthenticated(): boolean {
    return this.authContext.isAuthenticated();
  }

  /**
   * Verifica se l'utente è autenticato in modo asincrono
   */
  async isAuthenticatedAsync(): Promise<boolean> {
    return this.isAuthenticated();
  }

  /**
   * Verifica se il token corrente sta per scadere (meno di 5 minuti)
   */
  isTokenExpiring(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const timeRemaining = this.jwtHelper.getTokenTimeRemaining(token);
    return timeRemaining < 300; // Meno di 5 minuti
  }

  /**
   * Tenta di refreshare il token se sta per scadere
   * TODO: Implementare quando disponibile su accrualauthsbapi
   */
  async refreshToken(): Promise<boolean> {
    const token = this.getToken();
    if (!token) return false;

    // Per ora non implementato
    return false;
  }

  /**
   * Verifica se è il primo accesso dell'utente
   */
  isFirstAccess(): boolean {
    const userInfo = this.getUserInfo();
    return false; // TODO: Implementare se necessario
  }

  /**
   * Imposta l'ente corrente
   */
  setEnte(codiceFiscaleEnte: string): void {
    // Verifica che l'ente sia tra quelli disponibili
    const enteFound = this.availableEnti.find(e => e.codiceFiscale === codiceFiscaleEnte);

    if (!enteFound) {
      throw new Error(`Utente non ha accesso all'ente: ${codiceFiscaleEnte}`);
    }

    this.currentEnte = codiceFiscaleEnte;
    this.enteSubject.next(codiceFiscaleEnte);

    // Usa AuthContextService per impostare l'ente
    this.authContext.setCurrentEnte(codiceFiscaleEnte);

    // Aggiorna NGXS store con ente corrente e denominazione
    this.store.dispatch(new SetCurrentEnte(codiceFiscaleEnte));
    this.store.dispatch(new SetCurrentEnteDenominazione(enteFound.denominazione || codiceFiscaleEnte));
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
  getAvailableEnti(): string[] {
    return this.availableEnti.map(e => e.codiceFiscale);
  }

  /**
   * Ottiene le informazioni sull'utente
   */
  getCurrentUser(): UserInfo | null {
    return this.currentUser;
  }

  /**
   * Pulisce le informazioni di autenticazione
   */
  clearAuth(): void {
    this.authContext.clearContext();
    this.currentUser = null;
    this.currentEnte = null;
    this.availableEnti = [];
    this.enteSubject.next(null);
  }

  /**
   * Inizializza l'autenticazione da sessionStorage (al refresh della pagina)
   * Delegato ad AuthContextService
   * 
   * @deprecated Usare AuthContextService.loadFromStorage()
   */
  initAuthFromStorage(): void {
    console.log('[AuthenticationService] initAuthFromStorage deprecato, uso AuthContextService');
    this.authContext.loadFromStorage();
  }

  /**
   * Ripristina la sessione da sessionStorage.
   * Metodo helper che usa SessionProvider se disponibile.
   * 
   * @returns Observable boolean - true se sessione ripristinata
   */
  restoreSession(): Observable<boolean> {
    if (this.sessionProvider.isAvailable()) {
      console.log('[AuthenticationService] Ripristino sessione da SessionProvider');

      return this.sessionProvider.login({ provider: 'TOKEN' }).pipe(
        tap(response => {
          const token = response.token || response.data?.token;
          if (token) {
            const decodedToken = this.jwtHelper.decodeToken(token);
            
            const userInfo: UserInfoComplete = {
              codiceFiscale: decodedToken?.sub || decodedToken?.codiceFiscale || '',
              nome: decodedToken?.cognome || decodedToken?.nome || decodedToken?.name || '',
              email: '',
              enti: [{
                codice: this.jwtHelper.getCodiceEnte(token) || '',
                codiceFiscale: this.jwtHelper.getCodiceEnte(token) || '',
                denominazione: decodedToken?.denominazione_ente || ''
              }],
              ruolo: decodedToken?.ruolo || '',
              permissions: [],
              source: 'Session'
            };

            // Usa AuthContextService per impostare il contesto
            const ente: Ente = userInfo.enti[0];
            const annoBilancio = this.jwtHelper.getAnnoBilancio(token);
            const faseBilancio = this.jwtHelper.getFaseBilancio(token);
            this.authContext.setContext(ente, annoBilancio ?? undefined, faseBilancio ?? undefined);
            if (faseBilancio) this.store.dispatch(new SetFaseBilancio(faseBilancio));
            this.store.dispatch(new SetUserInfo(userInfo.codiceFiscale, userInfo.nome, userInfo.ruolo ?? null, decodedToken?.utente_codice || null));

            // Aggiorna stato locale
            this.currentUser = {
              username: userInfo.codiceFiscale,
              displayName: userInfo.nome,
              email: '',
              enti: [userInfo.enti[0].codiceFiscale],
              permissions: []
            };

            this.currentEnte = userInfo.enti[0].codiceFiscale;
            this.availableEnti = userInfo.enti;
            this.enteSubject.next(this.currentEnte);
          }
        }),
        map(() => true),
        catchError(error => {
          console.error('[AuthenticationService] Ripristino sessione fallito:', error);
          return of(false);
        })
      );
    }
    
    return of(false);
  }
}
