/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { AuthContextService } from './auth-context.service';
import { AuthenticationService } from './authentication.service';
import { JwtHelper } from '../utils/jwt.helper';
import { Ente } from '../models/ente.model';

describe('AuthContextService', () => {
  let service: AuthContextService;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let jwtHelperSpy: jasmine.SpyObj<JwtHelper>;

  beforeEach(() => {
    const authServiceMock = jasmine.createSpyObj('AuthenticationService', ['getToken']);
    const jwtHelperMock = jasmine.createSpyObj('JwtHelper', ['decodeToken', 'getCodiceEnte', 'getAnnoBilancio', 'getFaseBilancio']);

    TestBed.configureTestingModule({
      providers: [
        AuthContextService,
        { provide: AuthenticationService, useValue: authServiceMock },
        { provide: JwtHelper, useValue: jwtHelperMock }
      ]
    });

    service = TestBed.inject(AuthContextService);
    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    jwtHelperSpy = TestBed.inject(JwtHelper) as jasmine.SpyObj<JwtHelper>;
  });

  describe('setContext()', () => {
    it('dovrebbe impostare il contesto con ente, anno bilancio e fase', () => {
      const ente: Ente = { codice: '80087670016', codiceFiscale: '80087670016', denominazione: 'Regione Piemonte' };
      service.setContext(ente, 2026, 'APERTO');
      expect(service.getCurrentEnte()).toEqual(ente);
      expect(service.getAnnoBilancio()).toBe(2026);
      expect(service.getFaseBilancio()).toBe('APERTO');
    });

    it('dovrebbe impostare il contesto con solo ente', () => {
      const ente: Ente = { codice: '80087670016', codiceFiscale: '80087670016', denominazione: 'Regione Piemonte' };
      service.setContext(ente);
      expect(service.getCurrentEnte()).toEqual(ente);
      expect(service.getAnnoBilancio()).toBeNull();
      expect(service.getFaseBilancio()).toBeNull();
    });
  });

  describe('getCurrentEnte()', () => {
    it('dovrebbe restituire ente corrente', () => {
      const ente: Ente = { codice: '80087670016', codiceFiscale: '80087670016', denominazione: 'Regione Piemonte' };
      service.setContext(ente);
      expect(service.getCurrentEnte()).toEqual(ente);
    });

    it('dovrebbe restituire null se ente non impostato', () => {
      expect(service.getCurrentEnte()).toBeNull();
    });
  });

  describe('getAnnoBilancio()', () => {
    it('dovrebbe restituire anno bilancio corrente', () => {
      const ente: Ente = { codice: '80087670016', codiceFiscale: '80087670016', denominazione: 'Regione Piemonte' };
      service.setContext(ente, 2026);
      expect(service.getAnnoBilancio()).toBe(2026);
    });

    it('dovrebbe restituire null se anno bilancio non impostato', () => {
      expect(service.getAnnoBilancio()).toBeNull();
    });
  });

  describe('getFaseBilancio()', () => {
    it('dovrebbe restituire fase di bilancio corrente', () => {
      const ente: Ente = { codice: '80087670016', codiceFiscale: '80087670016', denominazione: 'Regione Piemonte' };
      service.setContext(ente, 2026, 'APERTO');
      expect(service.getFaseBilancio()).toBe('APERTO');
    });

    it('dovrebbe restituire null se fase non impostata', () => {
      expect(service.getFaseBilancio()).toBeNull();
    });
  });

  describe('clear()', () => {
    it('dovrebbe pulire il contesto', () => {
      const ente: Ente = { codice: '80087670016', codiceFiscale: '80087670016', denominazione: 'Regione Piemonte' };
      service.setContext(ente, 2026, 'APERTO');
      service.clear();
      expect(service.getCurrentEnte()).toBeNull();
      expect(service.getAnnoBilancio()).toBeNull();
      expect(service.getFaseBilancio()).toBeNull();
    });
  });

  describe('loadFromStorage()', () => {
    it('dovrebbe caricare contesto da sessionStorage con token valido', () => {
      const mockToken = 'valid-jwt-token';
      const mockDecoded = { ente_descrizione: 'Regione Piemonte', username: 'testuser' };
      authServiceSpy.getToken.and.returnValue(mockToken);
      jwtHelperSpy.decodeToken.and.returnValue(mockDecoded);
      jwtHelperSpy.getCodiceEnte.and.returnValue('80087670016');
      jwtHelperSpy.getAnnoBilancio.and.returnValue(2026);
      jwtHelperSpy.getFaseBilancio.and.returnValue('APERTO');

      const result = service.loadFromStorage();
      expect(result).toBeTrue();
      expect(service.getCurrentEnte()).toEqual(jasmine.objectContaining({
        codice: '80087670016',
        codiceFiscale: '80087670016',
        denominazione: 'Regione Piemonte'
      }));
      expect(service.getAnnoBilancio()).toBe(2026);
      expect(service.getFaseBilancio()).toBe('APERTO');
    });

    it('dovrebbe restituire false se nessun token presente', () => {
      authServiceSpy.getToken.and.returnValue(null);
      const result = service.loadFromStorage();
      expect(result).toBeFalse();
    });

    it('dovrebbe restituire false se token non decodificabile', () => {
      authServiceSpy.getToken.and.returnValue('invalid-token');
      jwtHelperSpy.decodeToken.and.returnValue(null);
      const result = service.loadFromStorage();
      expect(result).toBeFalse();
    });

    it('dovrebbe restituire false se ente non presente nel token', () => {
      const mockToken = 'token-senza-ente';
      const mockDecoded = { username: 'testuser' };
      authServiceSpy.getToken.and.returnValue(mockToken);
      jwtHelperSpy.decodeToken.and.returnValue(mockDecoded);
      jwtHelperSpy.getCodiceEnte.and.returnValue(null);
      const result = service.loadFromStorage();
      expect(result).toBeFalse();
    });
  });
});
