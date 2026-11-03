/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { PrimaNotaConfermaInfo } from '../../model/flusso.model';

export interface ElaboraAnnullamentoConfermaDialogData {
  codiceFlusso: string;
  confirmationMessage: string;
  primaNote: PrimaNotaConfermaInfo[];
  resumeToken: string;
}

@Component({
  selector: 'app-elabora-annullamento-conferma-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatDialogModule, MatIconModule],
  template: `
    <div class="p-6" style="min-width: 520px; max-width: 680px;">

      <!-- Header -->
      <div class="flex items-center gap-3 mb-4">
        <mat-icon style="color: #f59e0b; font-size: 28px; width: 28px; height: 28px;">warning</mat-icon>
        <h2 mat-dialog-title class="text-base font-semibold text-gray-800 m-0 leading-tight">
          Conferma annullamento prime note
          <div class="text-sm font-normal text-gray-500 mt-0.5">Flusso: {{ data.codiceFlusso }}</div>
        </h2>
      </div>

      <mat-dialog-content>
        <!-- Message -->
        <p class="text-sm text-gray-700 mb-4">{{ data.confirmationMessage }}</p>

        <!-- Lista prime note da annullare (se presenti) -->
        <ng-container *ngIf="data.primaNote && data.primaNote.length > 0">
          <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">
            Prime note che verranno annullate ({{ data.primaNote.length }})
          </p>
          <div style="max-height: 260px; overflow-y: auto; border: 1px solid #e5e7eb; border-radius: 6px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
              <thead>
                <tr style="background: #f9fafb; position: sticky; top: 0;">
                  <th style="padding: 8px 12px; text-align: left; font-weight: 600; color: #6b7280; border-bottom: 1px solid #e5e7eb;">Anno</th>
                  <th style="padding: 8px 12px; text-align: left; font-weight: 600; color: #6b7280; border-bottom: 1px solid #e5e7eb;">N. Prov.</th>
                  <th style="padding: 8px 12px; text-align: left; font-weight: 600; color: #6b7280; border-bottom: 1px solid #e5e7eb;">Descrizione</th>
                  <th style="padding: 8px 12px; text-align: left; font-weight: 600; color: #6b7280; border-bottom: 1px solid #e5e7eb;">Causale</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let pn of data.primaNote; let odd = odd"
                    [style.background]="odd ? '#f9fafb' : '#ffffff'">
                  <td style="padding: 7px 12px; color: #374151; border-bottom: 1px solid #f3f4f6;">{{ pn.anno }}</td>
                  <td style="padding: 7px 12px; color: #374151; border-bottom: 1px solid #f3f4f6;">{{ pn.numeroProvvisorio }}</td>
                  <td style="padding: 7px 12px; color: #374151; border-bottom: 1px solid #f3f4f6; max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" [title]="pn.descrizione">{{ pn.descrizione }}</td>
                  <td style="padding: 7px 12px; color: #374151; border-bottom: 1px solid #f3f4f6;">{{ pn.causale }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </ng-container>

        <!-- Warning footer -->
        <div style="margin-top: 16px; padding: 10px 12px; background: #fef3c7; border: 1px solid #fde68a; border-radius: 6px; display: flex; align-items: flex-start; gap: 8px;">
          <mat-icon style="color: #d97706; font-size: 16px; width: 16px; height: 16px; margin-top: 1px; flex-shrink: 0;">info</mat-icon>
          <span style="font-size: 12px; color: #92400e; line-height: 1.5;">
            Confermando, le prime note elencate verranno annullate e ricreate con i nuovi dati del flusso. L'operazione non è reversibile.
          </span>
        </div>
      </mat-dialog-content>

      <mat-dialog-actions align="end" class="mt-5 gap-2">
        <button mat-stroked-button (click)="onRifiuta()">Non procedere</button>
        <button mat-flat-button
                style="background-color: #f59e0b; color: white;"
                (click)="onConferma()"
                cdkFocusInitial>
          <mat-icon style="font-size: 18px; width: 18px; height: 18px; margin-right: 4px;">check</mat-icon>
          Annulla e ricrea
        </button>
      </mat-dialog-actions>
    </div>
  `
})
export class ElaboraAnnullamentoConfermaDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ElaboraAnnullamentoConfermaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ElaboraAnnullamentoConfermaDialogData
  ) {}

  onRifiuta(): void {
    this.dialogRef.close(false);
  }

  onConferma(): void {
    this.dialogRef.close(true);
  }
}
