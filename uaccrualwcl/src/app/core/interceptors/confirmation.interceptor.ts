/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, filter, take, switchMap } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/components/confirm-dialog/confirm-dialog.component';

/**
 * Interceptor per gestire le richieste di conferma (409 Conflict con confirmationRequired)
 * Intercetta le risposte 409 e mostra un dialog di conferma prima di riprovare la chiamata
 */
@Injectable()
export class ConfirmationInterceptor implements HttpInterceptor {
  
  private isConfirming = new BehaviorSubject<boolean>(false);
  
  constructor(private dialog: MatDialog) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Verifica se è un 409 con richiesta di conferma
        if (error.status === 409 && error.error?.confirmationRequired) {
          const confirmationCode = error.error.confirmationCode;
          const confirmationMessage = error.error.confirmationMessage;
          
          // Determina se è una richiesta di rinumerazione (3 pulsanti) o conferma generica (2 pulsanti)
          const isRinumerazione = confirmationCode === 'WEB_CON_033' || confirmationCode === 'WEB_CON_034';
          
          // Mostra il dialog di conferma
          const dialogRef = this.dialog.open(ConfirmDialogComponent, {
            data: {
              message: `${confirmationCode} - ${confirmationMessage}`,
              title: isRinumerazione
                ? 'Mantieni numero esistente'
                : 'Conferma Validazione',
              showThreeButtons: isRinumerazione,
              siLabel: 'Si',
              noLabel: 'No'
            } as ConfirmDialogData,
            disableClose: false
          });

          // Ritorna un Observable che emetterà la risposta dopo la conferma dell'utente
          return dialogRef.afterClosed().pipe(
            switchMap((result: boolean | 'no' | undefined) => {
              // Utente ha annullato: undefined (X o fuori dal dialog) o false (pulsante Annulla a 2 pulsanti)
              if (result === undefined || result === false) {
                // Utente ha chiuso il dialog senza confermare
                return throwError(() => ({
                  status: 409,
                  confirmationCancelled: true
                }));
              }

              // Utente ha confermato - riprova la chiamata con il parametro appropriato
              let newRequest = request;

              if (isRinumerazione) {
                // Per WEB_CON_033/034: true=Si (mantiene), 'no'=No (riassegna)
                const mantieniNumeri = result === true;
                const existingParams = request.params;

                // Determina quale parametro usare in base all'URL
                if (request.url.includes('/valida-massiva')) {
                  newRequest = request.clone({
                    params: existingParams.set('mantieniNumeri', mantieniNumeri.toString())
                  });
                } else {
                  newRequest = request.clone({
                    params: existingParams.set('mantieniNumeroDefinitivo', mantieniNumeri.toString())
                  });
                }
              } else {
                // Conferma generica (WEB_CON_0016 / WEB_CON_0017) - l'utente ha confermato
                // Aggiungo il parametro 'confermato=true' per informare il backend
                newRequest = request.clone({
                  params: request.params.set('confermato', 'true')
                });
              }

              // Riprova la chiamata
              return next.handle(newRequest);
            })
          );
        }
        
        // Per altri errori, propaga normalmente
        return throwError(() => error);
      })
    );
  }
}
