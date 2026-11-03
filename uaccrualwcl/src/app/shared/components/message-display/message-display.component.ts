/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-message-display',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-display.component.html',
  styleUrls: ['./message-display.component.css']
})
export class MessageDisplayComponent {
  @Input() showMessage = false;
  @Input() messageType: 'error' | 'warning' | 'info' | 'success' = 'info';
  @Input() messageTitle = '';
  @Input() messageText = '';
  @Input() showConfirmButtons = false;
  @Input() showCloseButton = true;

  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onCloseClick(): void {
    this.close.emit();
  }

  onConfirmClick(): void {
    this.confirm.emit();
  }

  onCancelClick(): void {
    this.cancel.emit();
  }
}