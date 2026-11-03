/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface InfoDialogData {
  title: string;
  message: string;
}

@Component({
  selector: 'app-info-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="bg-surface-light dark:bg-surface-dark border border-border-light dark:border-border-dark rounded shadow-soft w-full max-w-md">
      <div class="px-5 py-4 border-b border-border-light dark:border-border-dark flex items-center justify-between">
        <h2 class="text-lg font-medium text-primary m-0">{{ data.title }}</h2>
        <button mat-icon-button mat-dialog-close class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
          <mat-icon class="text-base">close</mat-icon>
        </button>
      </div>
      <div class="p-6">
        <p class="text-text-main-light dark:text-text-main-dark">{{ data.message }}</p>
      </div>
      <div class="px-4 py-3 border-t border-border-light dark:border-border-dark flex justify-end">
        <button
          mat-button
          mat-dialog-close
          class="px-6 py-2 bg-primary text-white text-sm font-medium uppercase border border-transparent rounded hover:bg-primary-hover transition-colors shadow-sm focus:ring-2 focus:ring-offset-2 focus:ring-primary">
          OK
        </button>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
  `]
})
export class InfoDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: InfoDialogData) {}
}