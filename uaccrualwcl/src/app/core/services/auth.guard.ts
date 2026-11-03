/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError, tap } from 'rxjs/operators';
import { Store } from '@ngxs/store';
import { AuthenticationService } from './authentication.service';
import { AuthStorageService } from './auth-storage.service';
import { AuthProviderType, AuthProviderUtils } from '../models/auth-provider.type';
import { JwtHelper } from '../utils/jwt.helper';
import { AuthContextService } from './auth-context.service';
import { SetFaseBilancio, ContextState } from '../store/context/context.state';
import { AzioniService } from './azioni.service';

/**
 * Guard per la protezione delle rotte autenticate.
 *
 * Gestisce:
 * - Login con token esterno (Shibboleth)
 * - Caching JWT (evita login multipli per stesso ente)
 * - Normalizzazione provider da query param
 * - Estrazione ente da JWT se non fornito
 * - Redirect a login o session-expired
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthenticationService,
    private router: Router,
    private authStorage: AuthStorageService,
    private jwtHelper: JwtHelper,
    private authContextService: AuthContextService,
    private store: Store,
    private azioniService: AzioniService
  ) {}

  /**
   * Estrae il path target dalla rotta per il redirect post-login.
   * 
   * @param route Rotta attivata
   * @returns Path completo da utilizzare per il redirect
   */
  private getTargetPath(route: ActivatedRouteSnapshot): string {
    const pathSegments: string[] = [];
    let currentRoute: ActivatedRouteSnapshot | null = route;

    while (currentRoute) {
      if (currentRoute.routeConfig && currentRoute.routeConfig.path) {
        const segment = currentRoute.routeConfig.path;
        // Ignora i path vuoti e quelli con parametri dinamici
        if (segment && !segment.startsWith(':')) {
          pathSegments.unshift(segment);
        }
      }
      currentRoute = currentRoute.parent;
    }

    const path = '/' + pathSegments.join('/');
    console.log('[AuthGuard] Target path from route:', path);

    return path || '/home';
  }

  /**
   * Normalizza il provider da query param in tipo forte.
   * 
   * @param raw Stringa grezza da query param
   * @returns AuthProviderType normalizzato o undefined se non valido
   */
  private normalizeProvider(raw: string | null): AuthProviderType | undefined {
    if (!raw) {
      return undefined;
    }

    try {
      return AuthProviderUtils.normalize(raw);
    } catch (error) {
      console.warn('[AuthGuard] Provider non riconosciuto:', raw);
      return undefined;
    }
  }

  /**
   * Verifica se due token appartengono allo stesso utente/ente.
   * 
   * @param token1 Primo token JWT
   * @param token2 Secondo token JWT
   * @returns true se stessi utente ed ente
   */
  private isSameSession(token1: string, token2: string): boolean {
    const codiceFiscale1 = this.jwtHelper.decodeToken(token1)?.sub;
    const codiceFiscale2 = this.jwtHelper.decodeToken(token2)?.sub;
    const ente1 = this.jwtHelper.getCodiceEnte(token1);
    const ente2 = this.jwtHelper.getCodiceEnte(token2);

    return codiceFiscale1 === codiceFiscale2 && ente1 === ente2;
  }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> {
    console.log('[AuthGuard] ===== CHECK AUTH GUARD =====');
    console.log('[AuthGuard] URL richiesta:', route.routeConfig?.path);
    console.log('[AuthGuard] URL completa:', window.location.href);
    console.log('[AuthGuard] window.location.pathname:', window.location.pathname);
    
    // Controllo se il bootstrap è completo
    const token = this.authService.getToken();
    console.log('[AuthGuard] Token presente al momento del check:', !!token);
    console.log('[AuthGuard] Lunghezza token:', token ? token.length : 0);
    
    // Estrai parametri da query string
    const externalToken = route.queryParams['token'];
    const rawProvider = route.queryParams['provider'];
    const provider = this.normalizeProvider(rawProvider);
    let enteCodiceUnivoco = route.queryParams['enteCodiceUnivoco'];
    const annoBilancio = route.queryParams['annoBilancio'];
    const faseBilancio = route.queryParams['faseBilancio'];

    // ==================== CASO 1: Token esterno presente ====================
    if (externalToken) {
      const targetPath = this.getTargetPath(route);
      console.log('[AuthGuard] External token login - Target path:', targetPath);

      // Se ente non fornito, estrai da JWT
      if (!enteCodiceUnivoco) {
        enteCodiceUnivoco = this.jwtHelper.getCodiceEnte(externalToken);
        if (!enteCodiceUnivoco) {
          console.warn('[AuthGuard] Ente non presente nel token esterno e non fornito nei parametri');
          this.authStorage.clear();
          return of(this.router.createUrlTree(['/unauthorized'], {
            queryParams: { error: 'token_invalid' }
          }));
        }
        console.log('[AuthGuard] Ente estratto da JWT:', enteCodiceUnivoco);
      }

      // Controlla caching JWT
      const currentToken = this.authService.getToken();

      if (currentToken && !this.jwtHelper.isTokenExpired(currentToken)) {
        // Token corrente valido, verifica se stesso utente/ente
        const currentEnte = this.jwtHelper.getCodiceEnte(currentToken);
        const newEnte = this.jwtHelper.getCodiceEnte(externalToken);

        if (currentEnte === newEnte && this.isSameSession(currentToken, externalToken)) {
          // Stessa sessione, salta login e procedi
          console.log('[AuthGuard] Caching JWT: stessa sessione, salto login');
          return of(true);
        }

        // Ente diverso o utente diverso, pulisci sessione precedente
        console.log('[AuthGuard] Token esistente rilevato, pulisco la sessione prima di fare login con nuovo utente/ente');
        this.authService.clearAuth();
      }

      // Effettua login con token esterno
      return this.authService.login({
        token: externalToken,
        provider: provider || 'SHIBBOLETH',
        enteCodiceUnivoco: enteCodiceUnivoco,
        annoBilancio: annoBilancio ? Number(annoBilancio) : undefined,
        faseBilancio: faseBilancio
      }).pipe(
        tap(response => {
          console.log('[AuthGuard] Login response:', response);
        }),
        map(response => {
          // Login effettuato con successo
          if (response && response.token) {
            this.authService.saveToken(response.token);
            console.log('[AuthGuard] Login successful - Redirecting to:', targetPath);
            this.router.navigateByUrl(targetPath, { replaceUrl: true });
            return true;
          }
          
          // Nessun token, pulisci e redirect a session-expired
          this.authStorage.clear();
          return this.router.createUrlTree(['/session-expired'], {
            queryParams: { error: 'auth_failed' }
          });
        }),
        catchError(error => {
          // Gestisci errori specifici
          const errorMsg = error?.error?.message || error?.message || '';
          const isUserNotFound = error?.status === 404 ||
                                 error?.status === 400 ||
                                 (error?.status === 401 && (
                                   errorMsg.includes('NotFoundException') ||
                                   errorMsg.includes('non ha accesso') ||
                                   errorMsg.includes('non trovato')
                                 ));

          // Pulisci e redirect
          this.authStorage.clear();
          return of(this.router.createUrlTree(['/session-expired'], {
            queryParams: {
              error: isUserNotFound ? 'user_not_found' : 'auth_failed'
            }
          }));
        })
      );
    }

    // ==================== CASO 2: Utente già autenticato ====================
    const isAuth = this.authService.isAuthenticated();
    console.log('[AuthGuard] Controllo isAuthenticated():', isAuth);
    console.log('[AuthGuard] Token presente:', !!this.authService.getToken());
    
    if (isAuth) {
      console.log('[AuthGuard] ACCESSO CONSENTITO - Utente autenticato');
      console.log('[AuthGuard] Utente già autenticato, accesso consentito');
      const storedToken = this.authStorage.getToken();
      if (storedToken) {
        const faseBilancio = this.jwtHelper.getFaseBilancio(storedToken);
        if (faseBilancio) this.store.dispatch(new SetFaseBilancio(faseBilancio));
      }
      // Carica i permessi se non ancora presenti (es. dopo page refresh)
      const permissions = this.store.selectSnapshot(ContextState.getUserPermissions);
      if (permissions.length === 0) {
        return this.azioniService.loadPermissions().pipe(
          map(() => true as const),
          catchError((err) => {
            console.error('[AuthGuard] Caricamento permessi fallito:', err?.status, err?.message || err);
            return of(true as const);
          })
        );
      }
      return of(true);
    }

    // ==================== CASO 3: Token scaduto o assente ====================
    const expiredToken = this.authStorage.getToken();
    console.log('[AuthGuard] Token scaduto presente:', !!expiredToken);
    console.log('[AuthGuard] Controllo: è possibile che APP_INITIALIZER non sia completato?');
    console.log('[AuthGuard] Se sei arrivato qui senza token, il bootstrap SSO è fallito');
    
    if (expiredToken) {
      // Token presente ma scaduto
      console.log('[AuthGuard] Token scaduto rilevato');
      this.authStorage.clear();
      
      return of(this.router.createUrlTree(['/login'], {
        queryParams: { expired: 'true' }
      }));
    }

    // Nessun token, redirect a login
    const returnUrl = route.routeConfig?.path || this.router.url;
    console.log('[AuthGuard] Nessun token valido, redirect a login');
    console.log('[AuthGuard] returnUrl:', returnUrl);
    console.log('[AuthGuard] ==================================================================');
    console.log('[AuthGuard] ERRORE: Il bootstrap SSO non ha impostato il token!');
    console.log('[AuthGuard] Controlla i log [Shibboleth-Bootstrap] e [AppModule-DEBUG]');
    console.log('[AuthGuard] ==================================================================');

    // Non aggiungere returnUrl se è la home
    if (returnUrl === 'home' || returnUrl === '/home') {
      console.log('[AuthGuard] Redirect a /login (home page)');
      return of(this.router.createUrlTree(['/login']));
    }

    console.log('[AuthGuard] Redirect a /login con returnUrl:', returnUrl);
    console.log('[AuthGuard] ===== FINE AUTH GUARD (redirect a login) =====');
    return of(this.router.createUrlTree(['/login'], {
      queryParams: { returnUrl }
    }));
  }
}
