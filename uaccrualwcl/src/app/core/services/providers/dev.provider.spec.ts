/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DevProvider } from './dev.provider';
import { LoginRequest } from '../../models/auth.models';
import { environment } from '../../../../environments/environment';

describe('DevProvider', () => {
  let provider: DevProvider;
  let httpMock: HttpTestingController;
  let originalProduction: boolean;

  beforeEach(() => {
    originalProduction = (environment as any).production;
    (environment as any).production = false;

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DevProvider]
    });

    provider = TestBed.inject(DevProvider);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    (environment as any).production = originalProduction;
    httpMock.verify();
  });

  describe('getName()', () => {
    it('dovrebbe restituire "Dev"', () => {
      expect(provider.getName()).toBe('Dev');
    });
  });

  describe('getPriority()', () => {
    it('dovrebbe restituire 1 in sviluppo', () => {
      (environment as any).production = false;
      expect(provider.getPriority()).toBe(1);
    });

    it('dovrebbe restituire 0 in produzione', () => {
      (environment as any).production = true;
      expect(provider.getPriority()).toBe(0);
    });
  });

  describe('isAvailable()', () => {
    it('dovrebbe restituire true solo in sviluppo', () => {
      (environment as any).production = false;
      expect(provider.isAvailable()).toBeTrue();
    });

    it('dovrebbe restituire false in produzione', () => {
      (environment as any).production = true;
      expect(provider.isAvailable()).toBeFalse();
    });
  });

  describe('login()', () => {
    it('dovrebbe caricare contesto da dev-info.json', (done) => {
      const mockDevInfo = {
        user: {
          username: 'DEVUSER',
          displayName: 'Utente di Sviluppo'
        },
        enteCodice: '80087670016',
        enteDenominazione: 'Regione Piemonte',
        annoBilancio: 2026,
        faseBilancio: 'APERTO'
      };

      provider.login({} as LoginRequest).subscribe(result => {
        expect(result.token).toBeTruthy();
        expect(result.data?.userInfo?.codiceFiscale).toBe('DEVUSER');
        expect(result.data?.userInfo?.nome).toBe('Utente di Sviluppo');
        done();
      });

      const req = httpMock.expectOne(environment.authentication.devFileUrl || '/accrualwcl/assets/config/dev-info.json');
      req.flush(mockDevInfo);
    });

    it('dovrebbe usare valori default se dev-info.json non disponibile', (done) => {
      provider.login({} as LoginRequest).subscribe(result => {
        expect(result.token).toBeTruthy();
        expect(result.data?.userInfo?.codiceFiscale).toBe('DEVUSER');
        done();
      });

      const req = httpMock.expectOne(environment.authentication.devFileUrl || '/accrualwcl/assets/config/dev-info.json');
      req.error(new ErrorEvent('Network error'));
    });
  });

  describe('logout()', () => {
    it('dovrebbe restituire Observable completato', (done) => {
      provider.logout().subscribe(() => done());
    });
  });
});
