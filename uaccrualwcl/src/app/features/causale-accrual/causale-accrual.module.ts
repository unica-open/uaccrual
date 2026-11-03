/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CausaleAccrualRoutingModule } from './causale-accrual-routing.module';
// I componenti standalone gestiscono le proprie importazioni
// Non è necessario importarli o dichiararli qui se sono usati solo tramite il routing
// import { CausaleAccrualSearchComponent } from './components/causale-accrual-search/causale-accrual-search.component';
// import { CausaleAccrualListComponent } from './components/causale-accrual-list/causale-accrual-list.component';
// import { CausaleAccrualDetailComponent } from './components/causale-accrual-detail/causale-accrual-detail.component';
import { ReactiveFormsModule } from '@angular/forms';
// Moduli Angular Material rimossi in quanto i componenti sono standalone

@NgModule({
  declarations: [
    // I componenti standalone non vengono dichiarati in un NgModule tradizionale
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    CausaleAccrualRoutingModule,
    // MatTableModule, // Rimossi
    // MatPaginatorModule, // Rimossi
    // MatFormFieldModule, // Rimossi
    // MatSelectModule, // Rimossi
    // MatInputModule, // Rimossi
    // MatButtonModule, // Rimossi
    // CausaleAccrualSearchComponent, // Rimossi
    // CausaleAccrualListComponent, // Rimossi
    // CausaleAccrualDetailComponent // Rimossi
  ]
})
export class CausaleAccrualModule { }
