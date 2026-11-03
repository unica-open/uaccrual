/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import it.csi.accrual.accrualauthsbapi.dto.CredentialsLoginRequest;
import it.csi.accrual.accrualauthsbapi.dto.ServiceLoginRequest;
import it.csi.accrual.accrualauthsbapi.dto.TokenLoginRequest;
import jakarta.ws.rs.core.Response;

/**
 * Unit tests for {@link TokenLoginApiImpl}.
 * 
 * Tests cover:
 * - NOT_IMPLEMENTED response for loginWithExternalToken
 * - Various token formats (valid, empty, null)
 * - Stub methods that throw UnsupportedOperationException
 */
class TokenLoginApiImplTest {

	private TokenLoginApiImpl tokenLoginApi;

	@BeforeEach
	void setUp() {
		tokenLoginApi = new TokenLoginApiImpl();
	}

	@Nested
	@DisplayName("loginWithExternalToken - Stub implementation")
	class LoginWithExternalTokenTests {

		@Test
		@DisplayName("Should return 501 NOT_IMPLEMENTED with valid token")
		void testLoginWithValidToken() {
			TokenLoginRequest request = new TokenLoginRequest();
			request.setToken("external-oidc-token-xyz");
			request.setCodiceEnte("80087670016");

			Response response = tokenLoginApi.loginWithExternalToken(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(501);
			assertThat(response.getEntity()).isNotNull();
			assertThat(response.getEntity().toString()).contains("EXT_TOKEN");
		}

		@Test
		@DisplayName("Should return 501 NOT_IMPLEMENTED with empty token")
		void testLoginWithEmptyToken() {
			TokenLoginRequest request = new TokenLoginRequest();
			request.setToken("");
			request.setCodiceEnte("80087670016");

			Response response = tokenLoginApi.loginWithExternalToken(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(501);
			assertThat(response.getEntity()).isNotNull();
		}

		@Test
		@DisplayName("Should return 501 NOT_IMPLEMENTED with null token")
		void testLoginWithNullToken() {
			TokenLoginRequest request = new TokenLoginRequest();
			request.setToken(null);
			request.setCodiceEnte("80087670016");

			Response response = tokenLoginApi.loginWithExternalToken(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(501);
			assertThat(response.getEntity()).isNotNull();
		}

		@Test
		@DisplayName("Should return 501 NOT_IMPLEMENTED without ente code")
		void testLoginWithoutEnteCode() {
			TokenLoginRequest request = new TokenLoginRequest();
			request.setToken("some-token");
			request.setCodiceEnte(null);

			Response response = tokenLoginApi.loginWithExternalToken(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(501);
		}
	}

	@Nested
	@DisplayName("Stub methods - Should throw UnsupportedOperationException")
	class StubMethodTests {

		@Test
		@DisplayName("loginWithCredentials should throw UnsupportedOperationException")
		void testLoginWithCredentialsThrowsException() {
			assertThatThrownBy(() -> tokenLoginApi.loginWithCredentials(null, null, null, null))
				.isInstanceOf(UnsupportedOperationException.class);
		}

		@Test
		@DisplayName("loginWithService should throw UnsupportedOperationException")
		void testLoginWithServiceThrowsException() {
			assertThatThrownBy(() -> tokenLoginApi.loginWithService(null, null, null, null))
				.isInstanceOf(UnsupportedOperationException.class);
		}
	}
}
