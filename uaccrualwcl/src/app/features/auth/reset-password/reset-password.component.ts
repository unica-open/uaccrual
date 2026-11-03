/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthenticationService } from '../../../core/services/authentication.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatIconModule
  ],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  resetForm!: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';
  requestSent = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthenticationService
  ) {}

  ngOnInit(): void {
    this.resetForm = this.formBuilder.group({
      codiceFiscale: ['', Validators.required]
    });
  }

  get f() {
    return this.resetForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    // Stop se il form non è valido
    if (this.resetForm.invalid) {
      return;
    }

    this.loading = true;

    this.authService.requestPasswordReset(this.f['codiceFiscale'].value)
      .subscribe({
        next: (response) => {
          this.loading = false;
          this.requestSent = true;
        },
        error: (error) => {
          this.loading = false;

          // Il backend ritorna una stringa JSON, va parsata
          let backendMessage = '';

          try {
            // Prova a parsare error.error come JSON
            const errorObj = typeof error?.error === 'string' ? JSON.parse(error?.error) : error?.error;
            backendMessage = errorObj?.message || errorObj?.exceptionMessage || '';
          } catch (e) {
            backendMessage = error?.message;
          }

          if (backendMessage) {
            this.errorMessage = backendMessage;
          } else if (error?.status === 404) {
            this.errorMessage = 'Codice fiscale non trovato.';
          } else if (error?.status === 401) {
            this.errorMessage = 'Utente non abilitato a questa modalità di autenticazione.';
          } else if (error?.status === 400) {
            this.errorMessage = 'Errore durante l\'invio dell\'email. Riprova più tardi.';
          } else if (error?.status === 0) {
            this.errorMessage = 'Impossibile raggiungere il server. Verifica la connessione.';
          } else {
            this.errorMessage = 'Si è verificato un errore. Riprova più tardi.';
          }
        }
      });
  }
}
