/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { FlussoService } from '../../../../core/services/flusso.service';
import { ProcessStatusResponse } from '../../model/flusso.model';

export interface ElaboraProgressDialogData {
  flussoId: string;
  codiceFlusso: string;
}

@Component({
  selector: 'app-elabora-progress-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatDialogModule, MatIconModule],
  template: `
    <div class="p-6" style="min-width: 420px;">

      <!-- Header -->
      <div class="flex items-center gap-3 mb-5">
        <span class="material-icons-round text-3xl"
              [style.color]="status === 'COMPLETED' ? '#22c55e' : status === 'FAILED' ? '#ef4444' : '#3b82f6'"
              [style.animation]="isRunning ? 'spin 1.2s linear infinite' : 'none'">
          {{ statusIcon }}
        </span>
        <div mat-dialog-title class="text-base font-semibold text-gray-800 m-0 leading-tight">
          Elaborazione Flusso
          <div class="text-sm font-normal text-gray-500 mt-0.5">{{ data.codiceFlusso }}</div>
        </div>
      </div>

      <mat-dialog-content>

        <!-- Step text -->
        <p class="text-sm text-gray-500 mb-3">{{ currentStep }}</p>

        <!-- Green progress bar (matching header style) -->
        <div style="width:100%; height:6px; background:#e5e7eb; border-radius:4px; overflow:hidden;">
          <div [style.width.%]="displayProgress"
               [style.background-color]="status === 'FAILED' ? '#ef4444' : '#22c55e'"
               style="height:100%; border-radius:4px; transition: width 0.28s ease, background-color 0.3s;">
          </div>
        </div>

        <!-- % label -->
        <div class="flex justify-between mt-1.5">
          <span class="text-xs text-gray-400">{{ statusLabel }}</span>
          <span class="text-xs font-medium" [style.color]="status === 'FAILED' ? '#ef4444' : '#22c55e'">
            {{ status === 'FAILED' ? 'Errore' : (displayProgress | number:'1.0-0') + '%' }}
          </span>
        </div>

        <!-- Error message -->
        <p *ngIf="errorMessage"
           class="mt-3 text-sm text-red-600 bg-red-50 rounded p-2 border border-red-100">
          {{ errorMessage }}
        </p>

      </mat-dialog-content>

      <mat-dialog-actions align="end" class="mt-4">
        <button mat-stroked-button (click)="onClose()" [disabled]="isRunning">
          {{ isRunning ? 'In corso...' : 'Chiudi' }}
        </button>
      </mat-dialog-actions>

    </div>
  `
})
export class ElaboraProgressDialogComponent implements OnInit, OnDestroy {

  status: ProcessStatusResponse['status'] = 'PENDING';
  displayProgress = 0;
  currentStep = 'Avvio elaborazione...';
  errorMessage: string | null = null;

  private statusSub: Subscription | null = null;
  private progressSub: Subscription | null = null;

  constructor(
    public dialogRef: MatDialogRef<ElaboraProgressDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ElaboraProgressDialogData,
    private flussoService: FlussoService
  ) {
    dialogRef.disableClose = true;
  }

  ngOnInit(): void {
    // Track status text and close trigger
    this.statusSub = this.flussoService.activeJobStatus$.pipe(
      filter(s => s !== null && s!.jobId === this.data.flussoId)
    ).subscribe((resp) => {
      if (!resp) return;
      this.status = resp.status;
      this.currentStep = resp.currentStep || '';

      if (resp.status === 'FAILED') {
        this.errorMessage = resp.message || 'Si è verificato un errore durante l\'elaborazione.';
        this.dialogRef.disableClose = false;
      } else if (resp.status === 'COMPLETED') {
        this.dialogRef.disableClose = false;
        setTimeout(() => this.dialogRef.close({ success: true }), 1200);
      }
    });

    // Track smooth progress for the bar
    this.progressSub = this.flussoService.elaborationProgress$.subscribe(p => {
      if (p !== null) this.displayProgress = Math.min(100, Math.max(0, p));
    });
  }

  ngOnDestroy(): void {
    this.statusSub?.unsubscribe();
    this.progressSub?.unsubscribe();
  }

  get isRunning(): boolean {
    return this.status === 'PENDING' || this.status === 'IN_PROGRESS';
  }

  get statusIcon(): string {
    switch (this.status) {
      case 'COMPLETED': return 'check_circle';
      case 'FAILED':    return 'error';
      default:          return 'autorenew';
    }
  }

  get statusLabel(): string {
    switch (this.status) {
      case 'PENDING':     return 'In attesa...';
      case 'IN_PROGRESS': return 'Elaborazione in corso...';
      case 'COMPLETED':   return 'Completato';
      case 'FAILED':      return 'Errore';
    }
  }

  onClose(): void {
    this.dialogRef.close({ success: this.status === 'COMPLETED' });
  }
}
