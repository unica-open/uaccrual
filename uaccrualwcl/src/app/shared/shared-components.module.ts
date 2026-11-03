/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';
import { RouterModule } from '@angular/router';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';

@NgModule({
  declarations: [
    UnauthorizedComponent,
    ConfirmDialogComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
    UnauthorizedComponent,
    ConfirmDialogComponent
  ]
})
export class SharedComponentsModule { }