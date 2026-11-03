/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class JwtHelper {
  /**
   * Decodifica il payload di un JWT senza validare la firma
   * Utile per verificare scadenza e claims senza chiamare il backend
   */
  decodeToken(token: string): any {
    if (!token) return null;

    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        throw new Error('Token non valido');
      }

      const payload = parts[1];
      const decoded = atob(payload); // Decode base64
      return JSON.parse(decoded);
    } catch (e) {
      console.error('Errore nella decodifica del token:', e);
      return null;
    }
  }

  /**
   * Verifica se un token è scaduto
   * @param token JWT token
   * @returns true se scaduto, false se ancora valido
   */
  isTokenExpired(token: string): boolean {
    const decoded = this.decodeToken(token);
    
    if (!decoded) {
      return true; // Token non valido = considerato scaduto
    }
    
    if (!decoded.exp) {
      return true; // Nessun claim exp = considerato scaduto
    }
    
    // exp è in secondi, Date.now() è in millisecondi
    const expirationDate = decoded.exp * 1000;
    const now = Date.now();
    
    // Consideriamo scaduto se manca meno di 30 secondi (margine di sicurezza)
    return expirationDate < now + 30000;
  }

  /**
   * Ottiene la data di scadenza del token
   * @param token JWT token
   * @returns Date di scadenza o null se non presente
   */
  getTokenExpiration(token: string): Date | null {
    const decoded = this.decodeToken(token);
    
    if (!decoded || !decoded.exp) {
      return null;
    }
    
    return new Date(decoded.exp * 1000);
  }

  /**
   * Ottiene il tempo rimanente prima della scadenza (in secondi)
   * @param token JWT token
   * @returns Secondi rimanenti, o 0 se scaduto
   */
  getTokenTimeRemaining(token: string): number {
    const expiration = this.getTokenExpiration(token);

    if (!expiration) {
      return 0;
    }

    const remaining = expiration.getTime() - Date.now();
    return Math.max(0, Math.floor(remaining / 1000));
  }

  /**
   * Estrae il codice fiscale ente dal token JWT.
   * Priorità: codice_ente (nuovo snake_case) → ente_codice_univoco (vecchio) → altri
   * @param token JWT token
   * @returns Codice fiscale ente o null se non presente
   */
  getCodiceEnte(token: string): string | null {
    const decoded = this.decodeToken(token);

    if (!decoded) {
      return null;
    }

    // Priorità ai nuovi claim snake_case, poi backward compatibility
    return decoded.codice_ente ||
           decoded.ente_codice_univoco ||
           decoded.codiceFiscaleEnte ||
           decoded.codice_ente ||
           decoded.ente ||
           decoded.enteId ||
           null;
  }

  /**
   * Estrae l'anno di bilancio dal token JWT.
   * @param token JWT token
   * @returns Anno di bilancio o null se non presente
   */
  getAnnoBilancio(token: string): number | null {
    const decoded = this.decodeToken(token);

    if (!decoded) {
      return null;
    }

    return decoded.anno_bilancio ? Number(decoded.anno_bilancio) : null;
  }

  /**
   * Estrae la fase di bilancio dal token JWT.
   * Priorità: fase_bilancio (nuovo snake_case) → faseBilancio (vecchio)
   * @param token JWT token
   * @returns Fase di bilancio o null se non presente
   */
  getFaseBilancio(token: string): string | null {
    const decoded = this.decodeToken(token);

    if (!decoded) {
      return null;
    }

    // Priorità ai nuovi claim snake_case, poi backward compatibility
    return decoded.fase_bilancio ||
           decoded.faseBilancio ||
           decoded.fase ||
           null;
  }
}
