/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageDisplayComponent } from './components/message-display/message-display.component';

@NgModule({
  declarations: [
    MessageDisplayComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    MessageDisplayComponent
  ]
})
export class SharedModule { }