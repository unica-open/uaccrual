/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.csi.accrual.accrualauthsbapi.dto.ConfirmPasswordResetRequest;
import it.csi.accrual.accrualauthsbapi.dto.ResetPasswordRequest;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthDAutenticazioneTipo;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTResetPassword;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.exception.MalformedIdTokenException;
import it.csi.accrual.accrualauthsbapi.service.AuthService;
import it.csi.accrual.accrualauthsbapi.service.EmailService;
import it.csi.accrual.accrualauthsbapi.service.ResetPasswordService;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Unit tests for {@link PasswordResetApiImpl}.
 * 
 * Tests cover:
 * - generateTemporalyTokenForResetPassword:
 *   - Successful reset token generation
 *   - User not found
 *   - Wrong auth type
 *   - Mail send failure
 *   - Malformed token exception
 * 
 * - confirmPasswordReset:
 *   - Successful password reset
 *   - Expired token
 *   - Token already used (gestDataDel not null)
 *   - Token not found
 *   - Codice fiscale mismatch
 *   - Generic exceptions
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetApiImplTest {

	@InjectMocks
	private PasswordResetApiImpl passwordResetApi;

	@Mock
	private AuthService authService;

	@Mock
	private EmailService emailService;

	@Mock
	private ResetPasswordService resetPasswordService;

	private AccrualAuthTUtente mockUser;
	private AccrualAuthDAutenticazioneTipo authTipoExtSpring;

	@BeforeEach
	void setUp() {
		authTipoExtSpring = new AccrualAuthDAutenticazioneTipo();
		authTipoExtSpring.setAutenticazioneTipoCodice("EXT-SPRING");

		mockUser = new AccrualAuthTUtente();
		mockUser.setUtenteCodiceUnivoco("SMNPLA00L13L219K");
		mockUser.setAutenticazioneTipo(authTipoExtSpring);
	}

	@Nested
	@DisplayName("generateTemporalyTokenForResetPassword - Success scenarios")
	class GenerateTokenSuccessTests {

		@Test
		@DisplayName("Should return 200 when reset token is generated and email sent successfully")
		void testGenerateResetTokenSuccess() {
			ResetPasswordRequest request = new ResetPasswordRequest();
			request.setCodiceFiscale("SMNPLA00L13L219K");

			when(authService.findByUtenteCodiceUnivoco("SMNPLA00L13L219K")).thenReturn(mockUser);
			doNothing().when(emailService).sendResetPasswordEmail(mockUser);

			Response response = passwordResetApi.generateTemporalyTokenForResetPassword(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
			assertThat(response.getEntity().toString()).contains("Token temporaneo generato");
			verify(emailService).sendResetPasswordEmail(mockUser);
		}

		@Test
		@DisplayName("Should handle lowercase CF and convert to uppercase")
		void testGenerateResetTokenWithLowercaseCF() {
			ResetPasswordRequest request = new ResetPasswordRequest();
			request.setCodiceFiscale("smnpla00l13l219k");

			when(authService.findByUtenteCodiceUnivoco("SMNPLA00L13L219K")).thenReturn(mockUser);
			doNothing().when(emailService).sendResetPasswordEmail(mockUser);

			Response response = passwordResetApi.generateTemporalyTokenForResetPassword(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
		}
	}

	@Nested
	@DisplayName("generateTemporalyTokenForResetPassword - Error scenarios")
	class GenerateTokenErrorTests {

		@Test
		@DisplayName("Should return 404 when user is not found")
		void testGenerateResetTokenUserNotFound() {
			ResetPasswordRequest request = new ResetPasswordRequest();
			request.setCodiceFiscale("SMNPLA00L13L219K");

			when(authService.findByUtenteCodiceUnivoco(anyString()))
				.thenThrow(new NotFoundException("Utente non trovato"));

			Response response = passwordResetApi.generateTemporalyTokenForResetPassword(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(404);
		}

		@Test
		@DisplayName("Should return 404 when user has wrong auth type (SIAC-SHIB instead of EXT-SPRING)")
		void testGenerateResetTokenWrongAuthType() {
			ResetPasswordRequest request = new ResetPasswordRequest();
			request.setCodiceFiscale("SMNPLA00L13L219K");

			AccrualAuthDAutenticazioneTipo authTipoShib = new AccrualAuthDAutenticazioneTipo();
			authTipoShib.setAutenticazioneTipoCodice("SIAC-SHIB");
			mockUser.setAutenticazioneTipo(authTipoShib);

			when(authService.findByUtenteCodiceUnivoco(anyString())).thenReturn(mockUser);

			Response response = passwordResetApi.generateTemporalyTokenForResetPassword(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(404);
		}

		@Test
		@DisplayName("Should return 400 when email sending fails")
		void testGenerateResetTokenMailSendError() {
			ResetPasswordRequest request = new ResetPasswordRequest();
			request.setCodiceFiscale("SMNPLA00L13L219K");

			when(authService.findByUtenteCodiceUnivoco(anyString())).thenReturn(mockUser);
			doThrow(new MailSendException("SMTP connection failed")).when(emailService).sendResetPasswordEmail(any());

			Response response = passwordResetApi.generateTemporalyTokenForResetPassword(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(400);
		}

		@Test
		@DisplayName("Should return 401 when MalformedIdTokenException is thrown")
		void testGenerateResetTokenMalformedToken() {
			ResetPasswordRequest request = new ResetPasswordRequest();
			request.setCodiceFiscale("SMNPLA00L13L219K");

			when(authService.findByUtenteCodiceUnivoco(anyString()))
				.thenThrow(new MalformedIdTokenException("Invalid token format"));

			Response response = passwordResetApi.generateTemporalyTokenForResetPassword(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}
	}

	@Nested
	@DisplayName("confirmPasswordReset - Success scenarios")
	class ConfirmResetSuccessTests {

		@Test
		@DisplayName("Should return 200 when password is reset successfully")
		void testConfirmResetSuccess() {
			ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
			request.setToken("reset-token-xyz");
			request.setCodiceFiscale("SMNPLA00L13L219K");
			request.setPassword("newPassword123");
			request.setConfirmPassword("newPassword123");

			AccrualAuthTResetPassword resetPassword = new AccrualAuthTResetPassword();
			resetPassword.setUtente(mockUser);
			resetPassword.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));
			resetPassword.setGestDataDel(null);

			when(resetPasswordService.findByResetPasswordTokenValue("reset-token-xyz")).thenReturn(resetPassword);
			when(resetPasswordService.confirmResetPassword(any(AccrualAuthTUtente.class), eq("newPassword123")))
				.thenReturn(mockUser);
			when(resetPasswordService.updateResetPassword(any(AccrualAuthTResetPassword.class))).thenReturn(resetPassword);

			Response response = passwordResetApi.confirmPasswordReset(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
			assertThat(response.getEntity().toString()).contains("Password cambiata");
			verify(resetPasswordService).confirmResetPassword(mockUser, "newPassword123");
			verify(resetPasswordService).updateResetPassword(resetPassword);
		}

		@Test
		@DisplayName("Should handle token expiring in the future (just before now)")
		void testConfirmResetWithTokenExpiringSoon() {
			ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
			request.setToken("reset-token");
			request.setCodiceFiscale("SMNPLA00L13L219K");
			request.setPassword("newPassword123");
			request.setConfirmPassword("newPassword123");

			AccrualAuthTResetPassword resetPassword = new AccrualAuthTResetPassword();
			resetPassword.setUtente(mockUser);
			resetPassword.setResetPasswordExpiresAt(LocalDateTime.now().plusSeconds(1));
			resetPassword.setGestDataDel(null);

			when(resetPasswordService.findByResetPasswordTokenValue("reset-token")).thenReturn(resetPassword);
			when(resetPasswordService.confirmResetPassword(any(), anyString())).thenReturn(mockUser);
			when(resetPasswordService.updateResetPassword(any())).thenReturn(resetPassword);

			Response response = passwordResetApi.confirmPasswordReset(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(200);
		}
	}

	@Nested
	@DisplayName("confirmPasswordReset - Error scenarios")
	class ConfirmResetErrorTests {

		@Test
		@DisplayName("Should return 401 when token is expired")
		void testConfirmResetExpiredToken() {
			ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
			request.setToken("reset-token");
			request.setCodiceFiscale("SMNPLA00L13L219K");
			request.setPassword("newPassword123");
			request.setConfirmPassword("newPassword123");

			AccrualAuthTResetPassword resetPassword = new AccrualAuthTResetPassword();
			resetPassword.setUtente(mockUser);
			resetPassword.setResetPasswordExpiresAt(LocalDateTime.now().minusHours(1));
			resetPassword.setGestDataDel(null);

			when(resetPasswordService.findByResetPasswordTokenValue("reset-token")).thenReturn(resetPassword);

			Response response = passwordResetApi.confirmPasswordReset(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity().toString()).contains("scaduto");
		}

		@Test
		@DisplayName("Should return 401 when token is already used (gestDataDel not null)")
		void testConfirmResetTokenAlreadyUsed() {
			ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
			request.setToken("reset-token");
			request.setCodiceFiscale("SMNPLA00L13L219K");
			request.setPassword("newPassword123");
			request.setConfirmPassword("newPassword123");

			AccrualAuthTResetPassword resetPassword = new AccrualAuthTResetPassword();
			resetPassword.setUtente(mockUser);
			resetPassword.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));
			resetPassword.setGestDataDel(LocalDateTime.now().minusHours(1)); // Already used

			when(resetPasswordService.findByResetPasswordTokenValue("reset-token")).thenReturn(resetPassword);

			Response response = passwordResetApi.confirmPasswordReset(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity().toString()).contains("scaduto");
		}

		@Test
		@DisplayName("Should return 404 when token is not found")
		void testConfirmResetTokenNotFound() {
			ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
			request.setToken("reset-token");
			request.setCodiceFiscale("SMNPLA00L13L219K");
			request.setPassword("newPassword123");
			request.setConfirmPassword("newPassword123");

			when(resetPasswordService.findByResetPasswordTokenValue(anyString()))
				.thenThrow(new NotFoundException("Token non trovato"));

			Response response = passwordResetApi.confirmPasswordReset(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(404);
		}

		@Test
		@DisplayName("Should return 401 when codice fiscale doesn't match token user")
		void testConfirmResetCfMismatch() {
			ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
			request.setToken("reset-token");
			request.setCodiceFiscale("RSSMRA80A01H501Z"); // Different CF
			request.setPassword("newPassword123");
			request.setConfirmPassword("newPassword123");

			AccrualAuthTUtente differentUser = new AccrualAuthTUtente();
			differentUser.setUtenteCodiceUnivoco("SMNPLA00L13L219K");
			differentUser.setAutenticazioneTipo(authTipoExtSpring);

			AccrualAuthTResetPassword resetPassword = new AccrualAuthTResetPassword();
			resetPassword.setUtente(differentUser);
			resetPassword.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));
			resetPassword.setGestDataDel(null);

			when(resetPasswordService.findByResetPasswordTokenValue("reset-token")).thenReturn(resetPassword);

			Response response = passwordResetApi.confirmPasswordReset(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
			assertThat(response.getEntity().toString()).contains("non corrisponde");
		}

		@Test
		@DisplayName("Should return 401 for BadCredentialsException")
		void testConfirmResetBadCredentials() {
			ConfirmPasswordResetRequest request = new ConfirmPasswordResetRequest();
			request.setToken("reset-token");
			request.setCodiceFiscale("SMNPLA00L13L219K");
			request.setPassword("newPassword123");
			request.setConfirmPassword("newPassword123");

			AccrualAuthTResetPassword resetPassword = new AccrualAuthTResetPassword();
			resetPassword.setUtente(mockUser);
			resetPassword.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1));
			resetPassword.setGestDataDel(null);

			when(resetPasswordService.findByResetPasswordTokenValue("reset-token")).thenReturn(resetPassword);
			when(resetPasswordService.confirmResetPassword(any(), anyString()))
				.thenThrow(new BadCredentialsException("Password validation failed"));

			Response response = passwordResetApi.confirmPasswordReset(request, null, null, null);

			assertThat(response.getStatus()).isEqualTo(401);
		}
	}
}
