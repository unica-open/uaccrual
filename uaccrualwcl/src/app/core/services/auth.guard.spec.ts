/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { AuthGuard } from './auth.guard';
import { AuthenticationService } from './authentication.service';
import { AuthStorageService } from './auth-storage.service';
import { JwtHelper } from '../utils/jwt.helper';
import { AuthContextService } from './auth-context.service';
import { of } from 'rxjs';

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let authStorageSpy: jasmine.SpyObj<AuthStorageService>;
  let jwtHelperSpy: jasmine.SpyObj<JwtHelper>;
  let authContextSpy: jasmine.SpyObj<AuthContextService>;

  beforeEach(() => {
    const authServiceMock = jasmine.createSpyObj('AuthenticationService', ['getToken', 'login', 'isAuthenticated', 'clearAuth']);
    const routerMock = jasmine.createSpyObj('Router', ['navigate', 'createUrlTree', 'navigateByUrl'], { url: '/home' });
    const authStorageMock = jasmine.createSpyObj('AuthStorageService', ['getToken', 'clear']);
    const jwtHelperMock = jasmine.createSpyObj('JwtHelper', ['isTokenExpired', 'getCodiceEnte', 'decodeToken']);
    const authContextMock = jasmine.createSpyObj('AuthContextService', [
      'setContext', 'getContext', 'clearContext', 'loadFromStorage',
      'getCurrentEnte', 'getAnnoBilancio', 'getFaseBilancio'
    ]);

    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthenticationService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: AuthStorageService, useValue: authStorageMock },
        { provide: JwtHelper, useValue: jwtHelperMock },
        { provide: AuthContextService, useValue: authContextMock }
      ]
    });

    guard = TestBed.inject(AuthGuard);
    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    authStorageSpy = TestBed.inject(AuthStorageService) as jasmine.SpyObj<AuthStorageService>;
    jwtHelperSpy = TestBed.inject(JwtHelper) as jasmine.SpyObj<JwtHelper>;
    authContextSpy = TestBed.inject(AuthContextService) as jasmine.SpyObj<AuthContextService>;
    
    // Configura i valori di ritorno default per i metodi del router
    routerSpy.createUrlTree.and.returnValue({} as UrlTree);
    routerSpy.navigateByUrl.and.returnValue(Promise.resolve(true));
    routerSpy.navigate.and.returnValue(Promise.resolve(true));
  });

  describe('canActivate()', () => {
    describe('Caso 1: Token esterno presente', () => {
      it('dovrebbe saltare login se stesso utente/ente (caching)', (done) => {
        const route = { queryParams: { token: 'external-token', provider: 'shibboleth' } } as any;
        authServiceSpy.getToken.and.returnValue('existing-token');
        jwtHelperSpy.isTokenExpired.and.returnValue(false);
        jwtHelperSpy.getCodiceEnte.withArgs('existing-token').and.returnValue('ente-1');
        jwtHelperSpy.getCodiceEnte.withArgs('external-token').and.returnValue('ente-1');
        jwtHelperSpy.decodeToken.and.returnValue({ sub: 'user1' });

        guard.canActivate(route).subscribe(result => {
          expect(result).toBeTrue();
          expect(authServiceSpy.login).not.toHaveBeenCalled();
          done();
        });
      });

      it('dovrebbe fare login se ente diverso', (done) => {
        const route = { queryParams: { token: 'external-token', provider: 'shibboleth' } } as any;
        authServiceSpy.getToken.and.returnValue('existing-token');
        jwtHelperSpy.isTokenExpired.and.returnValue(false);
        jwtHelperSpy.getCodiceEnte.withArgs('existing-token').and.returnValue('ente-1');
        jwtHelperSpy.getCodiceEnte.withArgs('external-token').and.returnValue('ente-2');
        authServiceSpy.login.and.returnValue(of({ token: 'new-token' } as any));

        guard.canActivate(route).subscribe(result => {
          expect(result).toBeTrue();
          expect(authServiceSpy.login).toHaveBeenCalled();
          done();
        });
      });
    });

    describe('Caso 2: Nessun token esterno, utente autenticato', () => {
      it('dovrebbe procedere se autenticato', (done) => {
        const route = { queryParams: {} } as ActivatedRouteSnapshot;
        authServiceSpy.getToken.and.returnValue('valid-token');
        jwtHelperSpy.isTokenExpired.and.returnValue(false);
        authServiceSpy.isAuthenticated.and.returnValue(true);

        guard.canActivate(route).subscribe(result => {
          expect(result).toBeTrue();
          done();
        });
      });
    });

    describe('Caso 3: Token scaduto o assente', () => {
      it('dovrebbe redirect a /login se token scaduto', (done) => {
        const route = { queryParams: {} } as ActivatedRouteSnapshot;
        authServiceSpy.isAuthenticated.and.returnValue(false);
        authStorageSpy.getToken.and.returnValue('expired-token');

        guard.canActivate(route).subscribe(() => {
          expect(authStorageSpy.clear).toHaveBeenCalled();
          expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/login'], jasmine.objectContaining({
            queryParams: { expired: 'true' }
          }));
          done();
        });
      });

      it('dovrebbe redirect a /login se nessun token', (done) => {
        const route = { queryParams: {} } as ActivatedRouteSnapshot;
        authServiceSpy.isAuthenticated.and.returnValue(false);
        authStorageSpy.getToken.and.returnValue(null);

        guard.canActivate(route).subscribe(() => {
          expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/login'], jasmine.objectContaining({
            queryParams: { returnUrl: '/home' }
          }));
          done();
        });
      });

      it('dovrebbe includere returnUrl nel redirect', (done) => {
        const route = { queryParams: {}, routeConfig: { path: 'pdc' } } as any;
        authServiceSpy.isAuthenticated.and.returnValue(false);
        authStorageSpy.getToken.and.returnValue(null);

        guard.canActivate(route).subscribe(() => {
          expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/login'], jasmine.objectContaining({
            queryParams: { returnUrl: 'pdc' }
          }));
          done();
        });
      });

      it('dovrebbe non aggiungere returnUrl se è la home', (done) => {
        const route = { queryParams: {}, routeConfig: { path: 'home' } } as any;
        authServiceSpy.isAuthenticated.and.returnValue(false);
        authStorageSpy.getToken.and.returnValue(null);

        guard.canActivate(route).subscribe(() => {
          expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/login']);
          done();
        });
      });
    });
  });
});
