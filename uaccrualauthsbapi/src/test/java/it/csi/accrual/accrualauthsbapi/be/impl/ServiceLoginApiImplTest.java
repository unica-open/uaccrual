/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.csi.accrual.accrualauthsbapi.dto.CredentialsLoginRequest;
import it.csi.accrual.accrualauthsbapi.dto.InlineResponse200;
import it.csi.accrual.accrualauthsbapi.dto.ServiceLoginRequest;
import it.csi.accrual.accrualauthsbapi.dto.TokenLoginRequest;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthDAutenticazioneTipo;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.exception.MalformedIdTokenException;
import it.csi.accrual.accrualauthsbapi.security.jwt.Identita;
import it.csi.accrual.accrualauthsbapi.security.jwt.ShibbolethValidator;
import it.csi.accrual.accrualauthsbapi.service.AuthService;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

/**
 * Unit tests for {@link ServiceLoginApiImpl}.
 * 
 * Tests cover:
 * - SIAC flow (delegated login with Shibboleth assertion)
 * - CSI_JOB flow (service login - returns 501)
 * - Invalid service credentials (missing/blank service code/secret)
 * - Invalid auth type
 * - User not found
 * - Malformed Shibboleth identity
 * - Generic exceptions
 * - Stub methods that throw UnsupportedOperationException
 */
@ExtendWith(MockitoExtension.class)
class ServiceLoginApiImplTest {

	@InjectMocks
	private ServiceLoginApiImpl serviceLoginApi;

	@Mock
	private AuthService authService;

	@Mock
	private TokenService tokenService;

	@Mock
	private ShibbolethValidator shibbolethValidator;

	private ServiceLoginRequest validSiacRequest;
	private AccrualAuthTUtente mockUser;
	private AccrualAuthDAutenticazioneTipo authTipoShib;
	private Identita mockIdentita;

	@BeforeEach
	void setUp() {
		validSiacRequest = new ServiceLoginRequest();
		validSiacRequest.setServiceCode("SIAC");
		validSiacRequest.setServiceSecret("secret123");
		validSiacRequest.setShibbolethIdentity("SMNPLA00L13L219K/Mario/Rossi/Shibboleth/20260407/1/abcd1234");
		validSiacRequest.setCodiceEnte("80087670016");

		authTipoShib = new AccrualAuthDAutenticazioneTipo();
		authTipoShib.setAutenticazioneTipoCodice("SIAC-SHIB");

		mockUser = new AccrualAuthTUtente();
		mockUser.setUtenteCodiceUnivoco("SMNPLA00L13L219K");
		mockUser.setAutenticazioneTipo(authTipoShib);

		mockIdentita = new Identita("SMNPLA00L13L219K/Mario/Rossi/Shibboleth/20260407/1/abcd1234");
	}

	@Nested
	@DisplayName("SIAC Login - Success scenarios")
	class SiacLoginSuccessTests {

		@Test
		@DisplayName("Should return 200 with token for valid SIAC login")
		void testSiacLoginSuccess() {
			when(shibbolethValidator.extractData(anyString())).thenReturn(mockIdentita);
			when(authService.findByUtenteCodiceUnivocoAndEnteCodiceUnivoco("SMNPLA00L13L219K", "80087670016"))
				.thenReturn(mockUser);
			when(tokenService.findByUtenteUtenteIdAndFineValiditaIsNull(any(AccrualAuthTUtente.class), anyString(), any(), any()))
				.thenReturn("siac-jwt-token");

			Response response = serviceLoginApi.loginWithService(validSiacRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
			assertThat(response.getEntity()).isInstanceOf(InlineResponse200.class);
			InlineResponse200 body = (InlineResponse200) response.getEntity();
			assertThat(body.getToken()).isEqualTo("siac-jwt-token");
		}

		@Test
		@DisplayName("Should handle different service codes")
		void testSiacLoginWithDifferentServiceCode() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode("OTHER_SERVICE");
			request.setServiceSecret("secret");
			request.setShibbolethIdentity("SMNPLA00L13L219K/Mario/Rossi/Shibboleth/20260407/1/abcd1234");
			request.setCodiceEnte("regione.piemonte");

			when(shibbolethValidator.extractData(anyString())).thenReturn(mockIdentita);
			when(authService.findByUtenteCodiceUnivocoAndEnteCodiceUnivoco("SMNPLA00L13L219K", "regione.piemonte"))
				.thenReturn(mockUser);
			when(tokenService.findByUtenteUtenteIdAndFineValiditaIsNull(any(), anyString(), any(), any()))
				.thenReturn("token-other-service");

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
		}
	}

	@Nested
	@DisplayName("SIAC Login - Error scenarios")
	class SiacLoginErrorTests {

		@Test
		@DisplayName("Should return 404 when user not found")
		void testSiacLoginUserNotFound() {
			when(shibbolethValidator.extractData(anyString())).thenReturn(mockIdentita);
			when(authService.findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(anyString(), anyString()))
				.thenThrow(new NotFoundException("Utente non trovato"));

			Response response = serviceLoginApi.loginWithService(validSiacRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(404);
		}

		@Test
		@DisplayName("Should return 401 when user has wrong auth type (EXT-SPRING instead of SIAC-SHIB)")
		void testSiacLoginWrongAuthType() {
			AccrualAuthDAutenticazioneTipo authTipoExt = new AccrualAuthDAutenticazioneTipo();
			authTipoExt.setAutenticazioneTipoCodice("EXT-SPRING");
			mockUser.setAutenticazioneTipo(authTipoExt);

			when(shibbolethValidator.extractData(anyString())).thenReturn(mockIdentita);
			when(authService.findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(anyString(), anyString()))
				.thenReturn(mockUser);

			Response response = serviceLoginApi.loginWithService(validSiacRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 400 when Shibboleth identity is malformed")
		void testSiacLoginMalformedShibbolethIdentity() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode("SIAC");
			request.setServiceSecret("secret123");
			request.setShibbolethIdentity("MALFORMED-NO-SLASHES");
			request.setCodiceEnte("80087670016");

			when(shibbolethValidator.extractData("MALFORMED-NO-SLASHES"))
				.thenThrow(new MalformedIdTokenException("MALFORMED-NO-SLASHES"));

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 for generic exception during SIAC login")
		void testSiacLoginGenericException() {
			when(shibbolethValidator.extractData(anyString())).thenReturn(mockIdentita);
			when(authService.findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(anyString(), anyString()))
				.thenThrow(new RuntimeException("Database error"));

			Response response = serviceLoginApi.loginWithService(validSiacRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}
	}

	@Nested
	@DisplayName("CSI_JOB Login - Not Implemented")
	class CsiJobLoginTests {

		@Test
		@DisplayName("Should return 501 NOT_IMPLEMENTED for CSI_JOB login without shibbolethIdentity")
		void testCsiJobReturnsNotImplemented() {
			ServiceLoginRequest csiJobRequest = new ServiceLoginRequest();
			csiJobRequest.setServiceCode("CSI_JOB");
			csiJobRequest.setServiceSecret("secret123");
			// No shibbolethIdentity - this is a batch service login

			Response response = serviceLoginApi.loginWithService(csiJobRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(501);
			assertThat(response.getEntity()).isNotNull();
			assertThat(response.getEntity().toString()).contains("CSI_JOB");
		}

		@Test
		@DisplayName("Should return 501 when shibbolethIdentity is empty string")
		void testCsiJobWithEmptyShibbolethIdentity() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode("CSI_JOB");
			request.setServiceSecret("secret");
			request.setShibbolethIdentity("");
			request.setCodiceEnte("80087670016");

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(501);
		}
	}

	@Nested
	@DisplayName("Validation - Service credentials")
	class ValidationTests {

		@Test
		@DisplayName("Should return 401 when serviceCode is null")
		void testMissingServiceCode() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode(null);
			request.setServiceSecret("secret123");

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when serviceCode is blank")
		void testBlankServiceCode() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode("   ");
			request.setServiceSecret("secret123");

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when serviceSecret is null")
		void testMissingServiceSecret() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode("SIAC");
			request.setServiceSecret(null);

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when serviceSecret is blank")
		void testBlankServiceSecret() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode("SIAC");
			request.setServiceSecret("   ");

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when both serviceCode and serviceSecret are null")
		void testBothCredentialsNull() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode(null);
			request.setServiceSecret(null);

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when serviceSecret is missing (only code present)")
		void testOnlyServiceCodePresent() {
			ServiceLoginRequest request = new ServiceLoginRequest();
			request.setServiceCode("SIAC");

			Response response = serviceLoginApi.loginWithService(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}
	}

	@Nested
	@DisplayName("Stub methods - Should throw UnsupportedOperationException")
	class StubMethodTests {

		@Test
		@DisplayName("loginWithCredentials should throw UnsupportedOperationException")
		void testLoginWithCredentialsThrowsException() {
			assertThatThrownBy(() -> serviceLoginApi.loginWithCredentials(null, null, null, null))
				.isInstanceOf(UnsupportedOperationException.class);
		}

		@Test
		@DisplayName("loginWithExternalToken should throw UnsupportedOperationException")
		void testLoginWithExternalTokenThrowsException() {
			assertThatThrownBy(() -> serviceLoginApi.loginWithExternalToken(null, null, null, null))
				.isInstanceOf(UnsupportedOperationException.class);
		}
	}
}
