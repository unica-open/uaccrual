/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, transition, style, animate } from '@angular/animations';
import { NotificationService, NotificationType } from '../../../core/services/notification.service';

@Component({
  selector: 'app-page-notification-banner',
  standalone: true,
  imports: [CommonModule],
  animations: [
    trigger('slideDown', [
      transition(':enter', [
        style({ height: '0px', opacity: 0, marginBottom: '0px', overflow: 'hidden' }),
        animate('250ms ease-out', style({ height: '*', opacity: 1, marginBottom: '16px' }))
      ]),
      transition(':leave', [
        style({ overflow: 'hidden' }),
        animate('200ms ease-in', style({ height: '0px', opacity: 0, marginBottom: '0px' }))
      ])
    ])
  ],
  template: `
    <div *ngIf="notification$ | async as notif"
         [@slideDown]
         class="page-banner" [ngClass]="'banner-' + notif.type">
      <span class="material-icons-round banner-icon">{{ iconMap[notif.type] }}</span>
      <div class="banner-content">
        <strong>{{ notif.title }}</strong>
        <span *ngIf="notif.message">&nbsp;{{ notif.message }}</span>
      </div>
      <button class="banner-close" (click)="dismiss()" title="Chiudi">
        <span class="material-icons-round">close</span>
      </button>
    </div>
  `,
  styles: [`
    :host { display: block; }
    .page-banner {
      display: flex; align-items: center; gap: 10px;
      padding: 12px 16px; border-radius: 10px; border: 1px solid;
      font-size: 14px; margin-bottom: 16px;
    }
    .banner-success { background: var(--success-soft); color: var(--success); border-color: var(--success); }
    .banner-info    { background: var(--info-soft);    color: #0a7e9c;        border-color: var(--info); }
    .banner-warning { background: var(--warning-soft); color: #b8860b;        border-color: var(--warning); }
    .banner-error   { background: var(--danger-soft);  color: var(--danger);  border-color: var(--danger); }
    .banner-icon    { font-size: 20px; flex-shrink: 0; }
    .banner-content { flex: 1; }
    .banner-close {
      flex-shrink: 0; background: none; border: none; cursor: pointer;
      padding: 2px 4px; border-radius: 4px; color: inherit; opacity: 0.65;
      display: flex; align-items: center;
    }
    .banner-close:hover { opacity: 1; background: rgba(0,0,0,0.06); }
    .banner-close .material-icons-round { font-size: 18px; }
  `]
})
export class PageNotificationBannerComponent {
  private notificationService = inject(NotificationService);

  notification$ = this.notificationService.notification$;

  readonly iconMap: Record<NotificationType, string> = {
    success: 'check_circle',
    info: 'info',
    warning: 'warning_amber',
    error: 'error'
  };

  dismiss(): void {
    this.notificationService.dismiss();
  }
}
