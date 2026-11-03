/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';

import { ModellomifRoutingModule } from './modellomif-routing.module';
import { ModellomifUploadComponent } from './components/modellomif-upload/modellomif-upload.component';

@NgModule({
  declarations: [
    ModellomifUploadComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ModellomifRoutingModule,
    ButtonModule,
    CardModule,
    ProgressSpinnerModule,
    SelectModule
  ]
})
export class ModellomifModule { }
