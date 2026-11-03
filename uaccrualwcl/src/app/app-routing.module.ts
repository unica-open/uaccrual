/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PageNotFoundComponent } from './components/page-not-found/page-not-found.component';
import { AuthGuard } from './core/services/auth.guard';
import { PublicGuard } from './core/guards/public.guard';
import { ShibbolethComponent } from './features/auth/shibboleth.component';
import { UnauthorizedComponent } from './shared/components/unauthorized/unauthorized.component';
import { LoginComponent } from './features/auth/login/login.component';
import { ResetPasswordComponent } from './features/auth/reset-password/reset-password.component';
import { ConfirmPasswordResetComponent } from './features/auth/confirm-password-reset/confirm-password-reset.component';
import { ChangePasswordComponent } from './features/auth/change-password/change-password.component';
import { SessionExpiredComponent } from './features/auth/session-expired/session-expired.component';

const routes: Routes = [
  // Rotte di autenticazione (pubbliche - usano PublicGuard per pulire sessionStorage)
  { path: 'login', component: LoginComponent, canActivate: [PublicGuard] },
  { path: 'reset-password', component: ResetPasswordComponent, canActivate: [PublicGuard] },
  { path: 'confirm-password-reset', component: ConfirmPasswordResetComponent, canActivate: [PublicGuard] },
  { path: 'change-password', component: ChangePasswordComponent, canActivate: [PublicGuard] },

  // Rotta per sessione scaduta (pubblica - usano PublicGuard per pulire sessionStorage)
  { path: 'session-expired', component: SessionExpiredComponent, canActivate: [PublicGuard] },

  // Rotta per utenti non autorizzati
  { path: 'unauthorized', component: UnauthorizedComponent, canActivate: [PublicGuard] },

  // Rotta per bootstrap autenticazione Shibboleth (nessun guard — come /callback)
  { path: 'auth/shibboleth', component: ShibbolethComponent },

  // Rotta di default, reindirizza a /home
  { path: '', redirectTo: '/home', pathMatch: 'full' },

  // Aggiungi il guard a tutte le rotte che richiedono autenticazione
  {
    path: 'pdc-esempio', // Prefisso per tutte le rotte relative all'entità PdcEsempio
    loadChildren: () => import('./features/pdc-esempio/pdc-esempio.module').then(m => m.PdcEsempioModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'pdc',
    loadChildren: () => import('./features/pdc/pdc.module').then(m => m.PdcModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'causale',
    loadChildren: () => import('./features/causale-accrual/causale-accrual.module').then(m => m.CausaleAccrualModule),
    canActivate: [AuthGuard]
  },
  { path: 'primanota', loadChildren: () => import('./features/primanota/primanota.module').then(m => m.PrimanotaModule), canActivate: [AuthGuard] },
  {
    path: 'flusso',
    loadChildren: () => import('./features/flusso/flusso.module').then(m => m.FlussoModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'home',
    loadChildren: () => import('./features/home/home.module').then(m => m.HomeModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'report',
    loadChildren: () => import('./features/report/report.module').then(m => m.ReportModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'modellomif',
    loadChildren: () => import('./features/modellomif/modellomif.module').then(m => m.ModellomifModule),
    canActivate: [AuthGuard]
  },
  { path: 'home', loadChildren: () => import('./features/home/home.module').then(m => m.HomeModule) },
  // ... altre rotte per altre funzionalità
  { path: '**', component: PageNotFoundComponent, canActivate: [AuthGuard] } // Rotta wildcard per gestire URL non trovati
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
