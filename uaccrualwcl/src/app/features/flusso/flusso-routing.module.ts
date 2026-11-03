/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'search', // Redirect to search by default
    pathMatch: 'full'
  },
  {
    path: 'search',
    loadComponent: () => // Use loadComponent for lazy loading
      import('./components/flusso-search/flusso-search.component')
        .then(m => m.FlussoSearchComponent)
  },
  {
    path: 'results',
    redirectTo: 'search',
    pathMatch: 'full'
  },
  {
    path: 'upload',
    loadComponent: () =>
      import('./components/flusso-upload/flusso-upload.component')
        .then(m => m.FlussoUploadComponent)
  },
  {
    path: 'detail/:id', // New route for detail view
    loadComponent: () =>
      import('./components/flusso-detail/flusso-detail.component')
        .then(m => m.FlussoDetailComponent)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FlussoRoutingModule {}
