/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Store } from '@ngxs/store';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { SetMessage } from '../store/pdc/pdc.actions';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface PageNotification {
  type: NotificationType;
  message: string;
  title: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private notificationSubject = new BehaviorSubject<PageNotification | null>(null);
  notification$ = this.notificationSubject.asObservable();

  private dismissTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(private store: Store) {}

  success(message: string, title = 'Operazione completata'): void {
    this.show('success', message, title, 4000);
  }

  error(message: string, title = 'Errore'): void {
    this.show('error', message, title, 6000);
  }

  warning(message: string, title = 'Attenzione'): void {
    this.show('warning', message, title, 5000);
  }

  info(message: string, title = 'Informazione'): void {
    this.show('info', message, title, 4000);
  }

  dismiss(): void {
    if (this.dismissTimer) clearTimeout(this.dismissTimer);
    this.notificationSubject.next(null);
  }

  private show(type: NotificationType, message: string, title: string, duration: number): void {
    if (this.dismissTimer) clearTimeout(this.dismissTimer);
    this.notificationSubject.next({ type, message, title });
    this.dismissTimer = setTimeout(() => this.notificationSubject.next(null), duration);
  }

  showSuccess(message: string, title?: string): void { this.success(message, title); }
  showError(message: string, title?: string): void { this.error(message, title); }
  showWarning(message: string, title?: string): void { this.warning(message, title); }
  showInfo(message: string, title?: string): void { this.info(message, title); }

  showMessageByCode(code: string, details?: any): void {
    const messageMap: { [key: string]: { type: NotificationType, message: string, title: string } } = {
      'WEB_OK_0004': { type: 'success', message: 'Salvataggio effettuato correttamente', title: 'Operazione Completata' },
      'WEB_ERR_0001': { type: 'error', message: 'Si è verificato un errore imprevisto', title: 'Errore di Sistema' },
      'WEB_ERR_0002': { type: 'error', message: 'Dato obbligatorio non valorizzato', title: 'Validazione Fallita' },
      'WEB_ERR_0005': { type: 'error', message: 'Il dato non è univoco', title: 'Duplicato Rilevato' },
      'WEB_ERR_0007': { type: 'error', message: 'Periodo di validità non ammesso', title: 'Errore Temporale' },
      'WEB_ERR_0012': { type: 'error', message: 'Impossibile eliminare l\'oggetto selezionato', title: 'Azione Negata' },
      'WEB_ERR_0013': { type: 'error', message: 'Operazione non eseguita a causa della concorrenza', title: 'Conflitto di Versione' },
      'WEB_ERR_0027': { type: 'error', message: 'Fine validità non ammessa per la presenza di prime note', title: 'Vincolo Integrità' },
      'WEB_WRN_0006': { type: 'warning', message: 'Si vuole eliminare il dato selezionato?', title: 'Conferma Eliminazione' },
      'WEB_INF_0003': { type: 'info', message: 'Nessun dato trovato', title: 'Esito Ricerca' },
      'WEB_INF_0006': { type: 'info', message: 'Esportazione completata con successo', title: 'Export' },
      'WEB_ERR_018': { type: 'error', message: 'Almeno 2 conti con segni diversi', title: 'Validazione Scrittura' },
      'WEB_ERR_019': { type: 'error', message: 'Totale Dare e Avere devono coincidere', title: 'Bilanciamento Contabile' },
      'WEB_ERR_020': { type: 'error', message: 'Stesso conto con segni diversi', title: 'Validazione Conti' },
      'WEB_ERR_021': { type: 'error', message: 'Conto non esistente', title: 'Validazione Conto' },
      'WEB_ERR_022': { type: 'error', message: 'Conto non valido rispetto alla data', title: 'Validità Temporale' },
      'WEB_ERR_028': { type: 'error', message: 'Il conto indicato non è foglia', title: 'Tipo di Conto' }
    };

    const config = messageMap[code];
    if (config) {
      const msg = details && details !== config.message ? `${config.message}: ${details}` : config.message;
      this.show(config.type, msg, config.title, config.type === 'error' ? 6000 : config.type === 'warning' ? 5000 : 4000);
    } else {
      this.error(`Codice messaggio sconosciuto: ${code}`, 'Errore Configurazione');
    }
  }

  confirm(title: string, message: string): Observable<boolean> {
    const result = window.confirm(`${title}\n\n${message}`);
    return of(result);
  }
}
