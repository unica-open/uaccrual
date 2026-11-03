/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NgxsModule } from '@ngxs/store';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { PrimanotaRoutingModule } from './primanota-routing.module';
import { PrimanotaSearchComponent } from './components/pn-search/pn-search.component';
import { PnDetailViewComponent } from './components/pn-detail-view/pn-detail-view.component';
import { PnSearchListComponent } from './components/pn-search-list/pn-search-list.component';
import { PrimanotaSearchState } from './state/primanota-search.state';

/**
 * Modulo per la gestione delle Prime Note
 * 
 * Nota: I nuovi componenti (PnTabTestataComponent, PnTabContiCollegatiComponent, 
 * PnTabAttributiComponent, PnContainerComponent) sono standalone e non richiedono
 * importazioni nel modulo.
 * 
 * PrimeNG è stato rimosso a favore di Angular Material + HTML/Tailwind
 */
@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PrimanotaRoutingModule,
    RouterModule,
    NgxsModule.forFeature([PrimanotaSearchState]),
    MatSnackBarModule, // Per le notifiche toast
    // Componenti standalone
    PrimanotaSearchComponent,
    PnDetailViewComponent,
    PnSearchListComponent
  ],
  providers: []
})
export class PrimanotaModule { }
