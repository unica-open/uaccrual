/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { 
  HttpTestingController, 
  HttpClientTestingModule 
} from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { AuthInterceptor } from './auth.interceptor';
import { AuthenticationService } from '../services/authentication.service';
import { AuthStorageService } from '../services/auth-storage.service';
import { Router } from '@angular/router';
import { NgZone } from '@angular/core';

describe('AuthInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let authStorageSpy: jasmine.SpyObj<AuthStorageService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let zone: NgZone;

  const TEST_API_URL = 'http://test.local/api';

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj('AuthenticationService', [
      'getToken',
      'clearAuth',
      'getUserInfo'
    ]);

    authStorageSpy = jasmine.createSpyObj('AuthStorageService', ['clear', 'getUserInfo']);

    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    Object.defineProperty(routerSpy, 'url', {
      value: '/dashboard',
      writable: true,
      configurable: true
    });

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: HTTP_INTERCEPTORS,
          useClass: AuthInterceptor,
          multi: true
        },
        { provide: AuthenticationService, useValue: authServiceSpy },
        { provide: AuthStorageService, useValue: authStorageSpy },
        { provide: Router, useValue: routerSpy },
        { provide: NgZone, useFactory: () => new NgZone({ enableLongStackTrace: false }) }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    zone = TestBed.inject(NgZone);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Aggiunta Token JWT', () => {
    it('dovrebbe aggiungere header Authorization con token valido per accrualsbsrv', () => {
      const mockToken = 'fake-jwt-token';
      authServiceSpy.getToken.and.returnValue(mockToken);
      const url = '/accrualsbsrv/api/v1/pdc';

      httpClient.get(url).subscribe(response => {
        expect(response).toBeTruthy();
      });

      const httpRequest = httpMock.expectOne(url);
      expect(httpRequest.request.headers.has('Authorization')).toBeTrue();
      expect(httpRequest.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

      httpRequest.flush({});
    });

    it('dovrebbe aggiungere token a richieste verso accrualauthsbapi', () => {
      const mockToken = 'jwt-token';
      authServiceSpy.getToken.and.returnValue(mockToken);
      const url = '/accrualauthsbapi/api/v1/logout';

      httpClient.get(url).subscribe(() => {});

      const httpRequest = httpMock.expectOne(url);
      expect(httpRequest.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

      httpRequest.flush({});
    });

    it('dovrebbe aggiungere token a richieste verso accrualreportsbapi', () => {
      const mockToken = 'jwt-token';
      authServiceSpy.getToken.and.returnValue(mockToken);
      const url = '/accrualreportsbapi/api/v1/report';

      httpClient.get(url).subscribe(() => {});

      const httpRequest = httpMock.expectOne(url);
      expect(httpRequest.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

      httpRequest.flush({});
    });

    it('dovrebbe aggiungere token a richieste verso accrualsbapi', () => {
      const mockToken = 'jwt-token';
      authServiceSpy.getToken.and.returnValue(mockToken);
      const url = '/accrualsbapi/api/v1/data';

      httpClient.get(url).subscribe(() => {});

      const httpRequest = httpMock.expectOne(url);
      expect(httpRequest.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

      httpRequest.flush({});
    });

    it('NON dovrebbe aggiungere header se token assente', () => {
      authServiceSpy.getToken.and.returnValue(null);

      httpClient.get(TEST_API_URL).subscribe(response => {
        expect(response).toBeTruthy();
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      expect(httpRequest.request.headers.has('Authorization')).toBeFalse();

      httpRequest.flush({});
    });

    it('NON dovrebbe aggiungere header per URL esterni non autorizzati', () => {
      const mockToken = 'jwt-token';
      authServiceSpy.getToken.and.returnValue(mockToken);
      const externalUrl = 'http://external-api.com/data';

      httpClient.get(externalUrl).subscribe(() => {});

      const httpRequest = httpMock.expectOne(externalUrl);
      expect(httpRequest.request.headers.has('Authorization')).toBeFalse();

      httpRequest.flush({});
    });
  });

  describe('Gestione Errori 401/403', () => {
    const mockUserInfo = {
      codiceFiscale: '80087670016',
      nome: 'Test User',
      ente: '80087670016',
      ruolo: 'ADMIN',
      source: 'EXT_SPRING'
    };

    it('dovrebbe chiamare clearAuth e clear su 401', (done) => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

      httpClient.get(TEST_API_URL).subscribe({
        error: (error) => {
          expect(error.status).toBe(401);
          done();
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

      expect(authStorageSpy.clear).toHaveBeenCalled();
      expect(authServiceSpy.clearAuth).toHaveBeenCalled();
    });

    it('dovrebbe chiamare clearAuth e clear su 403', (done) => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

      httpClient.get(TEST_API_URL).subscribe({
        error: (error) => {
          expect(error.status).toBe(403);
          done();
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

      expect(authStorageSpy.clear).toHaveBeenCalled();
      expect(authServiceSpy.clearAuth).toHaveBeenCalled();
    });

    it('dovrebbe navigare a /session-expired su 401', (done) => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

      httpClient.get(TEST_API_URL).subscribe({
        error: () => {
          setTimeout(() => {
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/session-expired'], jasmine.objectContaining({
              queryParams: jasmine.objectContaining({
                provider: 'EXT_SPRING',
                returnUrl: '/dashboard'
              })
            }));
            done();
          }, 100);
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('dovrebbe navigare a /session-expired su 403', (done) => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

      httpClient.get(TEST_API_URL).subscribe({
        error: () => {
          setTimeout(() => {
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/session-expired'], jasmine.objectContaining({
              queryParams: jasmine.objectContaining({
                provider: 'EXT_SPRING',
                returnUrl: '/dashboard'
              })
            }));
            done();
          }, 100);
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });

    it('dovrebbe propagare errore per status diversi da 401/403', () => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

      httpClient.get(TEST_API_URL).subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Internal Error', { status: 500, statusText: 'Internal Server Error' });

      expect(authStorageSpy.clear).not.toHaveBeenCalled();
      expect(authServiceSpy.clearAuth).not.toHaveBeenCalled();
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    });

    it('dovrebbe propagare errore al chiamante', (done) => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);
      let errorCaught = false;

      httpClient.get(TEST_API_URL).subscribe({
        error: () => {
          errorCaught = true;
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Error', { status: 401, statusText: 'Unauthorized' });

      setTimeout(() => {
        expect(errorCaught).toBeTrue();
        done();
      }, 100);
    });

    it('NON dovrebbe navigare se già su session-expired', (done) => {
      (routerSpy.url as any) = '/session-expired';
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

      httpClient.get(TEST_API_URL).subscribe({
        error: () => {
          setTimeout(() => {
            expect(routerSpy.navigate).not.toHaveBeenCalled();
            done();
          }, 100);
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('NON dovrebbe navigare se già su login', (done) => {
      (routerSpy.url as any) = '/login';
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);

      httpClient.get(TEST_API_URL).subscribe({
        error: () => {
          setTimeout(() => {
            expect(routerSpy.navigate).not.toHaveBeenCalled();
            done();
          }, 100);
        }
      });

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('Logging Error', () => {
    const mockUserInfo = {
      codiceFiscale: '80087670016',
      nome: 'Test User',
      ente: '80087670016',
      ruolo: 'ADMIN',
      source: 'EXT_SPRING'
    };

    it('dovrebbe loggare errore 401 con dettagli', (done) => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);
      spyOn(console, 'error');

      httpClient.get(TEST_API_URL).subscribe({ error: () => {
        setTimeout(() => {
          expect(console.error).toHaveBeenCalledWith(
            '[AuthInterceptor] Errore autorizzazione:',
            jasmine.objectContaining({
              status: 401,
              url: TEST_API_URL
            })
          );
          done();
        }, 100);
      }});

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    });

    it('dovrebbe loggare errore 403 con dettagli', (done) => {
      authServiceSpy.getToken.and.returnValue('valid-token');
      authServiceSpy.getUserInfo.and.returnValue(mockUserInfo);
      spyOn(console, 'error');

      httpClient.get(TEST_API_URL).subscribe({ error: () => {
        setTimeout(() => {
          expect(console.error).toHaveBeenCalledWith(
            '[AuthInterceptor] Errore autorizzazione:',
            jasmine.objectContaining({
              status: 403,
              url: TEST_API_URL
            })
          );
          done();
        }, 100);
      }});

      const httpRequest = httpMock.expectOne(TEST_API_URL);
      httpRequest.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });
  });

  describe('Endpoint Login', () => {
    it('NON dovrebbe gestire 401 per endpoint login (credenziali sbagliate)', () => {
      authServiceSpy.getToken.and.returnValue(null);
      const loginUrl = '/accrualauthsbapi/api/v1/login';

      httpClient.post(loginUrl, { username: 'test', password: 'wrong' }).subscribe({
        error: (error) => {
          expect(error.status).toBe(401);
        }
      });

      const httpRequest = httpMock.expectOne(loginUrl);
      httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

      expect(authStorageSpy.clear).not.toHaveBeenCalled();
      expect(authServiceSpy.clearAuth).not.toHaveBeenCalled();
      expect(routerSpy.navigate).not.toHaveBeenCalled();
    });
  });
});
