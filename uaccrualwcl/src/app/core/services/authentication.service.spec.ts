/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthenticationService } from './authentication.service';
import { AuthContextService } from './auth-context.service';
import { CredentialsProvider } from './providers/credentials.provider';
import { SessionProvider } from './providers/session.provider';
import { DevProvider } from './providers/dev.provider';
import { JwtHelper } from '../utils/jwt.helper';
import { AuthStorageService } from './auth-storage.service';
import { Store } from '@ngxs/store';
import { LoginRequest } from '../models/auth.models';
import { of } from 'rxjs';

describe('AuthenticationService (Refactored)', () => {
  let service: AuthenticationService;
  let authContextSpy: jasmine.SpyObj<AuthContextService>;
  let credentialsProviderSpy: jasmine.SpyObj<CredentialsProvider>;
  let sessionProviderSpy: jasmine.SpyObj<SessionProvider>;
  let devProviderSpy: jasmine.SpyObj<DevProvider>;
  let jwtHelperSpy: jasmine.SpyObj<JwtHelper>;
  let authStorageSpy: jasmine.SpyObj<AuthStorageService>;

  beforeEach(() => {
    const authContextMock = jasmine.createSpyObj('AuthContextService', ['setContext', 'getToken', 'setCurrentEnte', 'clearContext', 'isAuthenticated']);
    const credentialsProviderMock = jasmine.createSpyObj('CredentialsProvider', ['getName', 'isAvailable', 'login', 'logout', 'getPriority']);
    const sessionProviderMock = jasmine.createSpyObj('SessionProvider', ['getName', 'isAvailable', 'login', 'logout', 'getPriority']);
    const devProviderMock = jasmine.createSpyObj('DevProvider', ['getName', 'isAvailable', 'login', 'logout', 'getPriority']);
    const jwtHelperMock = jasmine.createSpyObj('JwtHelper', ['decodeToken', 'getCodiceEnte', 'getAnnoBilancio', 'getFaseBilancio']);
    const authStorageMock = jasmine.createSpyObj('AuthStorageService', ['getToken', 'getUserInfo']);
    const storeMock = jasmine.createSpyObj('Store', ['dispatch']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthenticationService,
        { provide: AuthContextService, useValue: authContextMock },
        { provide: CredentialsProvider, useValue: credentialsProviderMock },
        { provide: SessionProvider, useValue: sessionProviderMock },
        { provide: DevProvider, useValue: devProviderMock },
        { provide: JwtHelper, useValue: jwtHelperMock },
        { provide: AuthStorageService, useValue: authStorageMock },
        { provide: Store, useValue: storeMock }
      ]
    });

    service = TestBed.inject(AuthenticationService);
    authContextSpy = TestBed.inject(AuthContextService) as jasmine.SpyObj<AuthContextService>;
    credentialsProviderSpy = TestBed.inject(CredentialsProvider) as jasmine.SpyObj<CredentialsProvider>;
    sessionProviderSpy = TestBed.inject(SessionProvider) as jasmine.SpyObj<SessionProvider>;
    devProviderSpy = TestBed.inject(DevProvider) as jasmine.SpyObj<DevProvider>;
    jwtHelperSpy = TestBed.inject(JwtHelper) as jasmine.SpyObj<JwtHelper>;
    authStorageSpy = TestBed.inject(AuthStorageService) as jasmine.SpyObj<AuthStorageService>;
  });

  describe('selectProvider()', () => {
    it('dovrebbe selezionare provider con priorità più alta disponibile', () => {
      sessionProviderSpy.isAvailable.and.returnValue(false);
      credentialsProviderSpy.isAvailable.and.returnValue(true);
      credentialsProviderSpy.getPriority.and.returnValue(10);
      devProviderSpy.isAvailable.and.returnValue(false);
      const provider = service['selectProvider']();
      expect(provider).toBe(credentialsProviderSpy);
    });

    it('dovrebbe fare fallback a CredentialsProvider', () => {
      sessionProviderSpy.isAvailable.and.returnValue(false);
      credentialsProviderSpy.isAvailable.and.returnValue(false);
      devProviderSpy.isAvailable.and.returnValue(false);
      const provider = service['selectProvider']();
      expect(provider).toBe(credentialsProviderSpy);
    });
  });

  describe('login()', () => {
    it('dovrebbe selezionare provider e delegare login', (done) => {
      const loginRequest: LoginRequest = { codiceFiscale: 'test', provider: 'SPRING_SECURITY', password: 'pwd' };
      const mockResponse = { token: 'jwt-token' };
      credentialsProviderSpy.login.and.returnValue(of(mockResponse as any));
      jwtHelperSpy.decodeToken.and.returnValue({ sub: 'testuser', nome: 'Test User' });
      jwtHelperSpy.getCodiceEnte.and.returnValue('80087670016');
      jwtHelperSpy.getAnnoBilancio.and.returnValue(2026);
      jwtHelperSpy.getFaseBilancio.and.returnValue('APERTO');
      service.login(loginRequest).subscribe(result => {
        expect(result.token).toBe('jwt-token');
        expect(credentialsProviderSpy.login).toHaveBeenCalledWith(loginRequest);
        done();
      });
    });

    it('dovrebbe impostare contesto dopo login riuscito', (done) => {
      const loginRequest: LoginRequest = { codiceFiscale: 'test', provider: 'SPRING_SECURITY', password: 'pwd' };
      const mockResponse = { token: 'jwt-token' };
      credentialsProviderSpy.login.and.returnValue(of(mockResponse as any));
      jwtHelperSpy.decodeToken.and.returnValue({ sub: 'testuser', nome: 'Test User', enteDenominazione: 'Regione Piemonte' });
      jwtHelperSpy.getCodiceEnte.and.returnValue('80087670016');
      service.login(loginRequest).subscribe(() => {
        expect(authContextSpy.setContext).toHaveBeenCalled();
        done();
      });
    });
  });

  describe('logout()', () => {
    it('dovrebbe selezionare provider e chiamare logout', (done) => {
      sessionProviderSpy.isAvailable.and.returnValue(true);
      sessionProviderSpy.logout.and.returnValue(of(undefined));
      service.logout().subscribe(() => {
        expect(sessionProviderSpy.logout).toHaveBeenCalled();
        done();
      });
    });

    it('dovrebbe pulire auth locale dopo logout', (done) => {
      sessionProviderSpy.isAvailable.and.returnValue(true);
      sessionProviderSpy.logout.and.returnValue(of(undefined));
      service.logout().subscribe(() => {
        expect(authContextSpy.clearContext).toHaveBeenCalled();
        done();
      });
    });
  });

  describe('getToken()', () => {
    it('dovrebbe restituire token da AuthContextService', () => {
      authContextSpy.getToken.and.returnValue('context-token');
      authStorageSpy.getToken.and.returnValue('storage-token');
      expect(service.getToken()).toBe('context-token');
    });

    it('dovrebbe fallback a AuthStorageService', () => {
      authContextSpy.getToken.and.returnValue(null);
      authStorageSpy.getToken.and.returnValue('storage-token');
      expect(service.getToken()).toBe('storage-token');
    });
  });

  describe('isAuthenticated()', () => {
    it('dovrebbe restituire stato autenticazione', () => {
      authContextSpy.isAuthenticated.and.returnValue(true);
      expect(service.isAuthenticated()).toBeTrue();
    });

    it('dovrebbe restituire false se non autenticato', () => {
      authContextSpy.isAuthenticated.and.returnValue(false);
      expect(service.isAuthenticated()).toBeFalse();
    });
  });

  describe('clearAuth()', () => {
    it('dovrebbe pulire tutto lo stato', () => {
      service.clearAuth();
      expect(authContextSpy.clearContext).toHaveBeenCalled();
    });
  });

  describe('setEnte()', () => {
    it('dovrebbe impostare ente corrente', () => {
      service['availableEnti'] = [{ 
        codice: '80087670016',
        codiceFiscale: '80087670016', 
        denominazione: 'Regione Piemonte' 
      }];
      service.setEnte('80087670016');
      expect(service.getCurrentEnte()).toBe('80087670016');
      expect(authContextSpy.setCurrentEnte).toHaveBeenCalledWith('80087670016');
    });

    it('dovrebbe lanciare errore se ente non disponibile', () => {
      service['availableEnti'] = [{ 
        codice: '80087670016',
        codiceFiscale: '80087670016', 
        denominazione: 'Regione Piemonte' 
      }];
      expect(() => service.setEnte('ENTE_NON_VALIDO')).toThrow();
    });
  });
});
