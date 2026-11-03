/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthenticationService } from '../../../core/services/authentication.service';
import { JwtHelper } from '../../../core/utils/jwt.helper';
import { AzioniService } from '../../../core/services/azioni.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';
  isDev = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthenticationService,
    private jwtHelper: JwtHelper,
    private azioniService: AzioniService
  ) {}

  ngOnInit(): void {
    // Rileva se ambiente di sviluppo
    this.isDev = !window.location.hostname.match(/^prod/);

    this.loginForm = this.formBuilder.group({
      codiceFiscale: ['', Validators.required],
      password: ['', Validators.required],
      enteCodiceUnivoco: ['']
    });

    // Controlla i parametri di stato dalla query string
    this.route.queryParams.subscribe(params => {
      if (params['expired'] === 'true') {
        this.errorMessage = 'Sessione scaduta. Effettua nuovamente il login.';
      } else if (params['reset'] === 'success') {
        this.errorMessage = 'Password aggiornata. Effettua il login con la nuova password.';
      } else if (params['changed'] === 'success') {
        this.errorMessage = 'Password aggiornata. Effettua il login con la nuova password.';
      }
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    // Stop se il form non è valido
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;

    // Prendi ente dal form se presente, altrimenti da JWT o query param
    let enteCodiceUnivoco = this.f['enteCodiceUnivoco']?.value;

    // Se non fornito, prova a prendere da JWT esistente
    if (!enteCodiceUnivoco || enteCodiceUnivoco.trim() === '') {
      const token = this.authService.getToken();
      if (token) {
        enteCodiceUnivoco = this.jwtHelper.getCodiceEnte(token);
      }
    }

    // Se ancora non disponibile, errore
    if (!enteCodiceUnivoco || enteCodiceUnivoco.trim() === '') {
      this.errorMessage = 'Ente non specificato. Inserisci il codice ente o assicurati di avere una sessione attiva.';
      this.loading = false;
      return;
    }

    this.authService.login({
      codiceFiscale: this.f['codiceFiscale'].value,
      password: this.f['password'].value,
      provider: 'SpringSecurity',
      enteCodiceUnivoco: enteCodiceUnivoco.trim(),
      annoBilancio: 2025,
      faseBilancio: 'APERTO'
    }).subscribe({
      next: (response) => {
		//console.log('LOGIN RESPONSE:', response); 

        this.loading = false;

        // Controlla se il login è andato a buon fine (presenza del token)
        if (response && response.token) {
		
		  this.authService.saveToken(response.token);

          // Carica i permessi dal backend prima di navigare
          const primoAccesso = response.data?.primoAccesso;
          this.azioniService.loadPermissions().subscribe({
            next: () => {
              if (primoAccesso) {
                this.router.navigate(['/change-password'], {
                  queryParams: {
                    primoAccesso: 'true',
                    utenteCodice: response.data?.utenteCodice || this.f['codiceFiscale'].value
                  }
                });
              } else {
                this.router.navigate(['/home']);
              }
            },
            error: () => {
              // Naviga comunque in caso di errore nel caricamento permessi
              if (primoAccesso) {
                this.router.navigate(['/change-password'], {
                  queryParams: {
                    primoAccesso: 'true',
                    utenteCodice: response.data?.utenteCodice || this.f['codiceFiscale'].value
                  }
                });
              } else {
                this.router.navigate(['/home']);
              }
            }
          });
        } else {
          // Nessun token ricevuto, gestisci come errore
          this.errorMessage = 'Login fallito. Riprova.';
        }
      },
      error: (error) => {
        this.loading = false;

        // Gestisce sia il formato ApiResponse che le eccezioni Java serializzate
        if (error.error?.exceptionMessage) {
          // Formato eccezione Java serializzata
          this.errorMessage = error.error.exceptionMessage;
        } else if (error.error?.message) {
          this.errorMessage = error.error.message;
        } else if (error.status === 0) {
          this.errorMessage = 'Impossibile raggiungere il server. Verifica la connessione.';
        } else if (error.status === 401) {
          this.errorMessage = 'Credenziali non valide. Riprova.';
        } else {
          this.errorMessage = 'Si è verificato un errore durante il login. Riprova più tardi.';
        }
      }
    });
  }
}
