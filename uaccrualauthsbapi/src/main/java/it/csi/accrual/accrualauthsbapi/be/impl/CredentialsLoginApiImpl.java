/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import it.csi.accrual.accrualauthsbapi.be.LoginApi;
import it.csi.accrual.accrualauthsbapi.dto.CredentialsLoginRequest;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.service.AuthService;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import it.csi.accrual.accrualauthsbapi.util.AutenticazioneTipoEnum;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gestisce login con credenziali (CF + password) - EXT_SPRING
 */
@Component
@Slf4j
public class CredentialsLoginApiImpl implements LoginApi {

	@Inject
	private AuthService authService;
	@Inject
	private TokenService tokenService;

	@Override
	public Response loginWithCredentials(@Valid CredentialsLoginRequest body,
			@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {

		log.info("Tentativo di login con credenziali per utente: {}, ente: {}, anno: {}, fase: {}",
			body.getCodiceFiscale() != null ? body.getCodiceFiscale().toUpperCase() : null,
			body.getCodiceEnte(),
			body.getAnnoBilancio(),
			body.getFaseBilancio());

		try {
			if (body.getCodiceFiscale() == null) {
				log.error("ERRORE CRITICO: CodiceFiscale è NULL nel payload ricevuto!");
			}
			String cf = body.getCodiceFiscale().toUpperCase();
			AccrualAuthTUtente user = authService.login(cf, body.getCodiceEnte(), body.getPassword());
			if(!AutenticazioneTipoEnum.EXT_SPRING.getCodice().equals(user.getAutenticazioneTipo().getAutenticazioneTipoCodice())) {
				throw new BadRequestException("Utente non ha accesso a questa modalita' di autenticazione");
			}

			String token = tokenService.generateOrReplaceToken(user, AutenticazioneTipoEnum.EXT_SPRING.getDescrizione(),
				body.getAnnoBilancio(), body.getFaseBilancio());
			log.info("Login con credenziali effettuato con successo per utente: {}", user.getUtenteCodiceUnivoco());

			it.csi.accrual.accrualauthsbapi.dto.InlineResponse200 response = new it.csi.accrual.accrualauthsbapi.dto.InlineResponse200();
			response.setToken(token);
			return Response.status(Response.Status.OK).entity(response).build();
		} catch (NotFoundException e) {
			log.warn("Login fallito - Utente non trovato: {}", e.getMessage());
			return Response.status(Response.Status.NOT_FOUND).entity(e).build();
		} catch (BadRequestException e) {
			log.warn("Login fallito - Bad request: {}", e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		} catch (Exception e) {
			log.error("Login fallito - Errore generico: {}", e.getMessage(), e);
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		}
	}
}