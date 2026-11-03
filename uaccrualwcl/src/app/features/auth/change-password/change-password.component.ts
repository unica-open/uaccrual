/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthenticationService } from '../../../core/services/authentication.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {
  changeForm!: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';
  successMessage = '';
  isPrimoAccesso = false;
  utenteCodice: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthenticationService
  ) {}

  ngOnInit(): void {
    // Controlla se è primo accesso dai query parameters
    this.route.queryParams.subscribe(params => {
      if (params['primoAccesso'] === 'true' || params['utenteCodice']) {
        this.isPrimoAccesso = true;
        this.utenteCodice = params['utenteCodice'];
      }
    });

    this.changeForm = this.formBuilder.group({
      vecchiaPassword: this.isPrimoAccesso ? [''] : ['', Validators.required],
      nuovaPassword: ['', [Validators.required, Validators.minLength(8)]],
      confermaPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const nuovaPassword = control.get('nuovaPassword');
    const confermaPassword = control.get('confermaPassword');
    
    if (nuovaPassword && confermaPassword && nuovaPassword.value !== confermaPassword.value) {
      return { passwordMismatch: true };
    }
    
    return null;
  }

  get f() {
    return this.changeForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Stop se il form non è valido
    if (this.changeForm.invalid) {
      return;
    }

    this.loading = true;

    // TODO: Implementare endpoint change-password su accrualauthsbapi
    // Per ora uso una chiamata fittizia
    this.authService.changePassword({
      codiceFiscale: this.utenteCodice || '',
      vecchiaPassword: this.isPrimoAccesso ? undefined : this.f['vecchiaPassword'].value,
      nuovaPassword: this.f['nuovaPassword'].value,
      confermaPassword: this.f['confermaPassword'].value
    }).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = 'Password aggiornata con successo!';

        setTimeout(() => {
          if (this.isPrimoAccesso) {
            // Dopo il primo accesso, redirect alla home
            this.router.navigate(['/home']);
          } else {
            // Dopo il cambio password, redirect al login con flag di successo (URL pulito)
            this.router.navigate(['/login'], {
              queryParams: { changed: 'success' }
            });
          }
        }, 1500);
      },
      error: (error) => {
        this.loading = false;

        if (error.error?.exceptionMessage) {
          this.errorMessage = error.error.exceptionMessage;
        } else if (error.error?.message) {
          this.errorMessage = error.error.message;
        } else if (error.status === 0) {
          this.errorMessage = 'Impossibile raggiungere il server. Verifica la connessione.';
        } else if (error.status === 401) {
          this.errorMessage = 'Password attuale non corretta.';
        } else if (error.status === 400) {
          this.errorMessage = 'Dati non validi. Verifica di aver inserito correttamente tutti i campi.';
        } else {
          this.errorMessage = 'Si è verificato un errore. Riprova più tardi.';
        }
      }
    });
  }
}
