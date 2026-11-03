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
  selector: 'app-confirm-password-reset',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './confirm-password-reset.component.html',
  styleUrls: ['./confirm-password-reset.component.scss']
})
export class ConfirmPasswordResetComponent implements OnInit {
  confirmForm!: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthenticationService
  ) {}

  ngOnInit(): void {
    this.confirmForm = this.formBuilder.group({
      token: ['', Validators.required],
      codiceFiscale: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, {
      validators: this.passwordMatchValidator
    });

    // Legge il token dai query parameters (nascosto all'utente)
    this.route.queryParams.subscribe(params => {
      if (params['token']) {
        this.confirmForm.patchValue({ token: params['token'] });
      }
    });
  }

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    
    return null;
  }

  get f() {
    return this.confirmForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    // Stop se il form non è valido
    if (this.confirmForm.invalid) {
      return;
    }

    this.loading = true;

    this.authService.confirmPasswordReset({
      token: this.f['token'].value,
      codiceFiscale: this.f['codiceFiscale'].value,
      password: this.f['password'].value,
      confirmPassword: this.f['confirmPassword'].value
    }).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('Password reset confermata:', response);

        // Redirect al login con flag di successo (URL pulito)
        this.router.navigate(['/login'], {
          queryParams: { reset: 'success' }
        });
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
          this.errorMessage = 'Token non valido o scaduto. Richiedi un nuovo reset password.';
        } else if (error.status === 400) {
          this.errorMessage = 'Dati non validi. Verifica di aver inserito correttamente tutti i campi.';
        } else {
          this.errorMessage = 'Si è verificato un errore. Riprova più tardi.';
        }
      }
    });
  }
}
