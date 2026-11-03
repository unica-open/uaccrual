/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import it.csi.accrual.accrualauthsbapi.dto.CredentialsLoginRequest;
import it.csi.accrual.accrualauthsbapi.dto.InlineResponse200;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthDAutenticazioneTipo;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.service.AuthService;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

/**
 * Unit tests for {@link CredentialsLoginApiImpl}.
 *
 * Tests cover:
 * - Successful login with valid credentials
 * - User not found scenarios
 * - Invalid authentication type
 * - Generic exceptions
 * - Stub methods that throw UnsupportedOperationException
 */
@ExtendWith(MockitoExtension.class)
class CredentialsLoginApiImplTest {

	@InjectMocks
	private CredentialsLoginApiImpl credentialsLoginApi;

	@Mock
	private AuthService authService;

	@Mock
	private TokenService tokenService;

	private CredentialsLoginRequest validRequest;
	private AccrualAuthTUtente mockUser;
	private AccrualAuthDAutenticazioneTipo authTipoExtSpring;

	@BeforeEach
	void setUp() {
		validRequest = new CredentialsLoginRequest();
		validRequest.setCodiceFiscale("SMNPLA00L13L219K");
		validRequest.setPassword("password123");
		validRequest.setCodiceEnte("80087670016");

		authTipoExtSpring = new AccrualAuthDAutenticazioneTipo();
		authTipoExtSpring.setAutenticazioneTipoCodice("EXT-SPRING");

		mockUser = new AccrualAuthTUtente();
		mockUser.setUtenteCodiceUnivoco("SMNPLA00L13L219K");
		mockUser.setAutenticazioneTipo(authTipoExtSpring);
	}

	@Nested
	@DisplayName("loginWithCredentials - Success scenarios")
	class LoginSuccessTests {

		@Test
		@DisplayName("Should return 200 with token for valid credentials")
		void testLoginSuccess() throws Exception {
			when(authService.login("SMNPLA00L13L219K", "80087670016", "password123")).thenReturn(mockUser);
			when(tokenService.findByUtenteUtenteIdAndFineValiditaIsNull(any(AccrualAuthTUtente.class), anyString(), any(), any()))
				.thenReturn("mock-jwt-token");

			Response response = credentialsLoginApi.loginWithCredentials(validRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
			assertThat(response.getEntity()).isNotNull();
			assertThat(response.getEntity()).isInstanceOf(InlineResponse200.class);
			InlineResponse200 body = (InlineResponse200) response.getEntity();
			assertThat(body.getToken()).isEqualTo("mock-jwt-token");
		}

		@Test
		@DisplayName("Should handle lowercase CF and convert to uppercase")
		void testLoginWithLowercaseCF() throws Exception {
			CredentialsLoginRequest request = new CredentialsLoginRequest();
			request.setCodiceFiscale("smnpla00l13l219k");
			request.setPassword("password123");
			request.setCodiceEnte("80087670016");

			when(authService.login("SMNPLA00L13L219K", "80087670016", "password123")).thenReturn(mockUser);
			when(tokenService.findByUtenteUtenteIdAndFineValiditaIsNull(any(AccrualAuthTUtente.class), anyString(), any(), any()))
				.thenReturn("jwt-token-uppercase");

			Response response = credentialsLoginApi.loginWithCredentials(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
			InlineResponse200 body = (InlineResponse200) response.getEntity();
			assertThat(body.getToken()).isEqualTo("jwt-token-uppercase");
		}
	}

	@Nested
	@DisplayName("loginWithCredentials - Error scenarios")
	class LoginErrorTests {

		@Test
		@DisplayName("Should return 404 when user not found")
		void testLoginUserNotFound() throws Exception {
			when(authService.login(anyString(), anyString(), anyString())).thenAnswer(new Answer<AccrualAuthTUtente>() {
				@Override
				public AccrualAuthTUtente answer(InvocationOnMock invocation) throws Throwable {
					throw new NotFoundException("Utente non trovato");
				}
			});

			Response response = credentialsLoginApi.loginWithCredentials(validRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(404);
		}

		@Test
		@DisplayName("Should return 401 when user has wrong auth type (SIAC-SHIB instead of EXT-SPRING)")
		void testLoginWrongAuthType() throws Exception {
			AccrualAuthDAutenticazioneTipo authTipoShib = new AccrualAuthDAutenticazioneTipo();
			authTipoShib.setAutenticazioneTipoCodice("SIAC-SHIB");
			mockUser.setAutenticazioneTipo(authTipoShib);

			when(authService.login(anyString(), anyString(), anyString())).thenReturn(mockUser);

			Response response = credentialsLoginApi.loginWithCredentials(validRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when BadRequestException is thrown")
		void testLoginBadRequest() throws Exception {
			when(authService.login(anyString(), anyString(), anyString())).thenAnswer(new Answer<AccrualAuthTUtente>() {
				@Override
				public AccrualAuthTUtente answer(InvocationOnMock invocation) throws Throwable {
					throw new BadRequestException("Invalid credentials");
				}
			});

			Response response = credentialsLoginApi.loginWithCredentials(validRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 for generic exception")
		void testLoginGenericException() throws Exception {
			when(authService.login(anyString(), anyString(), anyString())).thenAnswer(new Answer<AccrualAuthTUtente>() {
				@Override
				public AccrualAuthTUtente answer(InvocationOnMock invocation) throws Throwable {
					throw new RuntimeException("Database connection failed");
				}
			});

			Response response = credentialsLoginApi.loginWithCredentials(validRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when password is incorrect")
		void testLoginIncorrectPassword() throws Exception {
			when(authService.login(anyString(), anyString(), anyString())).thenAnswer(new Answer<AccrualAuthTUtente>() {
				@Override
				public AccrualAuthTUtente answer(InvocationOnMock invocation) throws Throwable {
					throw new Exception("Invalid password");
				}
			});

			Response response = credentialsLoginApi.loginWithCredentials(validRequest, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}
	}

	@Nested
	@DisplayName("Stub methods - Should throw UnsupportedOperationException")
	class StubMethodTests {

		@Test
		@DisplayName("loginWithExternalToken should throw UnsupportedOperationException")
		void testLoginWithExternalTokenThrowsException() {
			assertThatThrownBy(() -> credentialsLoginApi.loginWithExternalToken(null, null, null, null))
				.isInstanceOf(UnsupportedOperationException.class);
		}

		@Test
		@DisplayName("loginWithService should throw UnsupportedOperationException")
		void testLoginWithServiceThrowsException() {
			assertThatThrownBy(() -> credentialsLoginApi.loginWithService(null, null, null, null))
				.isInstanceOf(UnsupportedOperationException.class);
		}
	}
}
