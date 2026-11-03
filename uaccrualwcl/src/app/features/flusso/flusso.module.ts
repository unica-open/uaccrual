/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common'; // Needed for *ngFor, *ngIf etc.
import { ReactiveFormsModule, FormsModule } from '@angular/forms'; // Added FormsModule
import { FlussoRoutingModule } from './flusso-routing.module';

@NgModule({
  imports: [
    CommonModule, // Add CommonModule
    ReactiveFormsModule, // Add ReactiveFormsModule
    FormsModule, // Keep FormsModule for other potential components
    FlussoRoutingModule
  ],
  declarations: []
})
export class FlussoModule {}