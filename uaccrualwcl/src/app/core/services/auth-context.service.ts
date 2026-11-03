/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';
import { Ente } from '../models/ente.model';
import { AuthStorageService } from './auth-storage.service';
import { JwtHelper } from '../utils/jwt.helper';

/**
 * Servizio per la gestione del contesto multi-ente.
 *
 * Fornisce informazioni sull'ente corrente, anno di bilancio e fase di bilancio.
 *
 * @see AuthenticationService per l'autenticazione
 */
@Injectable({
  providedIn: 'root'
})
export class AuthContextService {
  private currentEnte: Ente | null = null;
  private annoBilancio: number | null = null;
  private faseBilancio: string | null = null;

  constructor(
    private authStorage: AuthStorageService,
    private jwtHelper: JwtHelper
  ) {}

  /**
   * Imposta il contesto corrente (ente, anno bilancio, fase).
   *
   * @param ente Ente corrente
   * @param annoBilancio Anno di bilancio
   * @param faseBilancio Fase di bilancio
   */
  setContext(ente: Ente, annoBilancio?: number, faseBilancio?: string): void {
    this.currentEnte = ente;
    this.annoBilancio = annoBilancio || null;
    this.faseBilancio = faseBilancio || null;
  }

  /**
   * Ottiene l'ente corrente.
   *
   * @returns Ente corrente o null se non impostato
   */
  getCurrentEnte(): Ente | null {
    return this.currentEnte;
  }

  /**
   * Ottiene l'anno di bilancio corrente.
   *
   * @returns Anno di bilancio o null se non impostato
   */
  getAnnoBilancio(): number | null {
    return this.annoBilancio;
  }

  /**
   * Ottiene la fase di bilancio corrente.
   *
   * @returns Fase di bilancio o null se non impostato
   */
  getFaseBilancio(): string | null {
    return this.faseBilancio;
  }

  /**
   * Pulisce il contesto corrente.
   */
  clear(): void {
    this.currentEnte = null;
    this.annoBilancio = null;
    this.faseBilancio = null;
  }

  /**
   * Carica il contesto da sessionStorage.
   *
   * Estrae informazioni dal token JWT corrente e ripristina il contesto.
   *
   * @returns true se il contesto è stato ripristinato con successo
   */
  loadFromStorage(): boolean {
    const token = this.authStorage.getToken();

    if (!token) {
      console.log('[AuthContextService] Nessun token in sessionStorage');
      return false;
    }

    // Decodifica JWT per estrarre informazioni
    const decoded = this.jwtHelper.decodeToken(token);

    if (!decoded) {
      console.log('[AuthContextService] Token non valido');
      return false;
    }


	// Estrai ente dal token
    const enteCodice = this.jwtHelper.getCodiceEnte(token);
    const annoBilancio = this.jwtHelper.getAnnoBilancio(token);
    const faseBilancio = this.jwtHelper.getFaseBilancio(token);

    if (enteCodice) {
      const enteDenominazione = decoded.denominazione_ente;
      if (!enteDenominazione) {
        console.error('[AuthContextService] Ente denominazione non trovata nel token:', { enteCodice, decoded });
        return false;
      }

      // Crea ente base dalle informazioni JWT
      const ente: Ente = {
        codice: enteCodice,
        codiceFiscale: enteCodice,
        descrizione: enteDenominazione,
        denominazione: enteDenominazione
      };

      this.setContext(ente, annoBilancio ?? undefined, faseBilancio ?? undefined);
      console.log('[AuthContextService] Contesto ripristinato da JWT:', { ente, annoBilancio, faseBilancio });
      return true;
    }

    console.log('[AuthContextService] Ente non trovato nel token');
    return false;
  }

   /* aggiunto ale */	
   setToken(token: string): void {
	  console.log('[AuthContextService] setToken - Salvataggio token in sessionStorage');
	  console.log('[AuthContextService] setToken - Token valido:', !!token);
	  if (token) {
	    const decoded = this.jwtHelper.decodeToken(token);
	    console.log('[AuthContextService] setToken - Claims JWT:', JSON.stringify(decoded));
	  }
	  this.authStorage.setToken(token);
   }
    	
  /**
   * Imposta l'ente corrente.
   *
   * @param enteCodice Codice dell'ente da impostare
   */
  setCurrentEnte(enteCodice: string): void {
    if (this.currentEnte) {
      this.currentEnte.codice = enteCodice;
      this.currentEnte.codiceFiscale = enteCodice;
    } else {
      this.currentEnte = {
        codice: enteCodice,
        codiceFiscale: enteCodice,
        denominazione: enteCodice
      };
    }
  }

  /**
   * Carica il contesto da un token JWT.
   *
   * @param token Token JWT da decodificare
   * @returns true se il contesto è stato caricato con successo
   */
   loadFromToken(token: string): boolean {
     console.log('[AuthContextService] loadFromToken - Inizio caricamento da token');
     if (!token) {
       console.log('[AuthContextService] loadFromToken - ERRORE: Token non fornito');
       return false;
     }

     // Decodifica JWT per estrarre informazioni
     const decoded = this.jwtHelper.decodeToken(token);
     console.log('[AuthContextService] loadFromToken - JWT decodificato:', JSON.stringify(decoded));

     if (!decoded) {
       console.log('[AuthContextService] loadFromToken - ERRORE: Token non valido');
       return false;
     }

    // Estrai ente dal token
    const enteCodice = this.jwtHelper.getCodiceEnte(token);
    const annoBilancio = this.jwtHelper.getAnnoBilancio(token);
    const faseBilancio = this.jwtHelper.getFaseBilancio(token);

     if (enteCodice) {
       const enteDenominazione = decoded.denominazione_ente;
       console.log('[AuthContextService] loadFromToken - enteCodice:', enteCodice, 'denominazione:', enteDenominazione);
       
       if (!enteDenominazione) {
         console.error('[AuthContextService] loadFromToken - ERRORE: Ente denominazione non trovata nel token:', { enteCodice, decoded });
         return false;
       }

       // Crea ente base dalle informazioni JWT
       const ente: Ente = {
         codice: enteCodice,
         codiceFiscale: enteCodice,
         descrizione: enteDenominazione,
         denominazione: enteDenominazione
       };

       this.setContext(ente, annoBilancio ?? undefined, faseBilancio ?? undefined);
       // Persiste info utente per il ripristino al refresh della pagina
       this.authStorage.setUserInfo({
         codiceFiscale: decoded.sub || decoded.codiceFiscale || '',
         nome: decoded.cognome || decoded.nome || decoded.name || decoded.displayName || '',
         ente: enteCodice,
         ruolo: decoded.ruolo || decoded.role || '',
         utenteCodice: decoded.utente_codice || undefined
       });
       console.log('[AuthContextService] loadFromToken - Contesto caricato con successo:', { ente, annoBilancio, faseBilancio });
       return true;
     }

     console.log('[AuthContextService] loadFromToken - ERRORE: enteCodice non trovato nel token');
     return false;
   }

  /**
   * Ottiene il token corrente.
   *
   * @returns Token JWT o null se non presente
   */
  getToken(): string | null {
    return this.authStorage.getToken();
  }

  /**
   * Verifica se l'utente è autenticato.
   *
   * @returns true se c'è un token valido
   */
   isAuthenticated(): boolean {
     const token = this.getToken();
     console.log('[AuthContextService] isAuthenticated - token presente:', !!token);
     
     if (!token) {
       console.log('[AuthContextService] isAuthenticated - RISULTATO: NON AUTENTICATO (token mancante)');
       return false;
     }
     
     // Verifica scadenza token
     try {
       const timeRemaining = this.jwtHelper.getTokenTimeRemaining(token);
       console.log('[AuthContextService] isAuthenticated - timeRemaining (secondi):', timeRemaining);
       const isAuth = timeRemaining > 0;
       console.log('[AuthContextService] isAuthenticated - RISULTATO:', isAuth);
       return isAuth;
     } catch (error) {
       console.error('[AuthContextService] isAuthenticated - ERRORE nel controllo scadenza:', error);
       return false;
     }
   }

  /**
   * Pulisce il contesto e l'autenticazione.
   */
  clearContext(): void {
    this.clear();
    this.authStorage.clear();
  }
}
