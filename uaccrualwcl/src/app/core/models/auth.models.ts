/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Modelli per l'autenticazione.
 * 
 * @see AuthenticationService
 * @see AuthProvider
 */

import { AuthProviderType } from './auth-provider.type';
import { Ente, UserInfoComplete } from './ente.model';

/**
 * Richiesta di login al backend.
 * Supporta sia login con credenziali che con token esterno.
 */
export interface LoginRequest {
  /** Codice fiscale dell'utente (per login con credenziali) */
  codiceFiscale?: string;
  
  /** Password dell'utente (per login con credenziali) */
  password?: string;
  
  /** Token esterno (per login Shibboleth o Token) */
  token?: string;
  
  /** Provider di autenticazione usato */
  provider: AuthProviderType | 'SpringSecurity' | 'Shibboleth' | 'Token';
  
  /** Codice ente univoco (opzionale, preso da JWT se non fornito) */
  enteCodiceUnivoco?: string;
  
  /** Anno di bilancio (opzionale) */
  annoBilancio?: number;

  /** Fase di bilancio (opzionale) */
  faseBilancio?: string;
}

/**
 * Risposta del backend al login.
 */
export interface LoginResponse {
  /** Token JWT generato */
  token?: string;
  
  /** Status HTTP della risposta */
  status?: number;
  
  /** Messaggio di errore o successo */
  message?: string;
  
  /** Dati aggiuntivi della risposta */
  data?: {
    /** Token JWT (alternativa a token root-level) */
    token?: string;
    
    /** Indica se è il primo accesso dell'utente */
    primoAccesso?: boolean;
    
    /** Codice utente */
    utenteCodice?: string;
    
    /** Informazioni utente (se presenti nella risposta) */
    userInfo?: {
      codiceFiscale: string;
      nome: string;
      ente: string;
      ruolo: string;
    };
  };
}

/**
 * Richiesta di reset password.
 */
export interface ResetPasswordRequest {
  /** Codice fiscale dell'utente */
  codiceFiscale: string;
}

/**
 * Richiesta di conferma reset password.
 */
export interface ConfirmPasswordResetRequest {
  /** Token di reset ricevuto via email */
  token: string;
  
  /** Codice fiscale dell'utente */
  codiceFiscale: string;
  
  /** Nuova password */
  password: string;
  
  /** Conferma nuova password */
  confirmPassword: string;
}

/**
 * Richiesta di cambio password.
 */
export interface ChangePasswordRequest {
  /** Codice fiscale dell'utente */
  codiceFiscale: string;
  
  /** Vecchia password (opzionale per primo accesso) */
  vecchiaPassword?: string;
  
  /** Nuova password */
  nuovaPassword: string;
  
  /** Conferma nuova password */
  confermaPassword: string;
}

/**
 * Informazioni utente complete per il contesto.
 */
export interface AuthUserInfo extends UserInfoComplete {
  /** Username per il login */
  username: string;
  
  /** Nome visualizzato */
  displayName: string;
}

/**
 * Stato del contesto di autenticazione.
 */
export interface AuthContextState {
  /** Utente corrente */
  user: AuthUserInfo | null;
  
  /** Ente corrente */
  currentEnte: Ente | null;
  
  /** Lista degli enti disponibili */
  availableEnti: Ente[];
  
  /** Anno di bilancio */
  annoBilancio: number | null;
  
  /** Fase di bilancio */
  faseBilancio: string | null;

  /** Token JWT corrente */
  token: string | null;
}

// Re-export per comodità
export type { UserInfoComplete, Ente } from './ente.model';
