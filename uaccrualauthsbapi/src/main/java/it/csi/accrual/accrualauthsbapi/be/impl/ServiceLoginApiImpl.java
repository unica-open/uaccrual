/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import it.csi.accrual.accrualauthsbapi.be.ServiceLoginApi;
import it.csi.accrual.accrualauthsbapi.dto.ServiceLoginRequest;
import it.csi.accrual.accrualauthsbapi.dto.InlineResponse200;
import it.csi.accrual.accrualauthsbapi.entity.AccrualAuthTUtente;
import it.csi.accrual.accrualauthsbapi.security.jwt.ShibbolethValidator;
import it.csi.accrual.accrualauthsbapi.service.AuthService;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import it.csi.accrual.accrualauthsbapi.util.AutenticazioneTipoEnum;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
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
 * Endpoint per login delegato da servizi fidati (SIAC, CSI_JOB).
 */
@Component
@Slf4j
public class ServiceLoginApiImpl implements ServiceLoginApi {

	@Inject
	private AuthService authService;
	@Inject
	private TokenService tokenService;
	@Inject
	private ShibbolethValidator shibbolethValidator;

	@Override
	public Response loginWithService(ServiceLoginRequest body,
			@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {

		log.info(">>> Tentativo di login delegato da servizio: serviceCode={}, codiceEnte={}, annoBilancio={}",
			body != null ? body.getServiceCode() : "NULL",
			body != null ? body.getCodiceEnte() : "NULL",
			body != null ? body.getAnnoBilancio() : "NULL");

		if (body == null) {
			log.error(">>> Request body è NULL");
			return Response.status(Response.Status.BAD_REQUEST)
				.entity("Request body mancante").build();
		}

		log.info(">>> shibbolethIdentity={}", body.getShibbolethIdentity());
		log.info(">>> faseBilancio={}", body.getFaseBilancio());
		log.info(">>> serviceSecret presente: {}", body.getServiceSecret() != null && !body.getServiceSecret().isBlank());

		try {
			if (body.getServiceCode() == null || body.getServiceCode().isBlank()) {
				log.warn(">>> serviceCode mancante o vuoto");
				throw new BadRequestException("Service code è obbligatorio");
			}
			if (body.getServiceSecret() == null || body.getServiceSecret().isBlank()) {
				log.warn(">>> serviceSecret mancante o vuoto");
				throw new BadRequestException("Service secret è obbligatorio");
			}

			if (body.getShibbolethIdentity() != null && !body.getShibbolethIdentity().isEmpty()) {
				// CASO SIAC: Delegated login con assertion Shibboleth
				return handleDelegatedUserLogin(body);
			} else {
				// CASO CSI_JOB: Login di servizio (batch)
				return handleServiceLogin(body);
			}
		} catch (NotFoundException e) {
			log.warn("Login servizio fallito - Utente non trovato: {}", e.getMessage());
			return Response.status(Response.Status.NOT_FOUND).entity(e).build();
		} catch (BadRequestException e) {
			log.warn("Login servizio fallito - Bad request: {}", e.getMessage());
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		} catch (Exception e) {
			log.error("Login servizio fallito - Errore generico: {}", e.getMessage(), e);
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		}
	}

	/**
	 * Delegated login per SIAC: parse assertion Shibboleth → trova utente → genera JWT utente
	 */
	private Response handleDelegatedUserLogin(ServiceLoginRequest body) {
		log.info("Login delegato SIAC - parsing assertion Shibboleth");

		String cf = shibbolethValidator.extractData(body.getShibbolethIdentity()).getCodFiscale().toUpperCase();
		log.info("Login delegato SIAC - CF estratto: {}", cf);

		AccrualAuthTUtente user = authService.findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(cf, body.getCodiceEnte());

		if(!AutenticazioneTipoEnum.SIAC_SHIB.getCodice().equals(user.getAutenticazioneTipo().getAutenticazioneTipoCodice())) {
			throw new BadRequestException("Utente non ha accesso a questa modalita' di autenticazione");
		}

		String token = tokenService.generateOrReplaceToken(user,
			AutenticazioneTipoEnum.SIAC_SHIB.getDescrizione(), body.getAnnoBilancio(), body.getFaseBilancio());
		log.info("Login delegato SIAC effettuato con successo per utente: {}, anno: {}, fase: {}",
			user.getUtenteCodiceUnivoco(), body.getAnnoBilancio(), body.getFaseBilancio());

		InlineResponse200 response = new InlineResponse200();
		response.setToken(token);
		return Response.status(Response.Status.OK).entity(response).build();
	}

	/**
	 * Login di servizio per CSI_JOB (batch)
	 * TODO: Generare JWT di servizio (senza utente)
	 */
	private Response handleServiceLogin(ServiceLoginRequest body) {
		log.warn("Login servizio CSI_JOB non ancora implementato - TODO");

		// TODO: Creare JWT di servizio con claim service: true
		// Per ora ritorna errore 501 Not Implemented
		return Response.status(Response.Status.NOT_IMPLEMENTED)
			.entity("Login di servizio non ancora implementato - CSI_JOB")
			.build();
	}
}