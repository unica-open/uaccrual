/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable, NgZone } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, EMPTY, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';
import { AuthStorageService } from '../services/auth-storage.service';
import { AuthContextService } from '../services/auth-context.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthenticationService,
    private router: Router,
    private zone: NgZone,
    private authStorage: AuthStorageService,
    private authContext: AuthContextService
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();

    // Clona la richiesta per aggiungere gli header
    let authReq = req;

    // Aggiungi il token JWT alle richieste verso i backend
    if (req.url.includes('/accrualsbsrv/') || req.url.includes('/accrualauthsbapi/') || req.url.includes('/accrualsbapi/') || req.url.includes('/accrualreportsbapi/')) {
      const headers: Record<string, string> = {};
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      authReq = req.clone({ setHeaders: headers });
    }

    // Esegui la richiesta e gestisci eventuali errori di autorizzazione
    return next.handle(authReq).pipe(
      tap({
        error: (error: HttpErrorResponse) => {
          // Logga errori per debugging
          if (error.status === 401 || error.status === 403) {
            console.error('[AuthInterceptor] Errore autorizzazione:', {
              status: error.status,
              url: req.url,
              message: error.message
            });
          }
        }
      }),
      catchError((error: HttpErrorResponse) => {
        // Gestisci sia 401 (Unauthorized) che 403 (Forbidden) come token scaduto/non valido
        // MA NON per le richieste di login (che possono fallire con 401 per credenziali sbagliate)
        if ((error.status === 401 || error.status === 403) && !req.url.includes('/login')) {
          // Logga errore prima di redirect
          console.error('[AuthInterceptor] Sessione scaduta o non autorizzata', {
            status: error.status,
            url: req.url,
            message: error.message
          });

          // Pulisci sessione
          this.authStorage.clear();
          this.authService.clearAuth();

          // Evita loop di redirect se siamo già su session-expired o login
          const currentUrl = this.router.url;
          if (currentUrl.includes('/session-expired') || currentUrl.includes('/login')) {
            // Propaga errore per componenti
            return throwError(() => error);
          }

          // Esegui il redirect dentro NgZone per forzare il change detection
          this.zone.run(() => {
            this.router.navigate(['/session-expired'], {
              queryParams: {
                provider: this.authService.getUserInfo()?.['source'] || 'EXT_SPRING',
                returnUrl: currentUrl
              }
            });
          });

          // Propaga errore per componenti
          return throwError(() => error);
        }

        // Per altri errori, propaga l'errore normalmente
        return throwError(() => error);
      })
    );
  }
}
