/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTToken;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

/**
 * Unit tests for {@link LogoutApiImpl}.
 * 
 * Tests cover:
 * - Successful logout with valid token
 * - Token not found scenarios
 * - Missing Authorization header
 * - Invalid Authorization header formats
 * - Empty token after "Bearer " prefix
 * - Various Bearer token formats
 */
@ExtendWith(MockitoExtension.class)
class LogoutApiImplTest {

	@InjectMocks
	private LogoutApiImpl logoutApi;

	@Mock
	private TokenService tokenService;

	@Mock
	private HttpServletRequest httpRequest;

	private AccrualAuthTToken mockToken;

	@BeforeEach
	void setUp() {
		mockToken = new AccrualAuthTToken();
	}

	@Nested
	@DisplayName("logout - Success scenarios")
	class LogoutSuccessTests {

		@Test
		@DisplayName("Should return 200 when token is successfully deleted")
		void testLogoutSuccess() {
			when(httpRequest.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token");
			when(tokenService.deleteToken("valid-jwt-token")).thenReturn(Optional.of(mockToken));

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(200);
			assertThat(response.getEntity()).isEqualTo("Logout effettuato con successo");
			verify(tokenService).deleteToken("valid-jwt-token");
		}

		@Test
		@DisplayName("Should handle token with complex JWT format")
		void testLogoutWithComplexJwtToken() {
			String complexToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
			when(httpRequest.getHeader("Authorization")).thenReturn("Bearer " + complexToken);
			when(tokenService.deleteToken(complexToken)).thenReturn(Optional.of(mockToken));

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(200);
		}
	}

	@Nested
	@DisplayName("logout - Error scenarios")
	class LogoutErrorTests {

		@Test
		@DisplayName("Should return 401 when token is not found in database")
		void testLogoutTokenNotFound() {
			when(httpRequest.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token");
			when(tokenService.deleteToken("valid-jwt-token")).thenReturn(Optional.empty());

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity()).isEqualTo("Logout non effettuato");
		}

		@Test
		@DisplayName("Should return 401 when Authorization header is null")
		void testLogoutMissingAuthHeader() {
			when(httpRequest.getHeader("Authorization")).thenReturn(null);

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity()).isEqualTo("Authorization mancante o non valido");
		}

		@Test
		@DisplayName("Should return 401 when Authorization header is empty string")
		void testLogoutEmptyAuthHeader() {
			when(httpRequest.getHeader("Authorization")).thenReturn("");

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity()).isEqualTo("Authorization mancante o non valido");
		}

		@Test
		@DisplayName("Should return 401 when Authorization header doesn't start with 'Bearer '")
		void testLogoutInvalidAuthHeader() {
			when(httpRequest.getHeader("Authorization")).thenReturn("InvalidHeader");

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity()).isEqualTo("Authorization mancante o non valido");
		}

		@Test
		@DisplayName("Should return 401 when Authorization header is 'Basic' instead of 'Bearer'")
		void testLogoutBasicAuthHeader() {
			when(httpRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity()).isEqualTo("Authorization mancante o non valido");
		}

		@Test
		@DisplayName("Should return 401 when token is empty after 'Bearer ' prefix")
		void testLogoutEmptyTokenAfterBearer() {
			when(httpRequest.getHeader("Authorization")).thenReturn("Bearer ");

			Response response = logoutApi.logout(null, null, httpRequest);

			// Empty string token will be passed to deleteToken, which returns empty Optional
			when(tokenService.deleteToken("")).thenReturn(Optional.empty());

			// Re-setup the mock for this specific case
			when(httpRequest.getHeader("Authorization")).thenReturn("Bearer ");
			response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 when Authorization header is just 'Bearer' without space")
		void testLogoutBearerWithoutSpace() {
			when(httpRequest.getHeader("Authorization")).thenReturn("Bearer");

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("Should return 401 for expired token")
		void testLogoutExpiredToken() {
			when(httpRequest.getHeader("Authorization")).thenReturn("Bearer expired-token");
			when(tokenService.deleteToken("expired-token")).thenReturn(Optional.empty());

			Response response = logoutApi.logout(null, null, httpRequest);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity()).isEqualTo("Logout non effettuato");
		}
	}
}
