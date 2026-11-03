/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CredentialsProvider } from './credentials.provider';
import { LoginRequest, LoginResponse } from '../../models/auth.models';
import { environment } from '../../../../environments/environment';

describe('CredentialsProvider', () => {
  let provider: CredentialsProvider;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CredentialsProvider]
    });

    provider = TestBed.inject(CredentialsProvider);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('getName()', () => {
    it('dovrebbe restituire "SpringSecurity"', () => {
      expect(provider.getName()).toBe('SpringSecurity');
    });
  });

  describe('getPriority()', () => {
    it('dovrebbe restituire 10', () => {
      expect(provider.getPriority()).toBe(10);
    });
  });

  describe('isAvailable()', () => {
    it('dovrebbe restituire sempre true (fallback)', () => {
      expect(provider.isAvailable()).toBeTrue();
    });
  });

  describe('login()', () => {
    it('dovrebbe chiamare backend con credenziali e provider SpringSecurity', (done) => {
      const loginRequest: LoginRequest = {
        codiceFiscale: 'RSSMRA80A01H501U',
        password: 'password123',
        provider: 'SPRING_SECURITY',
        enteCodiceUnivoco: '80087670016'
      };

      const mockResponse: LoginResponse = { token: 'jwt-token' };

      provider.login(loginRequest).subscribe(result => {
        expect(result.token).toBe('jwt-token');
        done();
      });

      const req = httpMock.expectOne(`${environment.services.accrualAuthApiUrl}/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body.codiceFiscale).toBe('RSSMRA80A01H501U');
      expect(req.request.body.password).toBe('password123');
      expect(req.request.body.provider).toBe('SpringSecurity');
      expect(req.request.body.enteCodiceUnivoco).toBe('80087670016');

      req.flush(mockResponse);
    });

    it('dovrebbe includere annoBilancio e faseBilancio se presenti', (done) => {
      const loginRequest: LoginRequest = {
        codiceFiscale: 'testuser',
        password: 'pwd',
        provider: 'SPRING_SECURITY',
        annoBilancio: 2026,
        faseBilancio: 'APERTO'
      };

      provider.login(loginRequest).subscribe(() => done());

      const req = httpMock.expectOne(`${environment.services.accrualAuthApiUrl}/login`);
      expect(req.request.body.anno_bilancio).toBe(2026);
      expect(req.request.body.fase_bilancio).toBe('APERTO');
      req.flush({ token: 'token' });
    });
  });

  describe('logout()', () => {
    it('dovrebbe chiamare backend logout', (done) => {
      provider.logout().subscribe(() => done());

      const req = httpMock.expectOne(`${environment.services.accrualAuthApiUrl}/logout`);
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });
});
