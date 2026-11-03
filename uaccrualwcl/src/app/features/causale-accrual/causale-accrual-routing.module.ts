/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CausaleAccrualSearchComponent } from './components/causale-accrual-search/causale-accrual-search.component';
import { CausaleAccrualFormComponent } from './components/causale-accrual-form/causale-accrual-form.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'search',
    pathMatch: 'full'
  },
  {
    path: 'search',
    component: CausaleAccrualSearchComponent
  },
  {
    path: 'results',
    redirectTo: 'search',
    pathMatch: 'full'
  },
  {
    path: 'dettaglio/:id',
    component: CausaleAccrualFormComponent,
    data: { mode: 'view' }
  },
  {
    path: 'inserisci',
    component: CausaleAccrualFormComponent,
    data: { mode: 'insert' }
  },
  {
    path: 'modifica/:id',
    component: CausaleAccrualFormComponent,
    data: { mode: 'edit' }
  },
  {
    path: ':id',
    component: CausaleAccrualFormComponent,
    data: { mode: 'view' }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CausaleAccrualRoutingModule { }
