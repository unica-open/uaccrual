/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { SessionProvider } from './session.provider';
import { AuthStorageService } from '../auth-storage.service';
import { JwtHelper } from '../../utils/jwt.helper';
import { LoginRequest } from '../../models/auth.models';

describe('SessionProvider', () => {
  let provider: SessionProvider;
  let authStorageSpy: jasmine.SpyObj<AuthStorageService>;
  let jwtHelperSpy: jasmine.SpyObj<JwtHelper>;

  beforeEach(() => {
    const authStorageMock = jasmine.createSpyObj('AuthStorageService', ['getToken', 'clear']);
    const jwtHelperMock = jasmine.createSpyObj('JwtHelper', ['isTokenExpired', 'decodeToken', 'getCodiceEnte']);

    TestBed.configureTestingModule({
      providers: [
        SessionProvider,
        { provide: AuthStorageService, useValue: authStorageMock },
        { provide: JwtHelper, useValue: jwtHelperMock }
      ]
    });

    provider = TestBed.inject(SessionProvider);
    authStorageSpy = TestBed.inject(AuthStorageService) as jasmine.SpyObj<AuthStorageService>;
    jwtHelperSpy = TestBed.inject(JwtHelper) as jasmine.SpyObj<JwtHelper>;
  });

  describe('getName()', () => {
    it('dovrebbe restituire "Session"', () => {
      expect(provider.getName()).toBe('Session');
    });
  });

  describe('getPriority()', () => {
    it('dovrebbe restituire 100 se sessione valida', () => {
      authStorageSpy.getToken.and.returnValue('valid-token');
      jwtHelperSpy.isTokenExpired.and.returnValue(false);
      expect(provider.getPriority()).toBe(100);
    });

    it('dovrebbe restituire 0 se nessun token', () => {
      authStorageSpy.getToken.and.returnValue(null);
      expect(provider.getPriority()).toBe(0);
    });

    it('dovrebbe restituire 0 se token scaduto', () => {
      authStorageSpy.getToken.and.returnValue('expired-token');
      jwtHelperSpy.isTokenExpired.and.returnValue(true);
      expect(provider.getPriority()).toBe(0);
    });
  });

  describe('isAvailable()', () => {
    it('dovrebbe restituire true se token valido presente', () => {
      authStorageSpy.getToken.and.returnValue('valid-token');
      jwtHelperSpy.isTokenExpired.and.returnValue(false);
      expect(provider.isAvailable()).toBeTrue();
    });

    it('dovrebbe restituire false se nessun token', () => {
      authStorageSpy.getToken.and.returnValue(null);
      expect(provider.isAvailable()).toBeFalse();
    });

    it('dovrebbe restituire false se token scaduto', () => {
      authStorageSpy.getToken.and.returnValue('expired-token');
      jwtHelperSpy.isTokenExpired.and.returnValue(true);
      expect(provider.isAvailable()).toBeFalse();
    });
  });

  describe('login()', () => {
    it('dovrebbe restituire Observable con token esistente', (done) => {
      const mockToken = 'existing-jwt-token';
      const mockDecoded = { sub: 'testuser', nome: 'Test User' };

      authStorageSpy.getToken.and.returnValue(mockToken);
      jwtHelperSpy.decodeToken.and.returnValue(mockDecoded);
      jwtHelperSpy.getCodiceEnte.and.returnValue('80087670016');

      provider.login({} as LoginRequest).subscribe(result => {
        expect(result.token).toBe(mockToken);
        expect(result.data?.token).toBe(mockToken);
        done();
      });
    });

    it('dovrebbe lanciare errore se nessun token presente', () => {
      authStorageSpy.getToken.and.returnValue(null);
      expect(() => provider.login({} as LoginRequest).subscribe()).toThrowError('Nessun token presente in sessionStorage');
    });
  });

  describe('logout()', () => {
    it('dovrebbe pulire sessionStorage e restituire Observable completato', (done) => {
      provider.logout().subscribe(() => {
        expect(authStorageSpy.clear).toHaveBeenCalled();
        done();
      });
    });
  });
});
