import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export type NotificaTipo = 'INFO' | 'WARN' | 'ERROR' | 'CONFIRM';

export interface Notifica {
  id: number;
  tipo: NotificaTipo;
  codice?: string;
  titolo: string;
  messaggio: string;
  idFlusso?: string;
  resumeToken?: string;
  letta: boolean;
  data?: string;
}

/**
 * Centro avvisi dell'utente.
 *
 * Le notifiche vivono sul backend, non in memoria: restano quindi visibili dopo un logout,
 * un cambio ente o un semplice refresh della pagina.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificheService {

  private baseUrl = environment.services.accrualNewApiUrl;

  private notificheSubject = new BehaviorSubject<Notifica[]>([]);
  notifiche$ = this.notificheSubject.asObservable();

  constructor(private http: HttpClient) { }

  get notificheValue(): Notifica[] {
    return this.notificheSubject.getValue();
  }

  /**
   * Ricarica le notifiche dal backend. Da chiamare all'avvio e a ogni cambio di contesto
   * (login, cambio ente), perché è lì che lo stato in memoria andava perso.
   *
   * Un errore non deve rompere l'header: si degrada a elenco vuoto.
   */
  load(): Observable<Notifica[]> {
    return this.http.get<Notifica[]>(`${this.baseUrl}/notifiche`).pipe(
      tap(notifiche => this.notificheSubject.next(notifiche ?? [])),
      catchError(error => {
        console.error('Errore durante il recupero delle notifiche:', error);
        this.notificheSubject.next([]);
        return of([]);
      })
    );
  }

  /** Segna la notifica come letta, aggiornando subito l'elenco locale. */
  segnaLetta(id: number): Observable<void> {
    this.notificheSubject.next(
      this.notificheValue.map(n => (n.id === id ? { ...n, letta: true } : n))
    );
    return this.http.put<void>(`${this.baseUrl}/notifiche/${id}/letta`, {}).pipe(
      catchError(error => {
        console.error(`Errore nel segnare come letta la notifica ${id}:`, error);
        return of(void 0);
      })
    );
  }

  /** Chiude la notifica: sparisce dal pannello e non torna più. */
  chiudi(id: number): Observable<void> {
    this.notificheSubject.next(this.notificheValue.filter(n => n.id !== id));
    return this.http.delete<void>(`${this.baseUrl}/notifiche/${id}`).pipe(
      catchError(error => {
        console.error(`Errore durante la chiusura della notifica ${id}:`, error);
        return of(void 0);
      })
    );
  }

  /** Svuota l'elenco locale, senza toccare il backend: usato al logout. */
  reset(): void {
    this.notificheSubject.next([]);
  }
}
