/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { AuthStorageService } from '../services/auth-storage.service';

/**
 * Guard per le pagine pubbliche (login, session-expired, unauthorized)
 * 
 * Quando l'utente accede a queste pagine, pulisce sessionStorage per assicurarsi
 * che non rimangano dati di autenticazione residui.
 * 
 * Uso:
 * - /login
 * - /session-expired
 * - /unauthorized
 */
@Injectable({
  providedIn: 'root'
})
export class PublicGuard implements CanActivate {

  constructor(
    private authStorage: AuthStorageService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> {
    // Pulisci sempre sessionStorage quando si accede a pagine pubbliche
    // Questo previene che dati di autenticazione residui rimangano in memoria
    console.log('[PublicGuard] Pulizia sessionStorage per pagina pubblica:', route.routeConfig?.path);
    this.authStorage.clear();

    // Prosegui normalmente
    return of(true);
  }
}
