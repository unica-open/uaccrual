/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-message',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      *ngIf="visible" 
      [ngClass]="getCssClass()" 
      class="p-4 mb-4 rounded-lg text-sm" 
      role="alert">
      <div class="flex items-start">
        <span class="mr-2" *ngIf="showIcon">{{ getIcon() }}</span>
        <div>
          <h3 class="font-medium" *ngIf="title">{{ title }}</h3>
          <p>{{ message }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .alert-error { @apply bg-red-50 text-red-800 border border-red-200; }
    .alert-warning { @apply bg-yellow-50 text-yellow-800 border border-yellow-200; }
    .alert-info { @apply bg-blue-50 text-blue-800 border border-blue-200; }
    .alert-success { @apply bg-green-50 text-green-800 border border-green-200; }
  `]
})
export class MessageComponent implements OnInit {
  @Input() type: 'error' | 'warning' | 'info' | 'success' = 'info';
  @Input() message: string = '';
  @Input() title: string = '';
  @Input() showIcon: boolean = true;
  @Input() visible: boolean = true;

  constructor() { }

  ngOnInit() {
  }

  getCssClass(): string {
    switch (this.type) {
      case 'error': return 'alert-error';
      case 'warning': return 'alert-warning';
      case 'info': return 'alert-info';
      case 'success': return 'alert-success';
      default: return 'alert-info';
    }
  }

  getIcon(): string {
    switch (this.type) {
      case 'error': return '❌';
      case 'warning': return '⚠️';
      case 'info': return 'ℹ️';
      case 'success': return '✅';
      default: return 'ℹ️';
    }
  }
}