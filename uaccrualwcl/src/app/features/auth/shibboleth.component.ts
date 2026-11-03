/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Store } from '@ngxs/store';
import { AuthContextService } from '../../core/services/auth-context.service';
import { AuthStorageService } from '../../core/services/auth-storage.service';
import { AzioniService } from '../../core/services/azioni.service';
import {
  SetCurrentEnte,
  SetCurrentEnteDenominazione,
  SetAnnoBilancio,
  SetFaseBilancio,
  SetUserInfo
} from '../../core/store/context/context.state';

@Component({
  selector: 'app-shibboleth',
  imports: [CommonModule, RouterModule],
  template: `
<div class="login-container">
  <div class="login-card">
    <div class="login-header">
      <h1>Accrual - Login</h1>
      <p>Accedi al sistema di gestione Accrual</p>
    </div>
    <div class="login-form">
      <div class="spinner-wrap" aria-hidden="true">
        <div class="spinner"></div>
      </div>
      <div class="h1" *ngIf="!isError">Autenticazione Shibboleth in corso</div>
      <div class="h1" *ngIf="isError">Accesso non riuscito</div>

      <div class="p" *ngIf="!isError">
        Stiamo completando l'autenticazione Shibboleth e configurando la tua sessione.
        <br />Sarai reindirizzato a breve.
      </div>

      <div class="p" *ngIf="isError">
        {{ message }}<br />
        Verrai reindirizzato alla pagina di accesso.
      </div>

      <div class="dots" *ngIf="!isError" aria-hidden="true">
        <span></span><span></span><span></span>
      </div>

      <div class="hint" *ngIf="!isError">
        Se il reindirizzamento non avviene, attendi qualche secondo e non chiudere questa finestra.
      </div>
    </div>
  </div>
</div>
  `,
  styles: [`
    :host {
      display: block;
      height: 100%;
    }

    :host ::ng-deep body {
      overflow: hidden;
    }

    .spinner-wrap {
      width: 64px;
      height: 64px;
      margin: 8px auto 14px;
      display: grid;
      place-items: center;
    }

    .spinner {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      border: 6px solid rgba(34, 71, 255, 0.16);
      border-top-color: rgba(34, 71, 255, 0.95);
      animation: spin 900ms linear infinite;
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }

    .h1 {
      text-align: center;
      margin: 2px 0 10px;
      font-size: 1.25rem;
      font-weight: 800;
      color: #0a2a5a;
    }

    .p {
      text-align: center;
      margin: 0 0 10px;
      font-size: 0.95rem;
      line-height: 1.4;
      opacity: 0.9;
      color: #a32323;
    }

    .dots {
      display: inline-flex;
      gap: 8px;
      align-items: center;
      justify-content: center;
      margin-top: 6px;
    }

    .dots span {
      width: 7px;
      height: 7px;
      border-radius: 999px;
      background: rgba(34, 71, 255, 0.8);
      animation: dot 900ms ease-in-out infinite;
    }

    .dots span:nth-child(2) {
      animation-delay: 150ms;
      opacity: 0.8;
    }

    .dots span:nth-child(3) {
      animation-delay: 300ms;
      opacity: 0.7;
    }

    @keyframes dot {
      0%,
      100% {
        transform: translateY(0);
        opacity: 0.55;
      }
      50% {
        transform: translateY(-5px);
        opacity: 1;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .spinner,
      .dots span {
        animation: none !important;
      }
    }
  `]
})
export class ShibbolethComponent implements OnInit {
  message: string | null = null;
  isError = false;
  loading = false;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private authContextService: AuthContextService,
    private store: Store,
    private authStorage: AuthStorageService,
    private azioniService: AzioniService
  ) {}

  async ngOnInit(): Promise<void> {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/';
    const anno = this.route.snapshot.queryParamMap.get('anno');
    const fase = this.route.snapshot.queryParamMap.get('fase') || 'APERTO';
    const ente = this.route.snapshot.queryParamMap.get('ente');

    if (returnUrl.includes('auth/shibboleth')) {
      this.handleError('Redirect circolare rilevato');
      return;
    }

    try {
      let url = '/accrualwcl/api/shibboleth-login';
      const params = new URLSearchParams();
      params.set('anno', '2025');
      params.set('fase', fase);
      if (ente) params.set('codiceEnte', ente);
      const qs = params.toString();
      if (qs) url += '?' + qs;

      const response: any = await firstValueFrom(
        this.http.get(url, { withCredentials: true })
      );

      if (response && response.token) {
        this.authContextService.setToken(response.token);
        const loaded = this.authContextService.loadFromToken(response.token);

        if (loaded) {
          const ente = this.authContextService.getCurrentEnte();
          const annoBilancio = this.authContextService.getAnnoBilancio();
          const faseBilancio = this.authContextService.getFaseBilancio();

          if (ente) {
            this.store.dispatch(new SetCurrentEnte(ente.codiceFiscale));
            this.store.dispatch(new SetCurrentEnteDenominazione(ente.denominazione || ente.codiceFiscale));
          }
          if (annoBilancio) this.store.dispatch(new SetAnnoBilancio(annoBilancio));
          if (faseBilancio) this.store.dispatch(new SetFaseBilancio(faseBilancio));

          const userInfo = this.authStorage.getUserInfo();
          if (userInfo) {
            this.store.dispatch(new SetUserInfo(userInfo.codiceFiscale, userInfo.nome, userInfo.ruolo, userInfo.utenteCodice));
          }

          // Carica i permessi prima di navigare (come nel flusso credentials)
          try {
            await firstValueFrom(this.azioniService.loadPermissions());
          } catch (permErr) {
            console.warn('[ShibbolethComponent] Caricamento permessi fallito, si procede comunque:', permErr);
          }
        }

        this.router.navigateByUrl(returnUrl);
      } else {
        this.handleError('Token non ricevuto dal server');
      }
    } catch (err: any) {
      const backendMessage = err?.error?.message || err?.error || err?.message || null;
      const displayMessage = (typeof backendMessage === 'string' && backendMessage.length > 0)
        ? backendMessage
        : 'Autenticazione Shibboleth non riuscita.';
      this.handleError(displayMessage);
    }
  }

  private handleError(message: string): void {
    this.isError = true;
    this.message = message;
    window.setTimeout(() => this.router.navigate(['/login'], { queryParams: { shib_error: message } }), 900);
  }
}
