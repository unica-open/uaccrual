/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  templateUrl: './unauthorized.component.html',
  styleUrls: ['./unauthorized.component.scss'],
  standalone: false
})
export class UnauthorizedComponent {
  constructor(private router: Router) {}

  goHome(): void {
    // Reindirizzamento alla home page dell'applicazione
    // In futuro, questo potrebbe essere configurabile in base all'ambiente
    window.location.href = '/';
  }
}