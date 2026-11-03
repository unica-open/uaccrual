/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';

@Component({
  selector: 'app-pastel-snackbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pastel-snackbar.component.html',
  styleUrls: ['./pastel-snackbar.component.scss']
})
export class PastelSnackbarComponent {
  constructor(
    @Inject(MAT_SNACK_BAR_DATA) public data: { message: string; title?: string; type: string },
    private snackRef: MatSnackBarRef<PastelSnackbarComponent>
  ) {}

  close(): void {
    this.snackRef.dismiss();
  }

  iconMap: any = {
    success: 'check_circle',
    info: 'info',
    warning: 'warning_amber',
    error: 'error'
  };

  titleMap: any = {
    success: 'Operazione completata',
    info: 'Informazione',
    warning: 'Attenzione',
    error: 'Errore'
  };

  get icon(): string {
    return this.iconMap[this.data.type] || 'info';
  }

  get title(): string {
    return this.titleMap[this.data.type] || '';
  }
}
