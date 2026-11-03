/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PdcSearchComponent } from './components/pdc-search/pdc-search.component';
import { PdcDetailComponent } from './components/pdc-detail/pdc-detail.component';
import { PdcFormComponent } from './components/pdc-form/pdc-form.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'search',
    pathMatch: 'full'
  },
  {
    path: 'search',
    component: PdcSearchComponent
  },
  {
    path: 'results',
    redirectTo: 'search',
    pathMatch: 'full'
  },
  {
    path: 'dettaglio/:codiceUfficiale/:codiceClasse/:codiceFamiglia',
    component: PdcDetailComponent
  },
  {
    path: ':codiceUfficiale/:codiceClasse/:codiceFamiglia',
    component: PdcDetailComponent
  },
  {
    path: 'new/:codiceUfficialePadre/:codiceClassePadre/:codiceFamigliaPadre',
    component: PdcFormComponent,
    data: { mode: 'insert' }
  },
  {
    path: 'edit/:codiceUfficiale/:codiceClasse/:codiceFamiglia',
    component: PdcFormComponent,
    data: { mode: 'update' }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PdcRoutingModule { }