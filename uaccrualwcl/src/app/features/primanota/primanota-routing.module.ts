/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PrimanotaSearchComponent } from './components/pn-search/pn-search.component';
import { PnDetailViewComponent } from './components/pn-detail-view/pn-detail-view.component';
import { PnContainerComponent } from './components/pn-container/pn-container.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'search',
    pathMatch: 'full'
  },
  {
    path: 'search',
    component: PrimanotaSearchComponent
  },
  {
    path: 'results',
    redirectTo: 'search',
    pathMatch: 'full'
  },
  {
    path: 'dettaglio/:anno/:numero',
    component: PnDetailViewComponent
  },
  {
    path: 'nuovo',
    component: PnContainerComponent
  },
  {
    path: 'modifica/:anno/:numero',
    component: PnContainerComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PrimanotaRoutingModule { }
