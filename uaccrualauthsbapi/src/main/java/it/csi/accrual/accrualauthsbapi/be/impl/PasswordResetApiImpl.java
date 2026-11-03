/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import org.springframework.stereotype.Component;
import it.csi.accrual.accrualauthsbapi.be.ConfirmPasswordResetApi;
import it.csi.accrual.accrualauthsbapi.be.ResetPasswordApi;
import it.csi.accrual.accrualauthsbapi.dto.ConfirmPasswordResetRequest;
import it.csi.accrual.accrualauthsbapi.dto.ResetPasswordRequest;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTResetPassword;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.exception.MalformedIdTokenException;
import it.csi.accrual.accrualauthsbapi.service.AuthService;
import it.csi.accrual.accrualauthsbapi.service.EmailService;
import it.csi.accrual.accrualauthsbapi.service.ResetPasswordService;
import it.csi.accrual.accrualauthsbapi.util.AutenticazioneTipoEnum;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Gestisce reset password (genera token + conferma)
 */
@Component
@Slf4j
public class PasswordResetApiImpl implements ResetPasswordApi, ConfirmPasswordResetApi {

	@Inject
	private AuthService authService;
	@Inject
	private EmailService emailService;
	@Inject
	private ResetPasswordService resetPasswordService;

	@Override
	public Response generateTemporalyTokenForResetPassword(@Valid ResetPasswordRequest body,
			@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
		log.info("Richiesta reset password per utente: {}", body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null);
		try {
			AccrualAuthTUtente user = authService.findByUtenteCodiceUnivoco(body.getCodiceFiscale().toUpperCase());
			log.info("Utente trovato: {}", user.getUtenteCodiceUnivoco());

			if(!AutenticazioneTipoEnum.EXT_SPRING.getCodice().equals(user.getAutenticazioneTipo().getAutenticazioneTipoCodice())) {
				log.warn("Utente {} non ha accesso a questa modalita' di autenticazione", body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null);
				throw new BadCredentialsException("Utente non ha accesso a questa modalita' di autenticazione");
			}

			emailService.sendResetPasswordEmail(user);
			log.info("Email di reset password inviata con successo a: {}", body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null);
		} catch (NotFoundException e) {
			log.warn("Reset password - Utente non trovato: {}", body.getCodiceFiscale().toUpperCase());
			return Response.status(Response.Status.NOT_FOUND).entity(e).build();
		} catch (BadCredentialsException e) {
			log.warn("Reset password - Credenziali non valide: {}", e.getMessage());
			return Response.status(Response.Status.NOT_FOUND).entity(e).build();
		} catch (MalformedIdTokenException e) {
			log.warn("Reset password - Token malformato: {}", e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		} catch (MailSendException e) {
			log.error("Reset password - Errore invio email: {}", e.getMessage(), e);
			return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
		}

		return Response.status(Response.Status.OK).entity("Token temporaneo generato ed email inviata con successo").build();
	}

	@Override
	public Response confirmPasswordReset(@Valid ConfirmPasswordResetRequest body,
			@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {

		log.info("Richiesta conferma reset password per utente: {}", body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null);
		try {
			AccrualAuthTResetPassword accrualAuthTResetPassword = resetPasswordService.findByResetPasswordTokenValue(body.getToken());
			log.info("Token di reset trovato per utente: {}", accrualAuthTResetPassword.getUtente().getUtenteCodiceUnivoco());

			if (accrualAuthTResetPassword.getResetPasswordExpiresAt().isBefore(java.time.LocalDateTime.now()) || accrualAuthTResetPassword.getGestDataDel() != null) {
				log.warn("Token scaduto o già utilizzato per utente: {}", body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null);
				resetPasswordService.updateResetPassword(accrualAuthTResetPassword);
				throw new BadCredentialsException("Il token e' scaduto, si prega di rifare la procedura per il reset della password");
			}

			if(!accrualAuthTResetPassword.getUtente().getUtenteCodiceUnivoco().equals(body.getCodiceFiscale().toUpperCase())) {
				log.warn("Codice fiscale non corrisponde per token: {}", body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null);
				throw new BadCredentialsException("Il codice fiscale non corrisponde con l'utente che ha fatto richiesta di reset password");
			}

			resetPasswordService.confirmResetPassword(accrualAuthTResetPassword.getUtente(), body.getPassword());
			resetPasswordService.updateResetPassword(accrualAuthTResetPassword);
			log.info("Password cambiata con successo per utente: {}", body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null);
		} catch (NotFoundException e) {
			log.warn("Confirm reset password - Token non trovato: {}", body.getToken());
			return Response.status(Response.Status.NOT_FOUND).entity(e).build();
		} catch (BadCredentialsException e) {
			log.warn("Confirm reset password - Errore: {}", e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		}

		return Response.status(Response.Status.OK).entity("Password cambiata con successo, si prega di effettuare il login").build();
	}
}
