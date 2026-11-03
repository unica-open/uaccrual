/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient, HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';

// Importa PrimeNG MessageService e ToastModule
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

// Importa NGXS
import { NgxsModule } from '@ngxs/store';
import { NgxsLoggerPluginModule } from '@ngxs/logger-plugin';
import { Store } from '@ngxs/store';
import { SetCurrentEnte, SetCurrentEnteDenominazione, SetAnnoBilancio, SetFaseBilancio, SetUserInfo } from './core/store/context/context.state';
import { JwtHelper } from './core/utils/jwt.helper';
import { AuthStorageService } from './core/services/auth-storage.service';
import { PdcState } from './core/store/pdc/pdc.state';
import { ContextState } from './core/store/context/context.state';
import { CausaleState } from './core/store/causale/causale.state';
import { AsyncReportState } from './features/report/state/async-report.state';
import { environment } from '../environments/environment';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { UnauthorizedComponent } from './shared/components/unauthorized/unauthorized.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { SidebarLayoutComponent } from './shared/components/sidebar-layout/sidebar-layout.component';
import { PageNotificationBannerComponent } from './shared/components/page-notification-banner/page-notification-banner.component';
import { AuthModule } from './features/auth/auth.module';

import { DevAuthenticationService } from './core/services/dev-authentication.service';
import { AuthenticationService } from './core/services/authentication.service';
import { AuthContextService } from './core/services/auth-context.service';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';
import { ConfirmationInterceptor } from './core/interceptors/confirmation.interceptor';

/**
 * Factory per APP_INITIALIZER in modalità sviluppo.
 * Usa DevAuthenticationService per caricare dati da dev-info.json.
 * 
 * @param devAuthService Servizio di autenticazione dev
 * @returns Funzione di inizializzazione
 */
export function devAppInitializer(devAuthService: DevAuthenticationService) {
  return () => {
    console.log('[AppModule] ===== DEV APP INITIALIZER =====');
    console.log('[AppModule] Chiamo DevAuthenticationService.loadDevUserInfo()...');
    const result = devAuthService.loadDevUserInfo();
    console.log('[AppModule] loadDevUserInfo() avviata, Promise:', result);
    return result;
  };
}

/**
 * Factory per APP_INITIALIZER in modalità produzione.
 * Ripristina il contesto da sessionStorage se presente.
 * Il bootstrap SSO Shibboleth è gestito da ShibbolethComponent sulla route /auth/shibboleth.
 *
 * @param http HttpClient per chiamate HTTP
 * @param authContextService Servizio di contesto autenticazione
 * @param store NGXS Store
 * @returns Funzione di inizializzazione
 */
export function prodAppInitializer(
  http: HttpClient,
  authContextService: AuthContextService,
  store: Store,
  authStorage: AuthStorageService
) {
  return async () => {
    const restored = authContextService.loadFromStorage();

    if (!restored) return;

    const ente = authContextService.getCurrentEnte();
    const anno = authContextService.getAnnoBilancio();
    const fase = authContextService.getFaseBilancio();

    if (ente) {
      store.dispatch(new SetCurrentEnte(ente.codiceFiscale));
      store.dispatch(new SetCurrentEnteDenominazione(ente.denominazione || ente.codiceFiscale));
    }
    if (anno) store.dispatch(new SetAnnoBilancio(anno));
    if (fase) store.dispatch(new SetFaseBilancio(fase));

    const userInfo = authStorage.getUserInfo();
    if (userInfo) {
      store.dispatch(new SetUserInfo(userInfo.codiceFiscale, userInfo.nome, userInfo.ruolo, userInfo.utenteCodice ?? null));
    }
  };
}

/**
 * Factory conditionale per APP_INITIALIZER.
 * Seleziona la factory appropriata in base all'ambiente.
 *
 * @param http HttpClient per bootstrap Shibboleth
 * @param devAuthService Servizio dev
 * @param authContextService Servizio contesto
 * @param store NGXS Store
 * @returns Funzione di inizializzazione
 */
export function appInitializerFactory(
  http: HttpClient,
  devAuthService: DevAuthenticationService,
  authContextService: AuthContextService,
  store: Store,
  authStorage: AuthStorageService
) {
  // Log di debug estremo
  console.log('[AppModule-DEBUG] ===== APP_INITIALIZER FACTORY =====');
  console.log('[AppModule-DEBUG] environment.production:', environment.production);
  console.log('[AppModule-DEBUG] window.location.href:', window.location.href);
  console.log('[AppModule-DEBUG] window.location.pathname:', window.location.pathname);
  
  // Se in modalità sviluppo, usa dev initializer
  if (!environment.production) {
    console.log('[AppModule] Modalità sviluppo: uso DevAuthenticationService');
    return devAppInitializer(devAuthService);
  }

  // Altrimenti usa production initializer
  console.log('[AppModule] Modalità produzione: uso bootstrap Shibboleth');
  console.log('[AppModule-DEBUG] Chiamata a prodAppInitializer...');
  return prodAppInitializer(http, authContextService, store, authStorage);
}

@NgModule({
  declarations: [
    AppComponent,
    UnauthorizedComponent,
    SidebarComponent
  ],
  imports: [
    BrowserModule,
    CommonModule, // Aggiunto CommonModule esplicitamente
    RouterModule, // Aggiunto RouterModule esplicitamente
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule, // Richiesto per le animazioni di Angular Material

    // Configurazione di NGXS
    NgxsModule.forRoot([PdcState, ContextState, CausaleState, AsyncReportState], {
      developmentMode: !environment.production // Abilita controlli aggiuntivi in sviluppo
    }),
    NgxsLoggerPluginModule.forRoot({
      disabled: environment.production // Disabilita il logger in produzione
    }),

    // Moduli UI
    MatButtonModule,
    MatToolbarModule,
    MatIconModule,
    MatSnackBarModule,

    // Modulo Auth
    AuthModule,
    ToastModule,
    SidebarLayoutComponent,
    PageNotificationBannerComponent
  ],
  providers: [
    DatePipe,
    MessageService,
    // APP_INITIALIZER conditionale basato su ambiente
    {
      provide: APP_INITIALIZER,
      useFactory: appInitializerFactory,
      deps: [HttpClient, DevAuthenticationService, AuthContextService, Store, AuthStorageService],
      multi: true
    },
    // Interceptors
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ConfirmationInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
