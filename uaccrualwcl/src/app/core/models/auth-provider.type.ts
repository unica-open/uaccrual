/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

/**
 * Tipo forte per i provider di autenticazione supportati.
 * 
 * @see AuthProviderUtils per le funzioni di normalizzazione
 */
export type AuthProviderType = 'SHIBBOLETH' | 'SPRING_SECURITY' | 'TOKEN' | 'DEV';

/**
 * Utility per la normalizzazione e conversione dei provider.
 * 
 * @example
 * // Normalizza stringa da query param
 * const provider = AuthProviderUtils.normalize('shibboleth'); // 'SHIBBOLETH'
 * const provider2 = AuthProviderUtils.normalize('SIAC_SHIB'); // 'SHIBBOLETH'
 * 
 * // Converte per backend
 * const backendString = AuthProviderUtils.toBackendString('SHIBBOLETH'); // 'Shibboleth'
 */
export const AuthProviderUtils = {
  /**
   * Normalizza una stringa raw da query param in tipo forte AuthProviderType.
   * Supporta varianti comuni per ciascun provider.
   *
   * @param raw Stringa grezza da normalizzare (case-insensitive)
   * @returns AuthProviderType normalizzato
   * @throws Error se il provider non è riconosciuto
   *
   * @example
   * normalize('shibboleth') → 'SHIBBOLETH'
   * normalize('SIAC_SHIB') → 'SHIBBOLETH'
   * normalize('spring') → 'SPRING_SECURITY'
   * normalize('Token') → 'TOKEN'
   * normalize('dev') → 'DEV'
   */
  normalize(raw: string): AuthProviderType {
    if (!raw) {
      throw new Error('Provider non specificato');
    }

    // Normalizza: lowercase, trim, rimuovi separatori
    const normalized = raw.toLowerCase().trim().replace(/[_-]/g, '');

    // Shibboleth variants
    if (['shibboleth', 'siacshib', 'shib', 'sib'].includes(normalized)) {
      return 'SHIBBOLETH';
    }

    // Spring Security variants
    if (['springsecurity', 'spring', 'extspring', 'extspring'].includes(normalized)) {
      return 'SPRING_SECURITY';
    }

    // Token variants
    if (['token', 'exttoken', 'jwt'].includes(normalized)) {
      return 'TOKEN';
    }

    // Dev variants
    if (['dev', 'development', 'local'].includes(normalized)) {
      return 'DEV';
    }

    throw new Error(`Provider non riconosciuto: ${raw}`);
  },
  
  /**
   * Converte un tipo forte AuthProviderType in stringa per il backend.
   * 
   * @param type Tipo forte AuthProviderType
   * @returns Stringa formattata per il backend (PascalCase)
   * 
   * @example
   * toBackendString('SHIBBOLETH') → 'Shibboleth'
   * toBackendString('SPRING_SECURITY') → 'SpringSecurity'
   */
  toBackendString(type: AuthProviderType): string {
    switch (type) {
      case 'SHIBBOLETH':
        return 'Shibboleth';
      case 'SPRING_SECURITY':
        return 'SpringSecurity';
      case 'TOKEN':
        return 'Token';
      case 'DEV':
        return 'Dev';
    }
  }
};
