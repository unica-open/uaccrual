/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface ElaboraConfirmDialogData {
  codiceFlusso: string;
}

@Component({
  selector: 'app-elabora-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatDialogModule, MatIconModule],
  template: `
    <div class="p-6">
      <div class="flex items-center gap-3 mb-4">
        <mat-icon class="text-blue-500 text-3xl">play_circle</mat-icon>
        <h2 mat-dialog-title class="text-lg font-semibold text-gray-800 m-0">Conferma Elaborazione</h2>
      </div>
      <mat-dialog-content>
        <p class="text-gray-600">
          Stai per avviare l'elaborazione del flusso
          <strong class="text-gray-800">{{ data.codiceFlusso }}</strong>.
        </p>
        <p class="text-gray-500 text-sm mt-2">
          Il processo potrebbe richiedere alcuni minuti. Potrai monitorare l'avanzamento in tempo reale.
        </p>
      </mat-dialog-content>
      <mat-dialog-actions align="end" class="mt-4 gap-2">
        <button mat-stroked-button (click)="onCancel()">Annulla</button>
        <button mat-flat-button color="primary" (click)="onConfirm()" cdkFocusInitial>
          <mat-icon>play_arrow</mat-icon>
          Avvia Elaborazione
        </button>
      </mat-dialog-actions>
    </div>
  `
})
export class ElaboraConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ElaboraConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ElaboraConfirmDialogData
  ) {}

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }
}
