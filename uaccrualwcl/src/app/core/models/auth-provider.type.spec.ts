/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { AuthProviderUtils, AuthProviderType } from './auth-provider.type';

describe('AuthProviderUtils', () => {
  describe('normalize()', () => {
    describe('Shibboleth variants', () => {
      it('dovrebbe normalizzare "shibboleth" in SHIBBOLETH', () => {
        expect(AuthProviderUtils.normalize('shibboleth')).toBe('SHIBBOLETH');
      });

      it('dovrebbe normalizzare "SIAC_SHIB" in SHIBBOLETH', () => {
        expect(AuthProviderUtils.normalize('SIAC_SHIB')).toBe('SHIBBOLETH');
      });

      it('dovrebbe normalizzare "Shib" in SHIBBOLETH', () => {
        expect(AuthProviderUtils.normalize('Shib')).toBe('SHIBBOLETH');
      });

      it('dovrebbe normalizzare "sib" in SHIBBOLETH', () => {
        expect(AuthProviderUtils.normalize('sib')).toBe('SHIBBOLETH');
      });
    });

    describe('Spring Security variants', () => {
      it('dovrebbe normalizzare "SpringSecurity" in SPRING_SECURITY', () => {
        expect(AuthProviderUtils.normalize('SpringSecurity')).toBe('SPRING_SECURITY');
      });

      it('dovrebbe normalizzare "spring" in SPRING_SECURITY', () => {
        expect(AuthProviderUtils.normalize('spring')).toBe('SPRING_SECURITY');
      });

      it('dovrebbe normalizzare "ext_spring" in SPRING_SECURITY', () => {
        expect(AuthProviderUtils.normalize('ext_spring')).toBe('SPRING_SECURITY');
      });
    });

    describe('Token variants', () => {
      it('dovrebbe normalizzare "Token" in TOKEN', () => {
        expect(AuthProviderUtils.normalize('Token')).toBe('TOKEN');
      });

      it('dovrebbe normalizzare "JWT" in TOKEN', () => {
        expect(AuthProviderUtils.normalize('JWT')).toBe('TOKEN');
      });
    });

    describe('Dev variants', () => {
      it('dovrebbe normalizzare "dev" in DEV', () => {
        expect(AuthProviderUtils.normalize('dev')).toBe('DEV');
      });

      it('dovrebbe normalizzare "development" in DEV', () => {
        expect(AuthProviderUtils.normalize('development')).toBe('DEV');
      });
    });

    describe('Error cases', () => {
      it('dovrebbe lanciare errore per provider sconosciuto', () => {
        expect(() => AuthProviderUtils.normalize('unknown')).toThrowError('Provider non riconosciuto: unknown');
      });

      it('dovrebbe lanciare errore per stringa vuota', () => {
        expect(() => AuthProviderUtils.normalize('')).toThrowError('Provider non specificato');
      });

      it('dovrebbe lanciare errore per null', () => {
        expect(() => AuthProviderUtils.normalize(null as any)).toThrowError('Provider non specificato');
      });
    });
  });

  describe('toBackendString()', () => {
    it('dovrebbe convertire SHIBBOLETH in "Shibboleth"', () => {
      expect(AuthProviderUtils.toBackendString('SHIBBOLETH')).toBe('Shibboleth');
    });

    it('dovrebbe convertire SPRING_SECURITY in "SpringSecurity"', () => {
      expect(AuthProviderUtils.toBackendString('SPRING_SECURITY')).toBe('SpringSecurity');
    });

    it('dovrebbe convertire TOKEN in "Token"', () => {
      expect(AuthProviderUtils.toBackendString('TOKEN')).toBe('Token');
    });

    it('dovrebbe convertire DEV in "Dev"', () => {
      expect(AuthProviderUtils.toBackendString('DEV')).toBe('Dev');
    });
  });
});
