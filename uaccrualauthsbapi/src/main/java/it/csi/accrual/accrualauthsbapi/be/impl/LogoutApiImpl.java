/*
 * SPDX-FileCopyrightText: Copyright 2026 | CSI Piemonte
 * SPDX-License-Identifier: EUPL-1.2
 */

package it.csi.accrual.accrualauthsbapi.be.impl;

import it.csi.accrual.accrualauthsbapi.be.LogoutApi;
import it.csi.accrual.accrualauthsbapi.service.TokenService;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gestisce logout utente
 */
@Component
@Slf4j
public class LogoutApiImpl implements LogoutApi {

	@Inject
	private TokenService tokenService;

	@Override
	public Response logout(@Context SecurityContext securityContext, @Context HttpHeaders httpHeaders, @Context HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.warn("Logout - Authorization header mancante o non valido");
			return Response.status(Response.Status.UNAUTHORIZED).entity("Authorization mancante o non valido").build();
		}

		String token = authHeader.substring(7);
		log.info("Logout richiesto per token: {}", token);

		return tokenService.deleteToken(token)
		        .map(t -> {
		        	log.info("Logout effettuato con successo");
		        	return Response.status(Response.Status.OK).entity("Logout effettuato con successo").build();
		        })
		        .orElseGet(() -> {
		            log.warn("Logout non effettuato - Token non trovato");
		            return Response.status(Response.Status.UNAUTHORIZED)
		                           .entity("Logout non effettuato")
		                           .build();
		        });
	}
}
