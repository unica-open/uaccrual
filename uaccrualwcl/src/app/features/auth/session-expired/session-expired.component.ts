/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-session-expired',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule
  ],
  templateUrl: './session-expired.component.html',
  styleUrls: ['./session-expired.component.scss']
})
export class SessionExpiredComponent implements OnInit {
  title = 'Sessione Scaduta';
  message = 'La tua sessione è scaduta.';
  infoMessage = 'Per continuare a utilizzare l\'applicazione, devi effettuare nuovamente il login attraverso la piattaforma di autenticazione del tuo ente.';
  icon = 'session_timeout';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    // Leggi i parametri dall'URL per personalizzare il messaggio
    const errorType = this.route.snapshot.queryParams['error'];
    
    if (errorType === 'user_not_found') {
      this.title = 'Utente Non Trovato';
      this.message = 'L\'utente non risulta registrato nel sistema.';
      this.infoMessage = 'Contatta l\'amministratore di sistema per richiedere l\'abilitazione all\'applicazione.';
      this.icon = 'person_off';
    } else if (errorType === 'auth_failed') {
      this.title = 'Autenticazione Fallita';
      this.message = 'Si è verificato un errore durante l\'autenticazione.';
      this.infoMessage = 'Verifica di aver seguito il link corretto o contatta l\'amministratore di sistema.';
      this.icon = 'error';
    }
  }
}
