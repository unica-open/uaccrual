/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';

// Interfaccia per i dati passati al dialogo
export interface ConfirmDialogData {
  message: string; // Il messaggio da mostrare (es. WEB_WRN_0006)
  title?: string;  // Titolo opzionale
  showThreeButtons?: boolean; // Se true, mostra 3 pulsanti (Si/No/Annulla)
  siLabel?: string; // Etichetta pulsante Si (default: 'Si')
  noLabel?: string; // Etichetta pulsante No (default: 'No')
  annullaLabel?: string; // Etichetta pulsante Annulla (default: 'Annulla')
}

// Tipo per il risultato del dialog
export type ConfirmDialogResult = true | false | 'no';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title>{{ data.title || 'Conferma' }}</h2>
    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end" *ngIf="!data.showThreeButtons">
      <button mat-button [mat-dialog-close]="false">Annulla</button>
      <button mat-raised-button color="warn" [mat-dialog-close]="true" cdkFocusInitial>
        Conferma
      </button>
    </mat-dialog-actions>
    
    <mat-dialog-actions align="end" *ngIf="data.showThreeButtons">
      <button mat-button [mat-dialog-close]="undefined">Annulla</button>
      <button mat-button color="warn" [mat-dialog-close]="'no'">
        {{ data.noLabel || 'No' }}
      </button>
      <button mat-raised-button color="primary" [mat-dialog-close]="true" cdkFocusInitial>
        {{ data.siLabel || 'Si' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData) {}
}