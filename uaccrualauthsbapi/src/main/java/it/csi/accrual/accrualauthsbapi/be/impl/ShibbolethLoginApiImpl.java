/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import it.csi.accrual.accrualauthsbapi.be.ShibbolethLoginApi;
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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShibbolethLoginApiImpl implements ShibbolethLoginApi {

	@Inject
	private AuthService authService;
	@Inject
	private ShibbolethValidator shibbolethValidator;
	@Inject
	private TokenService tokenService;

	@Override
	public Response shibbolethLogin(
			String codiceEnte,
			String shibIrideIdentitaDigitale,
			Integer anno,
			String fase,
			@Context SecurityContext securityContext,
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest httpRequest) {

		log.info("[Shibboleth-SSO] ===== INIZIO LOGIN SSO =====");
		log.info("[Shibboleth-SSO] Richiesta ricevuta: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
		log.info("[Shibboleth-SSO] Parametri: codiceEnte={}, anno={}, fase={}", codiceEnte, anno, fase);
		log.info("[Shibboleth-SSO] Header remoti: {}", httpRequest.getRemoteAddr());
		log.info("[Shibboleth-SSO] Header Shib-Iride-IdentitaDigitale presente: {}", shibIrideIdentitaDigitale != null && !shibIrideIdentitaDigitale.isBlank());
		
		if (shibIrideIdentitaDigitale != null) {
			log.info("[Shibboleth-SSO] Lunghezza header Shib: {} caratteri", shibIrideIdentitaDigitale.length());
			log.debug("[Shibboleth-SSO] Header Shib (primi 100 char): {}", 
				shibIrideIdentitaDigitale.length() > 100 ? shibIrideIdentitaDigitale.substring(0, 100) + "..." : shibIrideIdentitaDigitale);
		}

		if (shibIrideIdentitaDigitale == null || shibIrideIdentitaDigitale.isBlank()) {
			log.warn("[Shibboleth-SSO] ERRORE: Header Shib-Iride-IdentitaDigitale mancante o vuoto");
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity("Header Shib-Iride-IdentitaDigitale mancante").build();
		}

		try {
			log.info("[Shibboleth-SSO] Estrazione dati da header Shibboleth...");
			var shibData = shibbolethValidator.extractData(shibIrideIdentitaDigitale);
			String cf = shibData.getCodFiscale().toUpperCase();
			log.info("[Shibboleth-SSO] CF estratto dall'identità Shibboleth: {}", cf);
			log.info("[Shibboleth-SSO] Nome: {}, Cognome: {}", shibData.getNome(), shibData.getCognome());

			log.info("[Shibboleth-SSO] Ricerca utente con CF: {}, Ente: {}", cf, codiceEnte);
			AccrualAuthTUtente user = authService.findByUtenteCodiceUnivocoAndEnteCodiceUnivoco(cf, codiceEnte);
			log.info("[Shibboleth-SSO] Utente trovato: ID={}, codice={}, tipo autenticazione={}", 
				user.getUtenteId(), user.getUtenteCodiceUnivoco(), 
				user.getAutenticazioneTipo().getAutenticazioneTipoCodice());

			if (!AutenticazioneTipoEnum.SIAC_SHIB.getCodice().equals(
					user.getAutenticazioneTipo().getAutenticazioneTipoCodice())) {
				log.warn("[Shibboleth-SSO] ERRORE: Utente non ha accesso a modalità SIAC_SHIB. Tipo attuale: {}", 
					user.getAutenticazioneTipo().getAutenticazioneTipoCodice());
				throw new BadRequestException("Utente non ha accesso a questa modalita' di autenticazione");
			}

			log.info("[Shibboleth-SSO] Tipo autenticazione valido. Ricerca/Generazione JWT...");
			String token = tokenService.generateOrReplaceToken(
					user,
					AutenticazioneTipoEnum.SIAC_SHIB.getDescrizione(),
					anno,
					fase);
			
			log.info("[Shibboleth-SSO] JWT ottenuto: {}", token != null ? "SI (lunghezza " + token.length() + ")" : "NO");

			log.info("[Shibboleth-SSO] Login Shibboleth completato con successo per utente: {}", cf);

			InlineResponse200 response = new InlineResponse200();
			response.setToken(token);
			log.info("[Shibboleth-SSO] Invio risposta al client con token");
			log.info("[Shibboleth-SSO] ===== FINE LOGIN SSO (SUCCESS) =====");
			return Response.status(Response.Status.OK).entity(response).build();

		} catch (NotFoundException e) {
			log.warn("[Shibboleth-SSO] ERRORE: Utente non trovato nel database: {}", e.getMessage());
			log.info("[Shibboleth-SSO] ===== FINE LOGIN SSO (NOT_FOUND) =====");
			return Response.status(Response.Status.NOT_FOUND).entity(e).build();
		} catch (BadRequestException e) {
			log.warn("[Shibboleth-SSO] ERRORE: Bad request: {}", e.getMessage());
			log.info("[Shibboleth-SSO] ===== FINE LOGIN SSO (BAD_REQUEST) =====");
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		} catch (Exception e) {
			log.error("[Shibboleth-SSO] ERRORE GENERICO: {}", e.getMessage(), e);
			log.info("[Shibboleth-SSO] ===== FINE LOGIN SSO (ERROR) =====");
			return Response.status(Response.Status.UNAUTHORIZED).entity(e).build();
		}
	}
}
